package com.javr.medicrouteapp.ui.paciente

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.GeoProvider
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.Paciente
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.data.sharedpreferences.PacienteManager
import com.javr.medicrouteapp.databinding.ActivitySearchBinding
import layout.fragments.ModalBottomSolicitudForPaciente
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener
import java.util.Date

class SearchActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_NOMBRE_PACIENTE= "SearchActivity:nombrePaciente"
        const val EXTRA_CONSULTA= "SearchActivity:consulta"
        const val EXTRA_PACIENTE_LAT= "SearchActivity:pacienteLat"
        const val EXTRA_PACIENTE_LNG= "SearchActivity:pacienteLng"
    }
    
    private lateinit var binding: ActivitySearchBinding
    private var shpPaciente: Paciente? = null
    private var extraNombrePaciente = ""
    private var extraConsulta = ""
    private var extraPacienteLat = 0.0
    private var extraPacienteLng = 0.0
    private val modalSolicitud = ModalBottomSolicitudForPaciente()

    private var ubicacionPaciente: LatLng? = null

    private val authProvider = AuthProvider()
    private val geoProvider = GeoProvider()
    private val solicitudProvider = SolicitudProvider()

    //BUSQUEDA DE MEDICO
    private var radius = 0.2
    private var idMedico = ""
    private var medicoLatLng: LatLng? = null
    private var isMedicoFound = false
    private var limitRadius = 20

    private var solicitudListener: ListenerRegistration? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Obtener datos de paciente
        shpPaciente = PacienteManager.obtenerPaciente(this)

        initComponents()
        getMedicoMasCercano()
        checkEstadoSolicitud()
    }

    private fun initComponents() {
        //EXTRAS
        extraNombrePaciente = intent.getStringExtra(EXTRA_NOMBRE_PACIENTE)!!
        extraConsulta = intent.getStringExtra(EXTRA_CONSULTA)!!
        extraPacienteLat = intent.getDoubleExtra(EXTRA_PACIENTE_LAT, 0.0)!!
        extraPacienteLng = intent.getDoubleExtra(EXTRA_PACIENTE_LNG, 0.0)!!

        ubicacionPaciente = LatLng(extraPacienteLat, extraPacienteLng)
    }

    private fun getMedicoMasCercano(){
        geoProvider.getMedicosCercanos(ubicacionPaciente!!, radius).addGeoQueryEventListener(object : GeoQueryEventListener{
            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                if (!isMedicoFound){
                    isMedicoFound = true
                    medicoLatLng = LatLng(location.latitude, location.longitude)

                    Log.d("FIRESTORE", "Medico id: $documentID")

                    binding.tvSearch.text = "MEDICO ENCONTRADO\nESPERANDO RESPUESTA"

                    createSolicitudConsulta(documentID)
                }
            }

            override fun onKeyExited(documentID: String) {
                
            }

            override fun onKeyMoved(documentID: String, location: GeoPoint) {
                
            }
            
            override fun onGeoQueryError(exception: Exception) {
                
            }

            override fun onGeoQueryReady() {    //SE EJECUTA CUANDO TERMINA LA BUSQUEDA
                //Se ejecutara solo si no encontro el medico
                if(!isMedicoFound){
                    radius = radius + 0.2

                    if(radius > limitRadius){
                        binding.tvSearch.text = "NO SE ENCONTRO NINGUN MEDICO DISPONIBLE"
                    }else{
                        getMedicoMasCercano()
                    }
                }
            }
        })
    }

    private fun createSolicitudConsulta(idMedico: String){
        val objSolicitud = Solicitud(
            idPaciente = authProvider.getId(),
            idMedico = idMedico,
            status = "creado",
            nombrePaciente = extraNombrePaciente,
            nombreMedico = null,
            nombreConsultorio = null,
            consulta = extraConsulta,
            horaAgendada= null,
            precio = null,
            pacienteLat = extraPacienteLat,
            pacienteLng = extraPacienteLng,
            consultorioLat = null,
            consultorioLng = null,
            timestampActualizacion = Date().time
        )

        solicitudProvider.create(objSolicitud).addOnCompleteListener {
            if(it.isSuccessful){
                Log.d("FIRESTORE", "SearchActivity/ Datos de la solicitud creados")
//                Toast.makeText(this@SearchActivity, "Datos de la solicitud creados", Toast.LENGTH_LONG).show()
            }else{
                Log.d("FIRESTORE", "SearchActivity/ERROR al crear datos de la solicitud")
                Toast.makeText(this, "Error al crear datos de la solicitud", Toast.LENGTH_LONG).show()
            }
        }
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

                if (solicitud?.status == "valorado") {
                    Log.d("FIRESTORE", "SearchActivity/ Solicitud Valorada")
//                    Toast.makeText(this@SearchActivity, "Solicitud Valorada", Toast.LENGTH_LONG).show()
                    showModalSolicitud(solicitud)
                } else if(solicitud?.status == "aceptado"){
                    Log.d("FIRESTORE", "SearchActivity/ Solicitud Aceptada")
//                    Toast.makeText(this@SearchActivity, "Solicitud Aceptada", Toast.LENGTH_LONG).show()
                    goToMapRutaConsultorio()
                    solicitudListener?.remove()
                } else if(solicitud?.status == "cancelado"){
                    Log.d("FIRESTORE", "SearchActivity/ Solicitud Cancelado")
//                    Toast.makeText(this@SearchActivity, "Solicitud Cancelado", Toast.LENGTH_LONG).show()
                    goToMapPaciente()
                    solicitudListener?.remove()
                }


            }
        }
    }

    private fun showModalSolicitud(solicitud: Solicitud?) {
        val bundle = Bundle()
        bundle.putString("solicitud", solicitud?.toJson())
        modalSolicitud.arguments = bundle
        modalSolicitud.isCancelable = false     //  No se puede ocultar el modal
        modalSolicitud.show(supportFragmentManager, ModalBottomSolicitudForPaciente.TAG)
    }

    private fun goToMapRutaConsultorio(){
        val intent = Intent(this, MapRutaConsultorioActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

    }

    private fun goToMapPaciente(){
        val intent = Intent(this, MapPacienteActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        solicitudListener?.remove()
    }
}