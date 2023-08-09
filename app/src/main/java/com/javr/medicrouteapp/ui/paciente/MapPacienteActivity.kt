package com.javr.medicrouteapp.ui.paciente

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.google.firebase.firestore.GeoPoint
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.GeoProvider
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.MedicoLocation
import com.javr.medicrouteapp.data.network.model.Paciente
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.data.sharedpreferences.PacienteManager
import com.javr.medicrouteapp.databinding.ActivityMapPacienteBinding
import com.javr.medicrouteapp.ui.LoginActivity
import com.javr.medicrouteapp.utils.MoveAnimation
import com.javr.medicrouteapp.utils.MyToolbar
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener

class MapPacienteActivity : AppCompatActivity(), OnMapReadyCallback, Listener {
    private lateinit var binding: ActivityMapPacienteBinding
    private var shpPaciente: Paciente? = null

    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var authProvider = AuthProvider()
    private var geoProvider = GeoProvider()
    private var solicitudProvider = SolicitudProvider()

    //GOOGLE PLACES
    private var isLocationEnabled = false

    private val lstMedicosMarkers = ArrayList<Marker>()
    private val lstMedicosLocations = ArrayList<MedicoLocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shpPaciente = PacienteManager.obtenerPaciente(this)

        MyToolbar().showToolbar(this, "Bienvenido ${shpPaciente?.nombres}", false)

        initMap()
        initListener()
        removeSolicitud()
    }

    private fun initListener() {
        iniWatchers()

        binding.btnSolicitarConsulta.setOnClickListener { goToSearchMedico() }
    }

    private fun goToSearchMedico() {
        if(validarFormulario()){
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra(SearchActivity.EXTRA_NOMBRE_PACIENTE, "${shpPaciente?.nombres} ${shpPaciente?.apellidos}")
            intent.putExtra(SearchActivity.EXTRA_CONSULTA, binding.etConsulta.getText().toString())
            intent.putExtra(SearchActivity.EXTRA_PACIENTE_LAT, myLocationLatLng?.latitude)
            intent.putExtra(SearchActivity.EXTRA_PACIENTE_LNG, myLocationLatLng?.longitude)
            startActivity(intent)
        }
    }

    private fun removeSolicitud() {
        solicitudProvider.getEstadoSolicitud().get().addOnSuccessListener { document ->
            if(document.exists()){
                val solicitud = document.toObject(Solicitud::class.java)
                if(solicitud?.status == "creado" || solicitud?.status == "cancelado" || solicitud?.status == "valorado"){
                    solicitudProvider.remove()
                }
            }
        }
    }


    private fun initMap() {
        //Visualizar mapa en activity
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
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

    private fun getMedicosCercanos() {
        if (myLocationLatLng == null) return

        geoProvider.getMedicosCercanos(myLocationLatLng!!, 100.0).addGeoQueryEventListener(object : GeoQueryEventListener {
                //Se ejecuta cuando encuentre un medico
                override fun onKeyEntered(documentID: String, location: GeoPoint) {
                    //Se recorre todos los medicos que esten disponibles
                    Log.d("FIRESTORE", "DOCUMENTO ${documentID}")
                    Log.d("FIRESTORE", "LOCALIZACIONES ${location}")
                    for (marker in lstMedicosMarkers) {
                        if (marker.tag != null) {
                            if (marker.tag == documentID) {       //Si ya esta agregado sale del onKeyEntered
                                return
                            }
                        }
                    }

                    //Si no existe el medico lo creamos un nuevo marcador para el medico conectado
                    val medicoLatLng = LatLng(location.latitude, location.longitude)
                    Log.d("FIRESTORE", "AQUI ${medicoLatLng}")
                    val marker = googleMap?.addMarker(
                        MarkerOptions().position(medicoLatLng).title("Medico disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_centro_medico_grey))
                    )

                    //Se le asigna el id del medico al tag
                    marker?.tag = documentID
                    lstMedicosMarkers.add(marker!!)

                    //Detectar movimiento del medico
                    val objMedicoLocation = MedicoLocation()
                    objMedicoLocation.id = documentID
                    lstMedicosLocations.add(objMedicoLocation)
                }

                //Elimina el marcador cuando el documento en FIREBASE deja de existir (Es decir el medico se desconecte)
                override fun onKeyExited(documentID: String) {
                    for (marker in lstMedicosMarkers) {
                        if (marker.tag != null) {
                            if (marker.tag == documentID) {
                                marker.remove()
                                lstMedicosMarkers.remove(marker)
                                lstMedicosLocations.removeAt(getPosicionesMedico(documentID))
                                return
                            }
                        }
                    }
                }

                //Este metodo se va a estar ejecutando entiempo real. Cada vez que cambie la posicion del medico se retornara su documento y la ubicacion
                override fun onKeyMoved(documentID: String, location: GeoPoint) {
                    for (marker in lstMedicosMarkers) {
                        val startPosition = LatLng(
                            location.latitude,
                            location.longitude
                        )       //Posicion inicial del medico
                        var endPosition: LatLng? =
                            null                                         //Posicion Final del medico
                        val position = getPosicionesMedico(marker.tag.toString())


                        if (marker.tag != null) {
                            if (marker.tag == documentID) {
//JONATHAN: ESTO DE AQUI ESTABA ANTES DEL IF DE ABAJO                            marker.position = LatLng(location.latitude, location.longitude)
                                if (lstMedicosLocations[position].latLng != null) {
                                    endPosition = lstMedicosLocations[position].latLng
                                }
                                lstMedicosLocations[position].latLng =
                                    LatLng(location.latitude, location.longitude)
                                if (endPosition != null) {
                                    MoveAnimation.animar(marker, endPosition, startPosition)
                                }

                            }
                        }
                    }
                }

                override fun onGeoQueryError(exception: Exception) {
                }

                override fun onGeoQueryReady() {
                }
            })
    }

    //Obtener las posiciones del medico que se esta moviendo
    private fun getPosicionesMedico(id: String): Int {
        var position = 0
        for (i in lstMedicosLocations.indices) {
            if (id == lstMedicosLocations[i].id) {
                position = i
                break
            }
        }

        return position
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = false

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

        googleMap?.isMyLocationEnabled = true//Mostrar marcador original de google
    }

    override fun currentLocation(location: Location) {//Actualizacion de la posicion en tiempo real
        myLocationLatLng = LatLng(location.latitude, location.longitude)//Latitud y longitud de la posicion actual

        if (!isLocationEnabled) {     //INGRESARA UNA SOLA VEZ
            isLocationEnabled = true

            googleMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder().target(myLocationLatLng!!).zoom(15f).build()
                )
            )

            getMedicosCercanos()
        }
    }

    private fun goToMain() {
        authProvider.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun goToHistorial() {
        val intent = Intent(this, HistorialPacienteActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_contextual, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_one) {
            val intent = Intent(this, PerfilPacienteActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (item.itemId == R.id.option_two) {
            goToHistorial()
        }

        if (item.itemId == R.id.option_three) {
            goToMain()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun locationOn() {
    }

    override fun locationCancelled() {
    }

    private fun validarFormulario(): Boolean {
        if (binding.etConsulta.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilConsulta,
                this.getString(R.string.not_insert_consulta)
            )
            return false
        }

        return true
    }
    private fun iniWatchers() {
        Global.setErrorInTextInputLayout(binding.etConsulta, binding.tilConsulta)
    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates();
    }

}