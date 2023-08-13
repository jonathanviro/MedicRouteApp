package com.javr.medicrouteapp.ui.medico

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.javr.medicrouteapp.data.network.firebase.MedicoProvider
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.data.sharedpreferences.MedicoManager
import com.javr.medicrouteapp.databinding.ActivityEsperaMedicoBinding

class EsperaMedicoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEsperaMedicoBinding
    private val medicoProvider = MedicoProvider()
    private var medicoListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEsperaMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listenerEstadoMedico()
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
                        if(medico != null && medico.status == "activo"){
                            MedicoManager.guardarMedico(this, medico)
                            Log.d("FIRESTORE", "EsperaMedicoActivity/DATA ${medico?.toJson()}")
                            goToVistaMedico()
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

    override fun onDestroy() {
        super.onDestroy()
        medicoListener?.remove()
    }
}