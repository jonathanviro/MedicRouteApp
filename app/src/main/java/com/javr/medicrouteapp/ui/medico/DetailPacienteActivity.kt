package com.javr.medicrouteapp.ui.medico

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.HistorialProvider
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.Historial
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.databinding.ActivityDetailPacienteBinding
import com.javr.medicrouteapp.toolbar.Toolbar
import com.javr.medicrouteapp.ui.LoginActivity
import com.javr.medicrouteapp.ui.adapter.HistorialAdapter
import com.javr.medicrouteapp.ui.paciente.DetailDiagnosticoActivity
import java.util.Date

class DetailPacienteActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SOLICITUD = "DetailPacienteActivity:Solicitud"
    }

    private lateinit var binding: ActivityDetailPacienteBinding
    private lateinit var extraObjSolicitud: Solicitud
    private var authProvider = AuthProvider()
    private var solicitudProvider = SolicitudProvider()
    private val historialProvider = HistorialProvider()

    private lateinit var historialAdapter: HistorialAdapter
    private val lstHistoriales = mutableListOf<Historial>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Toolbar().showToolbar(this, "Detalle de Paciente", false)

        extraObjSolicitud = intent.getParcelableExtra<Solicitud>(EXTRA_SOLICITUD)!!

        initComponents()
        initListener()
        initRecyclerView()
        getAllHistorial()
    }

    private fun initComponents() {
        binding.tvNombrePaciente.text = extraObjSolicitud.nombrePaciente
        binding.tvConsultaPaciente.text = extraObjSolicitud.consulta
        binding.tvValorConsulta.text = extraObjSolicitud.precio.toString()
        binding.tvHoraAtencion.text = extraObjSolicitud.horaAgendada

        if(extraObjSolicitud.status == "iniciado"){
            showButtonDiagnosticar()
        }
    }

    private fun initListener() {
        binding.btnIniciar.setOnClickListener { iniciarConsulta() }
        binding.btnDiagnosticar.setOnClickListener { goToDetalleAtencion() }
    }
    private fun iniciarConsulta() {
        solicitudProvider.updateStatus(extraObjSolicitud.idPaciente!!, "iniciado", Date().time).addOnCompleteListener {
            if(it.isSuccessful){
                showButtonDiagnosticar()
            }
        }
    }

    private fun getAllHistorial() {
        historialProvider.getAllHistorialByPaciente(extraObjSolicitud.idPaciente!!).get().addOnSuccessListener { query ->
            var historialList = mutableListOf<Historial>()

            if(query != null){
                if(query.documents.size > 0){

                    for (document in query.documents) {
                        val historial = document.toObject(Historial::class.java)
                        if (historial != null) {
                            historialList.add(historial)
                        }
                    }

                    Log.d("FIRESTORE", "HISTORIALES ${historialList}")


                    lstHistoriales.clear()
                    lstHistoriales.addAll(historialList)
                    historialAdapter.notifyDataSetChanged()
                }else{
                    Log.d("FIRESTORE", "DetailPacienteActivity/ No se encontro el historial")
                }
            }
        }
    }

    private fun initRecyclerView() {
        historialAdapter = HistorialAdapter(lstHistoriales) { historial ->
            onSelectedItem(historial)
        }

        binding.rvHistorialAtenciones.layoutManager = LinearLayoutManager(this)
        binding.rvHistorialAtenciones.adapter = historialAdapter
    }

    fun onSelectedItem(historial: Historial) {
        val intent = Intent(this, DetailDiagnosticoActivity::class.java)
        intent.putExtra(DetailDiagnosticoActivity.EXTRA_PANTALLA_PADRE, "MEDICO/DETAIL_PACIENTE_ACTIVITY")
        intent.putExtra(DetailDiagnosticoActivity.EXTRA_HISTORIAL, historial)
        intent.putExtra(DetailDiagnosticoActivity.EXTRA_SOLICITUD, extraObjSolicitud)
        startActivity(intent)
        finish()
    }

    private fun showButtonDiagnosticar() {
            binding.btnIniciar.visibility = View.GONE
            binding.btnDiagnosticar.visibility = View.VISIBLE
    }
    private fun goToDetalleAtencion() {
        val intent = Intent(this, DetailSolicitudActivity::class.java)
        intent.putExtra(DetailSolicitudActivity.EXTRA_SOLICITUD, extraObjSolicitud)
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