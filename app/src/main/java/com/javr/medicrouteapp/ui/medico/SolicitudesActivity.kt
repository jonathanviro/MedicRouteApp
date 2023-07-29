package com.javr.medicrouteapp.ui.medico

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ListenerRegistration
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.GeoProvider
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.data.sharedpreferences.MedicoManager
import com.javr.medicrouteapp.databinding.ActivitySolicitudesBinding
import com.javr.medicrouteapp.ui.adapter.SolicitudAdapter
import com.javr.medicrouteapp.ui.fragments.ModalBottomSolicitudForMedico
import com.javr.medicrouteapp.utils.MyToolbar

class SolicitudesActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySolicitudesBinding
    private var shpMedico: Medico? = null
    lateinit var solicitudAdapter: SolicitudAdapter
    private val lstSolicitudes = mutableListOf<Solicitud>()

    private var ubicacionConsultorio: LatLng? = null
    private var authProvider = AuthProvider()
    private var geoProvider = GeoProvider()
    private var solicitudProvider = SolicitudProvider()
    private val modalSolicitud = ModalBottomSolicitudForMedico()

    private var solicitudListener: ListenerRegistration? = null
    private var solicitudesAceptadasListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().showToolbar(this, "Solicitudes", false)

        // Setear ubicacion de consultorio
        shpMedico = MedicoManager.obtenerMedico(this)
        ubicacionConsultorio = LatLng(shpMedico?.consultorioLat!!, shpMedico?.consultorioLng!!)

        initListener()
        initRecyclerView()
        checkUsuarioIsConectected()
    }

    private fun initListener() {
        binding.btnConectar.setOnClickListener { connect() }
        binding.btnDesconectar.setOnClickListener { disconnect() }

        listarSolicitudesAceptadas()
        listenerSolicitudes()
    }

    private fun listarSolicitudesAceptadas() {
        solicitudesAceptadasListener = solicitudProvider.getSolicitudesByMedicoYStatus(authProvider.getId(), "aceptado").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d("FIRESTORE", "ERROR ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null)  {
                if (snapshot.documents.size > 0) {
                    val solicitudes = snapshot.toObjects(Solicitud::class.java)
                    lstSolicitudes.clear()
                    lstSolicitudes.addAll(solicitudes)
                    solicitudAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    //ESCUCHAR SOLICITUDES DE ATENCION MEDICA
    private fun listenerSolicitudes() {
        solicitudListener = solicitudProvider.getSolicitudesCreadasForMedico().addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d("FIRESTORE", "ERROR ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                if (snapshot.documents.size > 0) {
                    val lstSolicitudesActuales = snapshot.toObjects(Solicitud::class.java)
                    for (solicitud in lstSolicitudesActuales){
                        if(solicitud != null && solicitud.status == "creado"){
                            Log.d("FIRESTORE", "SolicitudesActivity/DATA ${solicitud?.toJson()}")
                            showModalSolicitud(solicitud)
                        }
                    }
                }
            }
        }
    }

    private fun showModalSolicitud(solicitud: Solicitud?) {
        val bundle = Bundle()
        bundle.putString("solicitud", solicitud?.toJson())
        modalSolicitud.arguments = bundle
        modalSolicitud.isCancelable = false     //  No se puede ocultar el modal
        modalSolicitud.show(supportFragmentManager, ModalBottomSolicitudForMedico.TAG)
    }

    private fun checkUsuarioIsConectected() {
        geoProvider.getLocation(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()) {
                if (document.contains("l")) {     //  l: es el campo que contiene la latitud y longitud en firebase
                    connect()
                } else {
                    showButtonConnection(true)
                }
            } else {
                showButtonConnection(true)
            }
        }
    }

    private fun disconnect() {
        if (ubicacionConsultorio != null) {
            geoProvider.removeLocation(authProvider.getId())    //  Eliminamos la ubicacion del medico de la coleccion "LocalizacionesMedicosConectados"
            showButtonConnection(true)
        }
    }

    private fun connect() {
        saveLocation()
        showButtonConnection(false)
    }

    private fun showButtonConnection(isConnectVisible: Boolean) {
        binding.btnConectar.visibility = if (isConnectVisible) View.VISIBLE else View.GONE
        binding.btnDesconectar.visibility = if (isConnectVisible) View.GONE else View.VISIBLE
    }

    private fun initRecyclerView() {
        solicitudAdapter = SolicitudAdapter(lstSolicitudes) { solicitud ->
            onSelectedItem(solicitud)
        }

        binding.rvSolicitudes.layoutManager = LinearLayoutManager(this)
        binding.rvSolicitudes.adapter = solicitudAdapter
    }

    fun onSelectedItem(solicitud: Solicitud) {
        val intent = Intent(this, DetailPacienteActivity::class.java)
        intent.putExtra(DetailPacienteActivity.EXTRA_PACIENTE, solicitud)
        startActivity(intent)
//        finish()
    }

    private fun saveLocation() {     //GUARDAR LA POSICION DEL USUARIO EN FIREBASE
        if (ubicacionConsultorio != null) {
            geoProvider.saveLocation(authProvider.getId(), ubicacionConsultorio!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        solicitudListener?.remove()
        solicitudesAceptadasListener?.remove()
    }
}