package com.javr.medicrouteapp.ui.administrador

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.HistorialProvider
import com.javr.medicrouteapp.data.network.firebase.MedicoProvider
import com.javr.medicrouteapp.data.network.model.Historial
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.databinding.ActivityDetailMedicoBinding
import com.javr.medicrouteapp.toolbar.Toolbar
import com.javr.medicrouteapp.ui.LoginActivity

class DetailMedicoActivity : AppCompatActivity(), OnMapReadyCallback, Listener {
    companion object {
        const val EXTRA_MEDICO = "DetailMedicoActivity:Medico"
    }

    private lateinit var binding: ActivityDetailMedicoBinding
    private lateinit var extraObjMedico: Medico
    private lateinit var dialogoCarga: AlertDialog
    private var ubicacionConsultorio: LatLng? = null
    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var medicoProvider = MedicoProvider()
    private val authProvider = AuthProvider()
    private val historialProvider = HistorialProvider()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Toolbar().showToolbar(this, "Detalle de Médico", false)

        extraObjMedico = intent.getParcelableExtra<Medico>(EXTRA_MEDICO)!!
        Log.d("DETALLE MEDICO: ", extraObjMedico.toString())

        initMap()
        iniComponents()
        initListener()
    }

    private fun iniComponents() {
        binding.tvNombreMedico.text = "${extraObjMedico.nombres} ${extraObjMedico.apellidos}"
        binding.tvSexoMedico.text = extraObjMedico.sexo
        binding.tvCedulaMedico.text = extraObjMedico.cedula
        binding.tvRazonSocialMedico.text = extraObjMedico.razonSocial
        binding.tvRucMedico.text = extraObjMedico.ruc
        binding.tvRegistroSanitarioMedico.text = extraObjMedico.registroSanitario

        if(extraObjMedico.imagenUrl != null){
            Glide.with(this).load(extraObjMedico.imagenUrl).into(binding.ivFotoPerfil)
        }

        if(extraObjMedico.status == "activo"){
            showButtonDeshabilitar()
            getAllHistorial()
        }
    }

    private fun getAllHistorial() {
        historialProvider.getAllHistorialByMedico(extraObjMedico.id!!).get().addOnSuccessListener { query ->
            var totalCalificaciones = 0f
            var totalNumCalificaciones = 0
            var calificacionMedico = 0f

            if(query != null){
                if(query.documents.size > 0){
                    for (document in query.documents) {
                        val historial = document.toObject(Historial::class.java)
                        if (historial != null) {
                            totalCalificaciones += historial.calificacionParaMedico!!
                            totalNumCalificaciones++
                        }
                    }

                    if (totalCalificaciones > 0) {
                        calificacionMedico = totalCalificaciones / totalNumCalificaciones.toFloat()
                        Log.d("FIRESTORE", "ActivityDetailMedicoBinding/CALIFICACION $calificacionMedico")
                    }
                }else{
                    Log.d("FIRESTORE", "ActivityDetailMedicoBinding/No se encontro el historial de calificación")
                }
            }

            binding.rbCalificacion.rating = calificacionMedico
        }
    }

    private fun initListener() {
        binding.btnHabilitarMedico.setOnClickListener { habilitarMedico() }
        binding.btnDeshabilitarMedico.setOnClickListener { deshabilitarMedico() }
        binding.btnCancelarMedico.setOnClickListener { goToMedicosPendientes() }
        binding.ivDownloadPdf.setOnClickListener { downloadPdf() }
    }

    private fun habilitarMedico() {
        dialogoCarga = Global.dialogoCarga(this, "Habilitando médico")
        dialogoCarga.show()
        medicoProvider.updateStatus(extraObjMedico.id!!, "activo").addOnCompleteListener {
            dialogoCarga.dismiss()
            goToMedicosPendientes()
        }
    }

    private fun deshabilitarMedico() {
        dialogoCarga = Global.dialogoCarga(this, "Deshabilitando médico")
        dialogoCarga.show()
        medicoProvider.updateStatus(extraObjMedico.id!!, "pendiente").addOnCompleteListener {
            dialogoCarga.dismiss()
            goToMedicosActivos()
        }
    }

    private fun goToMedicosPendientes() {
        val intent = Intent(this, MedicosPendientesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun goToMedicosActivos() {
        val intent = Intent(this, MedicosActivosActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun initMap() {
        //Visualizar mapa en fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapConsultorioMedico) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)

        locationPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        ubicacionConsultorio =  LatLng(extraObjMedico.consultorioLat!!, extraObjMedico.consultorioLng!!)//Latitud y longitud de la posicion actual
    }

    private fun addMarker(position: LatLng) {
        googleMap?.clear()
        marker = googleMap?.addMarker(MarkerOptions().position(position!!).title("${extraObjMedico.razonSocial}").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_centro_medico_grey)))
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition( CameraPosition.builder().target(position).zoom(17f).build()))
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        googleMap?.isMyLocationEnabled = false//Mostrar marcador original de google
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
    }

    override fun currentLocation(location: Location?) {
        addMarker(ubicacionConsultorio!!)
    }

    override fun locationOn() {
    }

    override fun locationCancelled() {
    }

    private fun goToMain() {
        authProvider.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_one) {
            goToMain()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showButtonDeshabilitar() {
        binding.btnHabilitarMedico.visibility = View.GONE
        binding.btnDeshabilitarMedico.visibility = View.VISIBLE
    }

    private fun downloadPdf() {
        dialogoCarga = Global.dialogoCarga(this, "Espere un momento")
        dialogoCarga.show()

        var pdfUrl = extraObjMedico?.pdfUrl
        pdfUrl?.let {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val request = DownloadManager.Request(Uri.parse(pdfUrl))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle("RegistroSanitario_${extraObjMedico.apellidos}${extraObjMedico.nombres}.pdf")
                .setDescription("Descargando Registro Sanitario...")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${authProvider.getId()}.pdf")

            val downloadId = downloadManager.enqueue(request)

            // Opcional: Puedes agregar un BroadcastReceiver para detectar cuando se completa la descarga
            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Toast.makeText(context, "Registro Sanitario Descargado", Toast.LENGTH_LONG).show()
                    dialogoCarga.dismiss()
                }
            }

            registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        Log.d("LOCALIZACION", "Permiso concedido")
                        easyWayLocation?.startLocation();
                    }

                    permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        Log.d("LOCALIZACION", "Permiso concedido con limitacion")
                        easyWayLocation?.startLocation();
                    }

                    else -> {
                        Log.d("LOCALIZACION", "Permiso no concedido")
                    }
                }
            }
        }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        if(extraObjMedico.status == "activo"){
            startActivity(Intent(this, MedicosActivosActivity::class.java))
        }else{
            startActivity(Intent(this, MedicosPendientesActivity::class.java))
        }

        finish()
    }

}