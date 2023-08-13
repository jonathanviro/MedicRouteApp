package com.javr.medicrouteapp.ui.medico

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.firestore.ListenerRegistration
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.MedicoProvider
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.data.sharedpreferences.MedicoManager
import com.javr.medicrouteapp.databinding.ActivityEsperaMedicoBinding

class EsperaMedicoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEsperaMedicoBinding
    private lateinit var dialogoCarga: AlertDialog
    private val authProvider = AuthProvider()
    private val medicoProvider = MedicoProvider()
    private var medicoListener: ListenerRegistration? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEsperaMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
        listenerEstadoMedico()
    }

    private fun initListener() {
        binding.btnReenviarSolicitud.setOnClickListener { reenviarSolicitud() }
    }

    private fun reenviarSolicitud() {
        dialogoCarga = Global.dialogoCarga(this, "Reenviando solicitud")
        dialogoCarga.show()
        medicoProvider.updateStatus(authProvider.getId(), "pendiente").addOnCompleteListener {
            dialogoCarga.dismiss()
            limpiar()
        }
    }

    private fun limpiar() {
        binding.tvEstadoCuenta.text = "Esperando validación de cuenta"
        binding.btnReenviarSolicitud.visibility = View.GONE
    }

    private fun listenerEstadoMedico() {
        medicoListener = medicoProvider.getMedicoRealTime().addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d("FIRESTORE", "ERROR ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                if (snapshot.documents.size > 0) {
                    val lstMedicosActuales = snapshot.toObjects(Medico::class.java)
                    for (medico in lstMedicosActuales){
                        if(medico != null){
                            Log.d("FIRESTORE", "EsperaMedicoActivity/DATA ${medico?.toJson()}")
                            MedicoManager.guardarMedico(this, medico)
                            when (medico.status){
                                "activo" -> goToVistaMedico()
                                "rechazado" -> goToVistaRechazado()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun goToVistaMedico() {
        val intent = Intent(this, SolicitudesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun goToVistaRechazado() {
        binding.tvEstadoCuenta.text = "Su solicitud ha sido rechazada"
        binding.btnReenviarSolicitud.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        medicoListener?.remove()
    }
}