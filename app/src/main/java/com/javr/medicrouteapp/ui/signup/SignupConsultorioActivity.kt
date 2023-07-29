package com.javr.medicrouteapp.ui.signup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.databinding.ActivitySignupConsultorioBinding
import com.javr.medicrouteapp.toolbar.Toolbar

class SignupConsultorioActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, Listener {

    private lateinit var binding: ActivitySignupConsultorioBinding
    private var googleMap: GoogleMap? = null
    private var consultorioLat: Double? = null
    private var consultorioLng: Double? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var ubicacionActual: LatLng? = null
    private var marker: Marker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupConsultorioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        initListener()
    }

    private fun initComponents() {
        Toolbar().showToolbar(this, "Registro de Consultorio Médico", true)

        initMap()
    }

    private fun initListener() {
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, SignupUsuarioActivity::class.java)
            intent.putExtra(SignupUsuarioActivity.EXTRA_TIPO_USUARIO, "MEDICO")
            intent.putExtra(SignupUsuarioActivity.EXTRA_RAZON_SOCIAL, binding.etRazonSocial.text.toString())
            intent.putExtra(SignupUsuarioActivity.EXTRA_RUC, binding.etRuc.text.toString())
            intent.putExtra(SignupUsuarioActivity.EXTRA_DIRECCION, binding.etUbicacion.text.toString())
            intent.putExtra(SignupUsuarioActivity.EXTRA_REGISTRO_SANITARIO, binding.etRegistroSanitario.text.toString())
            intent.putExtra(SignupUsuarioActivity.EXTRA_CONSULTORIO_LAT, consultorioLat)
            intent.putExtra(SignupUsuarioActivity.EXTRA_CONSULTORIO_LNG, consultorioLng)
            startActivity(intent)
        }
    }

    private fun initMap() {
        //Visualizar mapa en fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapConsultorio) as SupportMapFragment
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

    private fun addMarker(position: LatLng) {
        googleMap?.clear()
        marker = googleMap?.addMarker(MarkerOptions().position(position!!).title("Mi Consultorio").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_person)))
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition( CameraPosition.builder().target(position).zoom(17f).build()))
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        //easyWayLocation?.startLocation(); //INICIA EL APP CON LA LOCALIZACION DEL USUARIO. QUEREMOS QUE EL USUARIO UTILICE EL BOTON CONECTARSE.
        googleMap!!.setOnMapClickListener(this)
        googleMap!!.setOnMapLongClickListener(this)


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

    override fun onMapClick(ubicacion: LatLng) {
        easyWayLocation?.endUpdates()
        addMarker(ubicacion)

        consultorioLat = ubicacion.latitude
        consultorioLng = ubicacion.longitude

        binding.etUbicacion.setText("${consultorioLat} , ${consultorioLng}")
    }

    override fun onMapLongClick(ubicacion: LatLng) {
        easyWayLocation?.endUpdates()
        addMarker(ubicacion)

        consultorioLat = ubicacion.latitude
        consultorioLng = ubicacion.longitude

        binding.etUbicacion.setText("${consultorioLat} , ${consultorioLng}")
    }

    override fun currentLocation(location: Location?) {
        ubicacionActual =  LatLng(location?.latitude!!, location?.longitude!!)//Latitud y longitud de la posicion actual
        addMarker(ubicacionActual!!)

        consultorioLat = ubicacionActual?.latitude
        consultorioLng = ubicacionActual?.longitude

        binding.etUbicacion.setText("${consultorioLat} , ${consultorioLng}")
    }

    override fun locationOn() {
    }

    override fun locationCancelled() {
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

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }
}