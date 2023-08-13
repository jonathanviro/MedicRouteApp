package com.javr.medicrouteapp.ui.paciente

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
import com.javr.medicrouteapp.databinding.ActivityHistorialPacienteBinding
import com.javr.medicrouteapp.ui.LoginActivity
import com.javr.medicrouteapp.ui.adapter.HistorialAdapter
import com.javr.medicrouteapp.ui.medico.PerfilMedicoActivity
import com.javr.medicrouteapp.utils.MyToolbar

class HistorialPacienteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialPacienteBinding
    private lateinit var historialAdapter: HistorialAdapter
    private val lstHistoriales = mutableListOf<Historial>()

    private val authProvider = AuthProvider()
    private val historialProvider = HistorialProvider()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().showToolbar(this, "Detalle", false)

        initRecyclerView()
        getAllHistorial()
    }

    private fun getAllHistorial() {
        historialProvider.getAllHistorialByPaciente(authProvider.getId()).get().addOnSuccessListener { query ->
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
                    Log.d("FIRESTORE", "HistorialPacienteActivity/ No se encontro el historial")
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
        intent.putExtra(DetailDiagnosticoActivity.EXTRA_PANTALLA_PADRE, "PACIENTE/HISTORIAL_PACIENTE_ACTIVITY")
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
        val intent = Intent(this, HistorialPacienteActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_contextual, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_one) {
            val intent = Intent(this, PerfilPacienteActivity::class.java)
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
        startActivity(Intent(this, MapPacienteActivity::class.java))
        finish()
    }
}