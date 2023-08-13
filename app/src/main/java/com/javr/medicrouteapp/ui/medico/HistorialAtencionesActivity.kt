package com.javr.medicrouteapp.ui.medico

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.HistorialProvider
import com.javr.medicrouteapp.data.network.model.Historial
import com.javr.medicrouteapp.databinding.ActivityHistorialAtencionesBinding
import com.javr.medicrouteapp.ui.LoginActivity
import com.javr.medicrouteapp.ui.adapter.HistorialAtencionAdapter
import com.javr.medicrouteapp.ui.paciente.DetailDiagnosticoActivity
import com.javr.medicrouteapp.ui.paciente.MapPacienteActivity
import com.javr.medicrouteapp.utils.MyToolbar

class HistorialAtencionesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialAtencionesBinding
    private lateinit var historialAtencionAdapter: HistorialAtencionAdapter
    private val lstHistorialAtenciones = mutableListOf<Historial>()

    private val authProvider = AuthProvider()
    private val historialProvider = HistorialProvider()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialAtencionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().showToolbar(this, "Historial Atenciones", false)

        initRecyclerView()
        getAllHistorial()
    }

    private fun getAllHistorial() {
        historialProvider.getAllHistorialByMedico(authProvider.getId()).get().addOnSuccessListener { query ->
            var historialAtencionesList = mutableListOf<Historial>()

            if(query != null){
                if(query.documents.size > 0){

                    for (document in query.documents) {
                        val historial = document.toObject(Historial::class.java)
                        if (historial != null) {
                            historialAtencionesList.add(historial)
                        }
                    }

                    Log.d("FIRESTORE", "HISTORIALES ${historialAtencionesList}")


                    lstHistorialAtenciones.clear()
                    lstHistorialAtenciones.addAll(historialAtencionesList)
                    historialAtencionAdapter.notifyDataSetChanged()
                }else{
                    Log.d("FIRESTORE", "HistorialAtencionesActivity/ No se encontro el historial")
                }
            }
        }
    }

    private fun initRecyclerView() {
        historialAtencionAdapter = HistorialAtencionAdapter(lstHistorialAtenciones) { historial ->
            onSelectedItem(historial)
        }

        binding.rvHistorialAtenciones.layoutManager = LinearLayoutManager(this)
        binding.rvHistorialAtenciones.adapter = historialAtencionAdapter
    }

    fun onSelectedItem(historial: Historial) {
        val intent = Intent(this, DetailDiagnosticoActivity::class.java)
        intent.putExtra(DetailDiagnosticoActivity.EXTRA_TIPO_USUARIO, "MEDICO")
        intent.putExtra(DetailDiagnosticoActivity.EXTRA_HISTORIAL, historial)
        startActivity(intent)
        finish()
    }

    private fun goToMain() {
        authProvider.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

    }

    private fun goToHistorial() {
        val intent = Intent(this, HistorialAtencionesActivity::class.java)
        startActivity(intent)
        finish()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_medico, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_one) {
            val intent = Intent(this, PerfilMedicoActivity::class.java)
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

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        startActivity(Intent(this, SolicitudesActivity::class.java))
        finish()
    }
}