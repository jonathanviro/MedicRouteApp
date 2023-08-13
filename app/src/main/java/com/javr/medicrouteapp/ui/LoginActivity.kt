package com.javr.medicrouteapp.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.core.Validator
import com.javr.medicrouteapp.data.network.firebase.AdministradorProvider
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.MedicoProvider
import com.javr.medicrouteapp.data.network.firebase.PacienteProvider
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.Administrador
import com.javr.medicrouteapp.data.network.model.Historial
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.data.network.model.Paciente
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.data.sharedpreferences.MedicoManager
import com.javr.medicrouteapp.data.sharedpreferences.PacienteManager
import com.javr.medicrouteapp.databinding.ActivityLoginBinding
import com.javr.medicrouteapp.ui.medico.EsperaMedicoActivity
import com.javr.medicrouteapp.ui.medico.SolicitudesActivity
import com.javr.medicrouteapp.ui.menus.OpcionesAdministradorActivity
import com.javr.medicrouteapp.ui.menus.TipoUsuarioActivity
import com.javr.medicrouteapp.ui.paciente.DetailDiagnosticoActivity
import com.javr.medicrouteapp.ui.paciente.MapPacienteActivity
import com.javr.medicrouteapp.ui.paciente.MapRutaConsultorioActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var screenSplash: SplashScreen
    private lateinit var binding: ActivityLoginBinding
    private val startTime = System.currentTimeMillis()
    private val authProvider = AuthProvider()
    private val administradorProvider = AdministradorProvider()
    private val pacienteProvider = PacienteProvider()
    private val medicoProvider = MedicoProvider()
    private val solicitudProvider = SolicitudProvider()
    private lateinit var dialogoCarga: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        screenSplash.setKeepOnScreenCondition{
            true
        }

        initComponents()
        initListener()
    }

    private fun initComponents() {
        dialogoCarga = Global.dialogoCarga(this, "Accediendo")

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
            dialogoCarga.show()
            authProvider.login(binding.etCorreo.text.toString(), binding.etPassword.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful) {
                        searchUsuario()
                    } else {
                        dialogoCarga.dismiss()
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

                                when(solicitud?.status){
                                    "aceptado", "valorado", "iniciado" -> goToMapRutaConsultorio()
                                    "creado" -> removeSolicitud()
                                    "finalizado" -> goToDetailDiagnostico()
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
                            if(medico.status == "activo"){
                                dialogoCarga.dismiss()
                                MedicoManager.guardarMedico(this@LoginActivity, medico)
                                Log.d("FIRESTORE", "DATOS MEDICO: ${medico}")
                                goToVistaMedico()
                            }else{
                                dialogoCarga.dismiss()
                                goToVistaEsperaMedico()
                            }

                        }else{
                            dialogoCarga.dismiss()
                            Toast.makeText(this@LoginActivity, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        administradorProvider.getAdministrador().get().addOnSuccessListener { document ->
                            if(document != null && document.exists()){
                                val administrador = document.toObject(Administrador::class.java)
                                if(administrador != null){
                                    dialogoCarga.dismiss()
                                    Log.d("FIRESTORE", "DATOS ADMINISTRADOR: ${administrador}")
                                    goToVistaAdministrador()
                                }else{
                                    dialogoCarga.dismiss()
                                    Toast.makeText(this@LoginActivity, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                                }
                            }
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
        val intent = Intent(this, MapPacienteActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun goToVistaMedico() {
        val intent = Intent(this, SolicitudesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun goToVistaEsperaMedico() {
        val intent = Intent(this, EsperaMedicoActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun goToVistaAdministrador() {
        val intent = Intent(this, OpcionesAdministradorActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun goToMapRutaConsultorio(){
        val intent = Intent(this, MapRutaConsultorioActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun goToDetailDiagnostico() {
        val intent = Intent(this, DetailDiagnosticoActivity::class.java)
        intent.putExtra(DetailDiagnosticoActivity.EXTRA_PANTALLA_PADRE, "MEDICO/LOGIN_ACTIVITY")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

    private fun removeSolicitud() {
        solicitudProvider.getEstadoSolicitud().get().addOnSuccessListener { document ->
            if(document.exists()){
                solicitudProvider.remove()
                goToVistaPaciente()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (authProvider.existSession()){
            searchUsuario()
        }else{
            screenSplash.setKeepOnScreenCondition{
                val currentTime = System.currentTimeMillis()
                currentTime - startTime < 1500
            }
        }
    }
}