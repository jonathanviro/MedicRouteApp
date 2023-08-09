package com.javr.medicrouteapp.ui.paciente

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ListenerRegistration
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.GeoProvider
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.Historial
import com.javr.medicrouteapp.data.network.model.Paciente
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.data.sharedpreferences.PacienteManager
import com.javr.medicrouteapp.databinding.ActivityMapRutaConsultorioBinding
import com.javr.medicrouteapp.utils.MyToolbar

class MapRutaConsultorioActivity : AppCompatActivity(), OnMapReadyCallback, Listener,
    DirectionUtil.DirectionCallBack {
    private lateinit var binding: ActivityMapRutaConsultorioBinding
    private var shpPaciente: Paciente? = null
    private var solicitud: Solicitud? = null
    private var originLatLng: LatLng? = null
    private var ubicacionConsultiorio: LatLng? = null

    private var solicitudListener: ListenerRegistration? = null

    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var marKerPaciente: Marker? = null
    private var markerDestination: Marker? = null
    private var authProvider = AuthProvider()
    private var geoProvider = GeoProvider()
    private var solicitudProvider = SolicitudProvider()
    private var isLocationEnabled = false

    //VARIABLES PARA TRAZAR LA RUTA
    private var lstWayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapRutaConsultorioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().showToolbar(this, "Ruta Consultorio", false)

        // Obtener datos de paciente
        shpPaciente = PacienteManager.obtenerPaciente(this)

        initMap()
        initListener()
        checkEstadoSolicitud()

    }

    private fun initListener() {
        binding.btnCancelarConsulta.setOnClickListener { removeSolicitud() }
    }

    private fun checkEstadoSolicitud() {
        solicitudListener = solicitudProvider.getEstadoSolicitud().addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d("FIRESTORE", "SearchActivity/ERROR ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val solicitud = snapshot.toObject(Solicitud::class.java)
                Log.d("FIRESTORE", "SearchActivity/DATA ${solicitud?.toJson()}")

                if (solicitud?.status == "iniciado") {
                    Log.d("FIRESTORE", "MapRutaConsultorioActivity/ Solicitud Iniciada")
//                    Toast.makeText(this, "Solicitud Iniciada", Toast.LENGTH_LONG).show()
                    binding.btnCancelarConsulta.visibility = View.GONE
                } else if(solicitud?.status == "finalizado"){
                    Log.d("FIRESTORE", "MapRutaConsultorioActivity/ Solicitud Aceptada")
//                    Toast.makeText(this, "Solicitud Aceptada", Toast.LENGTH_LONG).show()
                    binding.btnVerDiagnostico.visibility = View.VISIBLE
                    goToDiagnostico()
                    solicitudListener?.remove()
                } else if(solicitud?.status == "cancelado"){
                    Log.d("FIRESTORE", "MapRutaConsultorioActivity/ Solicitud Cancelado")
//                    Toast.makeText(this, "Solicitud Cancelado", Toast.LENGTH_LONG).show()
                    goToMapPaciente()
                    solicitudListener?.remove()
                }


            }
        }
    }

    private fun goToDiagnostico() {
        val intent = Intent(this, DetailDiagnosticoActivity::class.java)
        intent.putExtra(DetailDiagnosticoActivity.EXTRA_HISTORIAL, "PACIENTE")
        intent.putExtra(DetailDiagnosticoActivity.EXTRA_HISTORIAL, Historial())     //Se envia Historial Nulo para que s epueda calificar. Solo se envia lleno cuando e sun item del Activity HistorialPaciente
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    private fun goToMapPaciente(){
        val intent = Intent(this, MapPacienteActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        googleMap?.isMyLocationEnabled = false
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

    override fun currentLocation(location: Location) {  //  Actualizacion de la posicion en tiempo real
        myLocationLatLng = LatLng(location.latitude, location.longitude)

        googleMap?.moveCamera( CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(myLocationLatLng!!).zoom(17f).build()))

        addPacienteMarker()
        saveLocation()

        if (!isLocationEnabled) {
            Log.d("AQUI", "isLocationEnabled ${isLocationEnabled}" )
            isLocationEnabled = true
            getSolicitud()
        }
    }

    private fun addPacienteMarker() {
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_car_paciente)
        val markerIcon = getMarkerFromDrawable(drawable!!)
        if (marKerPaciente != null) {
            marKerPaciente?.remove() //No redibujar el icono
        }

        if (myLocationLatLng != null) {
            marKerPaciente = googleMap?.addMarker(MarkerOptions().position(myLocationLatLng!!).anchor(0.5f, 0.5f).flat(true).icon(markerIcon))
        }
    }

    private fun saveLocation() {     //GUARDAR LA POSICION DEL USUARIO EN FIREBASE
        if (myLocationLatLng != null) {
            geoProvider.saveLocationWorking(authProvider.getId(), myLocationLatLng!!)
        }
    }

    private fun getSolicitud() {
        solicitudProvider.getSolicitudByPaciente().get().addOnSuccessListener { query ->
            Log.d("AQUI", "getSolicitud ${query}" )
            if (query != null) {
                if (query.size() > 0) {
                    solicitud = query.documents[0].toObject(Solicitud::class.java)
                    Log.d("AQUI", "getSolicitud ${solicitud}" )
                    originLatLng = LatLng(solicitud?.pacienteLat!!, solicitud?.pacienteLng!!)
                    ubicacionConsultiorio = LatLng(solicitud?.consultorioLat!!, solicitud?.consultorioLng!!)

                    initDrawRuta()
                }
            }
        }
    }

    private fun initDrawRuta() {
        Log.d("AQUI", "initDrawRuta ${ubicacionConsultiorio}" )
        if(ubicacionConsultiorio != null) {
            googleMap?.clear()
            addPacienteMarker()
            addConsultorioMarker()
            easyDrawRoute(ubicacionConsultiorio!!)
        }
    }

    //METODO QUE DIBUJA LA RUTA
    private fun easyDrawRoute(position: LatLng) {
        lstWayPoints.clear()
        lstWayPoints.add(myLocationLatLng!!)
        lstWayPoints.add(position!!)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(myLocationLatLng!!)
            .setWayPoints(lstWayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.background_medicrouteapp)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(position!!)
            .build()

        directionUtil.initPath()
    }

    private fun addConsultorioMarker() {
        markerDestination = googleMap?.addMarker(
            MarkerOptions().position(ubicacionConsultiorio!!).title("Consultorio").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_centro_medico_grey))
        )
    }

    private fun removeSolicitud() {
        solicitudProvider.getEstadoSolicitud().get().addOnSuccessListener { document ->
            if(document.exists()){
                val solicitud = document.toObject(Solicitud::class.java)
                if(solicitud?.status == "creado" || solicitud?.status == "valorado" || solicitud?.status == "aceptado" || solicitud?.status == "cancelado"){
                    solicitudProvider.remove()
                    goToVistaPaciente()
                }
            }
        }
    }

    private fun goToVistaPaciente() {
        val intent = Intent(this, MapPacienteActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK//Convertir activity en la activity principal. Eliminando el historial de pantallas
        startActivity(intent)
    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(150, 150,Bitmap.Config.ARGB_8888)

        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, 150, 150)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        //DIBUJAR RUTA
        directionUtil.drawPath(WAY_POINT_TAG)
    }

    override fun locationOn() {
    }

    override fun locationCancelled() {
    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }

}