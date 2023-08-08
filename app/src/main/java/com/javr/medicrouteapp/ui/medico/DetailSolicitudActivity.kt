package com.javr.medicrouteapp.ui.medico

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.javr.medicrouteapp.R
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

        MyToolbar().showToolbar(this, "Detalle de Atención", true)

        extraObjSolicitud = intent.getParcelableExtra<Solicitud>(EXTRA_SOLICITUD)!!
        Log.d("DETALLE PACIENTE: ", extraObjSolicitud.toString())

        initListener()

    }

    private fun initListener() {
        binding.rbCalificacion.setOnRatingBarChangeListener { ratingBar, value, fromUser ->
            calificacion = value
        }

        binding.btnFinalizarConsulta.setOnClickListener {
            diagnosticar()
            goToSolicitudes()
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
                solicitudProvider.updateStatus(extraObjSolicitud.idPaciente!!, "finalizado").addOnCompleteListener {
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
}