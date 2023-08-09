package com.javr.medicrouteapp.ui.paciente

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.HistorialProvider
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.Historial
import com.javr.medicrouteapp.databinding.ActivityDetailDiagnosticoBinding
import com.javr.medicrouteapp.ui.LoginActivity
import com.javr.medicrouteapp.ui.medico.HistorialAtencionesActivity
import com.javr.medicrouteapp.ui.medico.PerfilMedicoActivity
import com.javr.medicrouteapp.utils.MyToolbar

class DetailDiagnosticoActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TIPO_USUARIO = "DetailDiagnosticoActivity:TipoUsuario"
        const val EXTRA_HISTORIAL = "DetailDiagnosticoActivity:Historial"
    }

    private lateinit var binding: ActivityDetailDiagnosticoBinding
    private lateinit var extraTipoUsuario: String
    private lateinit var extraObjHistorial: Historial
    private var historial: Historial? = null
    private var calificacion = 0f
    private var authProvider = AuthProvider()
    private var solicitudProvider = SolicitudProvider()
    private var historialProvider =  HistorialProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailDiagnosticoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().showToolbar(this, "Diagnóstico", false)

        initListener()

        extraObjHistorial = intent.getParcelableExtra<Historial>(DetailDiagnosticoActivity.EXTRA_HISTORIAL)!!
        if(extraObjHistorial.idPaciente != null){
            initExtras()
        }else{
            getHistorial()
        }



    }

    private fun initExtras() {
        extraTipoUsuario = intent.getStringExtra(DetailDiagnosticoActivity.EXTRA_TIPO_USUARIO)!!
        extraObjHistorial = intent.getParcelableExtra<Historial>(DetailDiagnosticoActivity.EXTRA_HISTORIAL)!!

        val esMedico = extraTipoUsuario == "MEDICO"

        if (esMedico && extraObjHistorial.idMedico != null) {
            binding.tvNombreMedico.text = "${extraObjHistorial.nombrePaciente}"
            binding.tvTituloCalificacion.text = "Calificación dada al Paciente"
            binding.rbCalificacionFirebase.rating = extraObjHistorial.calificacionParaPaciente ?: 0f
        } else if (!esMedico && extraObjHistorial.idPaciente != null) {
            binding.tvNombreMedico.text = "Dr. ${extraObjHistorial.nombreMedico}"
            binding.tvTituloCalificacion.text = "Calificación dada al Médico"
            binding.rbCalificacionFirebase.rating = extraObjHistorial.calificacionParaMedico ?: 0f
        }

        binding.tvConsulta.text = extraObjHistorial.consulta
        binding.tvDiagnostico.text = extraObjHistorial.diagnostico
        binding.tvReceta.text = extraObjHistorial.receta
        binding.tvFechaAtencion.text = Global.timestampToDate(extraObjHistorial.timestamp!!)

        binding.btnCalificarMedico.visibility = View.GONE
        binding.rbCalificacion.visibility = View.GONE
        binding.rbCalificacionFirebase.visibility = View.VISIBLE
    }

    private fun initDetalle() {
        binding.tvNombreMedico.text = "Dr. ${historial?.nombreMedico}"
        binding.tvConsulta.text = historial?.consulta
        binding.tvDiagnostico.text = historial?.diagnostico
        binding.tvReceta.text = historial?.receta
    }

    private fun initListener() {
        binding.rbCalificacion.setOnRatingBarChangeListener { ratingBar, value, fromUser ->
            calificacion = value
        }

        binding.btnCalificarMedico.setOnClickListener {updateCalification(historial?.id!!)}
    }

    private fun getHistorial() {
        historialProvider.getLastHistorialByPaciente().get().addOnSuccessListener { query ->
            if(query != null){
                if(query.documents.size > 0){
                    historial = query.documents[0].toObject(Historial::class.java)
                    Log.d("FIRESTORE", "HISTORIAL ${historial?.toJson()}")

                    historial?.id = query.documents[0].id

                    initDetalle()
                }else{
                    Log.d("FIRESTORE", "DetailDiagnosticoActivity/ No se encontro el historial")
//                    Toast.makeText(this, "No se encontro el historial", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateCalification(idDocument: String){
        historialProvider.updateCalificationToMedico(idDocument, calificacion).addOnCompleteListener {
            if(it.isSuccessful){
                solicitudProvider.remove()
                goToMapPaciente()
            }else{
                Toast.makeText(this, "Error al enviar la calificación", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToMain() {
        authProvider.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun goToMapPaciente(){
        val intent = Intent(this, MapPacienteActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToHistorialPaciente() {
        val intent = Intent(this, HistorialPacienteActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToHistorialMedico() {
        val intent = Intent(this, HistorialAtencionesActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(extraObjHistorial.idPaciente != null){
            menuInflater.inflate(R.menu.menu_medico, menu)

        }else{
            menuInflater.inflate(R.menu.menu_paciente, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(extraObjHistorial.idPaciente != null){
            if (item.itemId == R.id.option_one) {
                val intent = Intent(this, PerfilMedicoActivity::class.java)
                startActivity(intent)
                finish()
            }

            if (item.itemId == R.id.option_two) {
                goToHistorialMedico()
            }
        }else{
            if (item.itemId == R.id.option_one) {
                val intent = Intent(this, PerfilPacienteActivity::class.java)
                startActivity(intent)
                finish()
            }

            if (item.itemId == R.id.option_two) {
                goToHistorialPaciente()
            }
        }

        if (item.itemId == R.id.option_three) {
            goToMain()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        if(extraObjHistorial.idPaciente != null){
            startActivity(Intent(this, HistorialAtencionesActivity::class.java))

        }else{
            startActivity(Intent(this, HistorialPacienteActivity::class.java))
        }

        finish()
    }
}