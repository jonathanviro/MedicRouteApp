package com.javr.medicrouteapp.ui.medico

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import com.javr.medicrouteapp.core.Validator
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.MedicoProvider
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.data.sharedpreferences.MedicoManager
import com.javr.medicrouteapp.databinding.ActivityPerfilMedicoBinding
import com.javr.medicrouteapp.toolbar.Toolbar
import com.javr.medicrouteapp.ui.LoginActivity

class PerfilMedicoActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, Listener {
    private lateinit var binding: ActivityPerfilMedicoBinding
    private var shpMedico: Medico? = null
    private val authProvider = AuthProvider()
    private val medicoProvider = MedicoProvider()
    private var uriImagen: Uri? = null
    private var uriPdf: Uri? = null

    private var isCargaInicial: Boolean = false
    private var isCambioUbicacion: Boolean = false
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var consultorioLat: Double? = null
    private var consultorioLng: Double? = null
    private var ubicacionActual: LatLng? = null
    private var ubicacionConsultorio: LatLng? = null
    private var marker: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Toolbar().showToolbar(this, "Mi Perfil", true)

        shpMedico = MedicoManager.obtenerMedico(this)

        initMap()
        initComponents()
        initListener()
    }

    private fun initMap() {
        //Visualizar mapa en fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapConsultorioPerfil) as SupportMapFragment
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

        consultorioLat = shpMedico?.consultorioLat!!
        consultorioLng = shpMedico?.consultorioLng!!
        ubicacionConsultorio =  LatLng(consultorioLat!!, consultorioLng!!)//Latitud y longitud de la posicion actual
    }

    private fun initComponents() {
        iniWatchers()
        binding.etNombres.setText(shpMedico?.nombres)
        binding.etApellidos.setText(shpMedico?.apellidos)
        binding.etCedula.setText(shpMedico?.cedula)
        binding.etTelefono.setText(shpMedico?.telefono)
        binding.tvSexo.setText(shpMedico?.sexo)
        binding.etRazonSocial.setText(shpMedico?.razonSocial)
        binding.etRuc.setText(shpMedico?.ruc)
        binding.etRegistroSanitario.setText(shpMedico?.registroSanitario)
        binding.etUbicacion.setText("$consultorioLat, $consultorioLng")

        if(shpMedico?.imagenUrl != null){
            Glide.with(this).load(shpMedico?.imagenUrl).into(binding.ivFotoPerfil)
        }
    }

    private fun iniWatchers() {
        //Watcher Errores
        Global.setErrorInTextInputLayout(binding.etNombres, binding.tilNombres)
        Global.setErrorInTextInputLayout(binding.etApellidos, binding.tilApellidos)
        Global.setErrorInTextInputLayout(binding.etCedula, binding.tilCedula)
        Global.setErrorInTextInputLayout(binding.etTelefono, binding.tilTelefono)
    }

    private fun initListener() {
        binding.ivFotoPerfil.setOnClickListener { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        binding.ivUploadPdf.setOnClickListener { selectedPdf() }
        binding.ivDownloadPdf.setOnClickListener { downloadPdf() }
        binding.btnActualizar.setOnClickListener { actualizarUsuario() }
    }

    private fun actualizarUsuario() {
        if (validarFormulario()) {
            val dialogoCarga = Global.dialogoCarga(this, "Actualizando Datos")
            dialogoCarga.show()

            val medico = Medico(
                id = authProvider.getId(),
                nombres = binding.etNombres.text.toString(),
                apellidos = binding.etApellidos.text.toString(),
                cedula = binding.etCedula.text.toString(),
                telefono = binding.etTelefono.text.toString(),
                sexo = binding.tvSexo.text.toString(),
                razonSocial = binding.etRazonSocial.text.toString(),
                ruc = binding.etRuc.text.toString(),
                registroSanitario = binding.etRegistroSanitario.text.toString(),
                consultorioLat = consultorioLat,
                consultorioLng = consultorioLng,
                imagenUrl = shpMedico?.imagenUrl,
                pdfUrl = shpMedico?.pdfUrl)


            if(uriImagen != null){
                medicoProvider.uploadImagen(authProvider.getId(), uriImagen!!).addOnSuccessListener {taskSnapshot ->
                    medicoProvider.getImagenUrl(authProvider.getId()).addOnSuccessListener {url ->
                        val imageUrl = url.toString()
                        medico.imagenUrl = imageUrl
                        Log.d("STORAGE", "URL IMAGE: $imageUrl")

                        medicoProvider.update(medico).addOnCompleteListener {
                            if(it.isSuccessful){
                                dialogoCarga.dismiss()
                                MedicoManager.guardarMedico(this, medico)
                                Toast.makeText(this@PerfilMedicoActivity, "Datos actualizados", Toast.LENGTH_LONG).show()
                            }else{
                                dialogoCarga.dismiss()
                                Toast.makeText(this@PerfilMedicoActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }else if(uriPdf != null){
                medicoProvider.uploadPdf(authProvider.getId(), uriPdf!!).addOnSuccessListener {taskSnapshotPdf ->
                    medicoProvider.getPdfUrl(authProvider.getId()).addOnSuccessListener { urlPdf ->
                        val pdfUrl = urlPdf.toString()
                        medico.pdfUrl = pdfUrl
                        Log.d("STORAGE", "URL PDF: $pdfUrl")
                        medicoProvider.update(medico).addOnCompleteListener {
                            if(it.isSuccessful){
                                dialogoCarga.dismiss()
                                MedicoManager.guardarMedico(this, medico)
                                Toast.makeText(this@PerfilMedicoActivity, "Datos actualizados", Toast.LENGTH_LONG).show()
                            }else{
                                dialogoCarga.dismiss()
                                Toast.makeText(this@PerfilMedicoActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }else{
                medicoProvider.update(medico).addOnCompleteListener {
                    if(it.isSuccessful){
                        dialogoCarga.dismiss()
                        MedicoManager.guardarMedico(this, medico)
                        Toast.makeText(this@PerfilMedicoActivity, "Datos actualizados", Toast.LENGTH_LONG).show()
                    }else{
                        dialogoCarga.dismiss()
                        Toast.makeText(this@PerfilMedicoActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun validarFormulario(): Boolean {

        if (binding.etNombres.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilNombres,
                this.getString(R.string.not_insert_names)
            )
            return false
        }

        if (binding.etApellidos.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilApellidos,
                this.getString(R.string.not_insert_lastnames)
            )
            return false
        }

        if (binding.etCedula.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilCedula,
                this.getString(R.string.not_insert_passport)
            )
            return false
        } else {
            if (!Validator.isValidCedula(binding.etCedula.text.toString())) {
                Global.setErrorInTextInputLayout(
                    binding.tilCedula,
                    this.getString(R.string.invalid_passport)
                )
                return false
            }
        }

        if (binding.etTelefono.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilTelefono,
                this.getString(R.string.not_insert_phone)
            )
            return false
        } else {
            if (binding.etTelefono.text.toString().length < 10) {
                Global.setErrorInTextInputLayout(
                    binding.tilTelefono,
                    this.getString(R.string.invalid_phone)
                )
                return false
            }
        }

        return true
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.isMyLocationEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        //easyWayLocation?.startLocation(); //INICIA EL APP CON LA LOCALIZACION DEL USUARIO. QUEREMOS QUE EL USUARIO UTILICE EL BOTON CONECTARSE.
        googleMap!!.setOnMapClickListener(this)
        googleMap!!.setOnMapLongClickListener(this)


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

    override fun onMapClick(ubicacion: LatLng) {
        isCambioUbicacion = true

        easyWayLocation?.endUpdates()
        addMarker(ubicacion)

        consultorioLat = ubicacion.latitude
        consultorioLng = ubicacion.longitude

        binding.etUbicacion.setText("$consultorioLat, $consultorioLng")
    }

    override fun onMapLongClick(ubicacion: LatLng) {
        isCambioUbicacion = true

        easyWayLocation?.endUpdates()
        addMarker(ubicacion)

        consultorioLat = ubicacion.latitude
        consultorioLng = ubicacion.longitude

        binding.etUbicacion.setText("$consultorioLat, $consultorioLng")
    }

    override fun currentLocation(location: Location?) {
        if(!isCargaInicial){
            isCargaInicial = true
            addMarker(ubicacionConsultorio!!)
        }

        if(isCambioUbicacion){
            ubicacionActual =  LatLng(location?.latitude!!, location?.longitude!!)//Latitud y longitud de la posicion actual
            addMarker(ubicacionActual!!)

            consultorioLat = ubicacionActual?.latitude
            consultorioLng = ubicacionActual?.longitude

            binding.etUbicacion.setText("$consultorioLat, $consultorioLng")
        }
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

    private fun goToHistorialAtenciones() {
        val intent = Intent(this, HistorialAtencionesActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_contextual, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_one) {
            val intent = Intent(this, PerfilMedicoActivity::class.java)
            startActivity(intent)
        }

        if (item.itemId == R.id.option_two) {
            goToHistorialAtenciones()
        }

        if (item.itemId == R.id.option_three) {
            goToMain()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun selectedPdf() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }

        pickPdfFile.launch(intent)
    }

    private fun downloadPdf() {
        var pdfUrl = shpMedico?.pdfUrl
        pdfUrl?.let {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val request = DownloadManager.Request(Uri.parse(pdfUrl))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle("Descargando PDF")
                .setDescription("Descargando archivo PDF...")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${authProvider.getId()}.pdf")

            val downloadId = downloadManager.enqueue(request)

            // Opcional: Puedes agregar un BroadcastReceiver para detectar cuando se completa la descarga
            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Toast.makeText(this@PerfilMedicoActivity, "Se ha descargado el PDF", Toast.LENGTH_LONG).show()
                }
            }

            registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.ivFotoPerfil.setImageURI(uri)
                uriImagen = uri
            } else {
                Toast.makeText(this, "NO HA SELECCIONADO FOTO", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickPdfFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let { pdfUri ->
                    val cursor = this.contentResolver.query(uri, null, null, null, null)
                    cursor?.let {
                        if (it.moveToFirst()) {
                            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val fileName =
                                if (nameIndex >= 0) it.getString(nameIndex) else uri.lastPathSegment

//                            binding.tvNombrePdf.text = fileName
                            uriPdf = uri
                        }
                        it.close()
                    }
                }
            }
        }

    private fun addMarker(position: LatLng) {
        googleMap?.clear()
        marker = googleMap?.addMarker(MarkerOptions().position(position!!).title("Mi Consultorio").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_centro_medico_grey)))
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition( CameraPosition.builder().target(position).zoom(17f).build()))
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
}