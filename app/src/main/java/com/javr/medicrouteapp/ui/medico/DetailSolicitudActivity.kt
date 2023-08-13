package com.javr.medicrouteapp.ui.medico

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.GeoProvider
import com.javr.medicrouteapp.data.network.firebase.HistorialProvider
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.Historial
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.databinding.ActivityDetailSolicitudBinding
import com.javr.medicrouteapp.ui.LoginActivity
import com.javr.medicrouteapp.utils.MyToolbar
import java.util.Date

class DetailSolicitudActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SOLICITUD = "DetailSolicitudActivity:Solicitud"
    }

    private lateinit var binding: ActivityDetailSolicitudBinding
    private lateinit var extraObjSolicitud: Solicitud
    private var calificacion = 0f
    private var authProvider = AuthProvider()
    private var geoProvider = GeoProvider()
    private var solicitudProvider = SolicitudProvider()
    private var historialProvider =  HistorialProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailSolicitudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().showToolbar(this, "Detalle de Atención", false)

        extraObjSolicitud = intent.getParcelableExtra<Solicitud>(EXTRA_SOLICITUD)!!
        Log.d("DETALLE PACIENTE: ", extraObjSolicitud.toString())

        initComponet()
        initListener()

    }

    private fun initComponet() {
        binding.tvMotivoConsulta.text = extraObjSolicitud.consulta
    }

    private fun initListener() {
        iniWatchers()

        binding.rbCalificacion.setOnRatingBarChangeListener { ratingBar, value, fromUser ->
            calificacion = value
        }

        binding.btnFinalizarConsulta.setOnClickListener {
            if (validarFormulario()) {
                diagnosticar()
            }
        }
    }

    private fun diagnosticar() {
        geoProvider.removeLocationWorking(authProvider.getId())
        crearHistorial()
    }

    private fun crearHistorial() {
        val historial = Historial(
            idMedico = authProvider.getId(),
            idPaciente = extraObjSolicitud.idPaciente,
            calificacionParaPaciente = calificacion,
            nombrePaciente = extraObjSolicitud.nombrePaciente,
            nombreMedico = extraObjSolicitud.nombreMedico,
            nombreConsultorio = extraObjSolicitud.nombreConsultorio,
            consulta = extraObjSolicitud.consulta,
            horaAgendada = extraObjSolicitud.horaAgendada,
            precio = extraObjSolicitud.precio,
            diagnostico = binding.etDiagnostico.text.toString(),
            receta = binding.etReceta.text.toString(),
            timestamp = Date().time
        )

        historialProvider.create(historial).addOnCompleteListener {
            if (it.isSuccessful ) {
                solicitudProvider.updateStatus(extraObjSolicitud.idPaciente!!, "finalizado", Date().time).addOnCompleteListener {
                    if(it.isSuccessful){
                        goToSolicitudes()
                    }
                }
            }
        }
    }
    private fun goToSolicitudes() {
        val intent = Intent(this, SolicitudesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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

    private fun validarFormulario(): Boolean {
        if (binding.etDiagnostico.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilDiagnostico,
                this.getString(R.string.not_insert_diagnostico)
            )
            return false
        }

        if (binding.etReceta.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilReceta,
                this.getString(R.string.not_insert_receta)
            )
            return false
        }

        return true
    }
    private fun iniWatchers() {
        Global.setErrorInTextInputLayout(binding.etDiagnostico, binding.tilDiagnostico)
        Global.setErrorInTextInputLayout(binding.etReceta, binding.tilReceta)
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        startActivity(Intent(this, SolicitudesActivity::class.java))
        finish()
    }
}