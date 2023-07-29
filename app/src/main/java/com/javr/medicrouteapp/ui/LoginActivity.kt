package com.javr.medicrouteapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.core.Validator
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.MedicoProvider
import com.javr.medicrouteapp.data.network.firebase.PacienteProvider
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.data.network.model.Paciente
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.data.sharedpreferences.MedicoManager
import com.javr.medicrouteapp.data.sharedpreferences.PacienteManager
import com.javr.medicrouteapp.databinding.ActivityLoginBinding
import com.javr.medicrouteapp.ui.medico.SolicitudesActivity
import com.javr.medicrouteapp.ui.menus.TipoUsuarioActivity
import com.javr.medicrouteapp.ui.paciente.MapPacienteActivity
import com.javr.medicrouteapp.ui.paciente.MapRutaConsultorioActivity

//import com.javr.medicrouteapp.ui.map.MapPacienteActivity
//import com.javr.medicrouteapp.ui.map.MapPacienteActivity
//import com.javr.medicrouteapp.ui.menus.TipoUsuarioActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    val authProvider = AuthProvider()
    val pacienteProvider = PacienteProvider()
    val medicoProvider = MedicoProvider()
    val solicitudProvider = SolicitudProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        initListener()
    }

    private fun initComponents() {
        //Watcher Errores
        Global.setErrorInTextInputLayout(binding.etCorreo, binding.tilCorreo)
        Global.setErrorInTextInputLayout(binding.etPassword, binding.tilPassword)
    }

    private fun initListener() {
        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.btnSignup.setOnClickListener {
            goToSignup()
        }
    }

    private fun login() {
        if (validarFormulario()) {
            authProvider.login(binding.etCorreo.text.toString(), binding.etPassword.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        searchUsuario()
                    } else {
                        Toast.makeText(this, "Error en inicio de sesión", Toast.LENGTH_SHORT).show()
                        Log.d("FIREBASE", "ERROR ${it.exception.toString()}")
                    }
                }
        }
    }

    private fun searchUsuario() {
        pacienteProvider.getPaciente().get().addOnSuccessListener { document ->
            if(document != null && document.exists()){
                val paciente = document.toObject(Paciente::class.java)
                if(paciente != null){
                    PacienteManager.guardarPaciente(this@LoginActivity, paciente)
                    Log.d("FIRESTORE", "DATOS PACIENTE: ${paciente}")


                    solicitudProvider.getSolicitudByPaciente().get().addOnSuccessListener { query ->
                        Log.d("AQUI", "getSolicitud ${query}")
                        if (query != null) {
                            if (query.size() > 0) {
                                var solicitud = query.documents[0].toObject(Solicitud::class.java)
                                Log.d("LOGIN SOLICITUD", "getSolicitud ${solicitud}")

                                if(solicitud?.status == "aceptado"){
                                    goToMapRutaConsultorio()
                                }
                            }
                        }
                    }

                    goToVistaPaciente()
                }else{
                    Toast.makeText(this@LoginActivity, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                }
            }else{
                medicoProvider.getMedico().get().addOnSuccessListener { document ->
                    if(document != null && document.exists()){
                        val medico = document.toObject(Medico::class.java)
                        if(medico != null){
                            MedicoManager.guardarMedico(this@LoginActivity, medico)
                            Log.d("FIRESTORE", "DATOS MEDICO: ${medico}")
                            goToVistaMedico()
                        }else{
                            Toast.makeText(this@LoginActivity, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun goToSignup() {
        val intent = Intent(this, TipoUsuarioActivity::class.java)
        startActivity(intent)
    }

    private fun goToVistaPaciente() {
        Toast.makeText(this@LoginActivity, "IR A VISTA PACIENTE", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MapPacienteActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK//Convertir activity en la activity principal. Eliminando el historial de pantallas
        startActivity(intent)
    }

    private fun goToVistaMedico() {
        Toast.makeText(this@LoginActivity, "IR A VISTA MEDICO", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SolicitudesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK//Convertir activity en la activity principal. Eliminando el historial de pantallas
        startActivity(intent)
    }

    private fun goToMapRutaConsultorio(){
        val intent = Intent(this, MapRutaConsultorioActivity::class.java)
        startActivity(intent)
    }



    private fun validarFormulario(): Boolean {
        if (binding.etCorreo.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilCorreo,
                this.getString(R.string.not_insert_email)
            )
            return false
        } else {
            if (!Validator.isValidEmail(binding.etCorreo.text.toString())) {
                Global.setErrorInTextInputLayout(
                    binding.tilCorreo,
                    this.getString(R.string.invalid_email)
                )
                return false
            }
        }

        if (binding.etPassword.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilPassword,
                this.getString(R.string.not_insert_password)
            )
            return false
        }

        if (Validator.isValidPassword(binding.etPassword.text.toString())) {
            Global.setErrorInTextInputLayout(
                binding.tilPassword,
                this.getString(R.string.invalid_password)
            )
            return false
        }

        return true
    }

    override fun onStart() {
        super.onStart()

        if (authProvider.existSession()){
            searchUsuario()
        }
    }
}