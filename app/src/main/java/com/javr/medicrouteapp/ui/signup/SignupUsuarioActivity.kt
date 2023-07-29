package com.javr.medicrouteapp.ui.signup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.core.Validator
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.MedicoProvider
import com.javr.medicrouteapp.data.network.firebase.PacienteProvider
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.data.network.model.Paciente
import com.javr.medicrouteapp.databinding.ActivitySignupUsuarioBinding
import com.javr.medicrouteapp.toolbar.Toolbar
import com.javr.medicrouteapp.ui.medico.SolicitudesActivity

class SignupUsuarioActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TIPO_USUARIO = "SignupUsuarioActivity:TipoUsuario"
        const val EXTRA_RAZON_SOCIAL = "SignupUsuarioActivity:RazonSocial"
        const val EXTRA_RUC = "SignupUsuarioActivity:Ruc"
        const val EXTRA_DIRECCION = "SignupUsuarioActivity:Direccion"
        const val EXTRA_REGISTRO_SANITARIO = "SignupUsuarioActivity:RegistroSanitario"
        const val EXTRA_CONSULTORIO_LAT = "SignupUsuarioActivity:ConsultorioLat"
        const val EXTRA_CONSULTORIO_LNG = "SignupUsuarioActivity:ConsultorioLng"
    }

    private lateinit var binding: ActivitySignupUsuarioBinding
    private val authProvider = AuthProvider()
    private val pacienteProvider = PacienteProvider()
    private val medicoProvider = MedicoProvider()
    private var extraTipoUsuario: String? = null
    private var extraRazonSocial: String? = null
    private var extraRuc: String? = null
    private var extraDireccion: String? = null
    private var extraRegistroSanitario: String? = null
    private var extraConsultorioLat: Double? = null
    private var extraConsultorioLng: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        initListener()
    }

    private fun initComponents() {
        iniWatchers()

        extraTipoUsuario = intent.getStringExtra(EXTRA_TIPO_USUARIO)
        extraRazonSocial = intent.getStringExtra(EXTRA_RAZON_SOCIAL)
        extraRuc = intent.getStringExtra(EXTRA_RUC)
        extraDireccion = intent.getStringExtra(EXTRA_DIRECCION)
        extraRegistroSanitario = intent.getStringExtra(EXTRA_REGISTRO_SANITARIO)
        extraConsultorioLat = intent.getDoubleExtra(EXTRA_CONSULTORIO_LAT, 0.0)
        extraConsultorioLng = intent.getDoubleExtra(EXTRA_CONSULTORIO_LNG, 0.0)

        if (extraTipoUsuario.equals("PACIENTE")) {
            Toolbar().showToolbar(this, "Registro de Paciente", true)
            binding.llUploadPdf.visibility = View.GONE
            binding.llDownloadPdf.visibility = View.GONE
        } else {
            Toolbar().showToolbar(this, "Registro de Médico", true)
        }
    }

    private fun iniWatchers() {
        //Watcher Errores
        Global.setErrorInTextInputLayout(binding.etNombres, binding.tilNombres)
        Global.setErrorInTextInputLayout(binding.etApellidos, binding.tilApellidos)
        Global.setErrorInTextInputLayout(binding.etCedula, binding.tilCedula)
        Global.setErrorInTextInputLayout(binding.etCorreo, binding.tilCorreo)
        Global.setErrorInTextInputLayout(binding.etTelefono, binding.tilTelefono)
        Global.setErrorInTextInputLayout(binding.etPassword, binding.tilPassword)
        Global.setErrorInTextInputLayout(binding.etConfirmPassword, binding.tilConfirmPassword)
    }

    private fun initListener() {
        binding.ivFotoPerfil.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.ivUploadPdf.setOnClickListener {
            selectedPdf()
        }

        binding.btnSignup.setOnClickListener {
            registrerUsuario()
        }
    }

    private fun registrerUsuario() {
        if (validarFormulario()) {
            authProvider.checkEmailExists(binding.etCorreo.text.toString()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        if (signInMethods != null && signInMethods.isNotEmpty()) {
                            Toast.makeText(this@SignupUsuarioActivity, "Correo ingresado ya se encuentra en uso", Toast.LENGTH_LONG).show()
                        } else {
                            authProvider.registrer(
                                binding.etCorreo.text.toString(),
                                binding.etPassword.text.toString()
                            ).addOnCompleteListener {
                                if (it.isSuccessful){
                                    if (extraTipoUsuario.equals("PACIENTE")) {
                                        val paciente = Paciente(
                                            id = authProvider.getId(),
                                            nombres = binding.etNombres.text.toString(),
                                            apellidos = binding.etApellidos.text.toString(),
                                            cedula = binding.etCedula.text.toString(),
                                            email = binding.etCorreo.text.toString(),
                                            telefono = binding.etTelefono.text.toString(),
                                            sexo = binding.tvSexo.text.toString(),
                                            )

                                        pacienteProvider.create(paciente).addOnCompleteListener {
                                            if(it.isSuccessful){
                                                Toast.makeText(this@SignupUsuarioActivity, "REGISTRO EXITOSO", Toast.LENGTH_LONG).show()
                                                goToVistaPaciente()
                                            }else{
                                                Log.e("FIRESTORE", "ERROR: Error almacenando los datos del paciente. ${it.exception.toString()}")
                                            }
                                        }
                                    }else{
                                        val medico = Medico(
                                            id = authProvider.getId(),
                                            nombres = binding.etNombres.text.toString(),
                                            apellidos = binding.etApellidos.text.toString(),
                                            cedula = binding.etCedula.text.toString(),
                                            email = binding.etCorreo.text.toString(),
                                            telefono = binding.etTelefono.text.toString(),
                                            sexo = binding.tvSexo.text.toString(),
                                            razonSocial = extraRazonSocial,
                                            ruc = extraRuc,
                                            registroSanitario = extraRegistroSanitario,
                                            consultorioLat = extraConsultorioLat,
                                            consultorioLng = extraConsultorioLng)

                                        medicoProvider.create(medico).addOnCompleteListener {
                                            if(it.isSuccessful){
                                                Toast.makeText(this@SignupUsuarioActivity, "REGISTRO EXITOSO", Toast.LENGTH_LONG).show()
                                                goToVistaMedico()
                                            }else{
                                                Log.e("FIRESTORE", "ERROR: Error almacenando los datos del medico. ${it.exception.toString()}")
                                            }
                                        }
                                    }

                                }else{
                                    Log.e("FIRESTORE", "Registro fallido ${it.exception.toString()}")
                                }
                            }
                        }
                    } else {
                        val exception = task.exception
                        Log.e("FIRESTORE", "Error al verificar el correo electrónico: $exception")
                    }
                }
        }
    }

    private fun goToVistaPaciente() {
        Toast.makeText(this@SignupUsuarioActivity, "IR A VISTA PACIENTE", Toast.LENGTH_SHORT).show()
//        val intent = Intent(this, MapPacienteActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK//Convertir activity en la activity principal. Eliminando el historial de pantallas
    }

    private fun goToVistaMedico() {
        Toast.makeText(this@SignupUsuarioActivity, "IR A VISTA MEDICO", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SolicitudesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK//Convertir activity en la activity principal. Eliminando el historial de pantallas
    }

    private fun validarFormulario(): Boolean {
        if (binding.etNombres.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilNombres,
                this.getString(R.string.not_insert_names)
            )
            return false
        }

        if (binding.etApellidos.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilApellidos,
                this.getString(R.string.not_insert_lastnames)
            )
            return false
        }

        if (binding.etCedula.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilCedula,
                this.getString(R.string.not_insert_passport)
            )
            return false
        } else {
            if (!Validator.isValidCedula(binding.etCedula.text.toString())) {
                Global.setErrorInTextInputLayout(
                    binding.tilCedula,
                    this.getString(R.string.invalid_passport)
                )
                return false
            }
        }

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

        if (binding.etTelefono.text.toString().isNullOrEmpty()) {
            Global.setErrorInTextInputLayout(
                binding.tilTelefono,
                this.getString(R.string.not_insert_phone)
            )
            return false
        } else {
            if (binding.etTelefono.text.toString().length < 10) {
                Global.setErrorInTextInputLayout(
                    binding.tilTelefono,
                    this.getString(R.string.invalid_phone)
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

        if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
            Global.setErrorInTextInputLayout(
                binding.tilConfirmPassword,
                this.getString(R.string.invalid_confirm_password)
            )
            return false
        }

        return true
    }

    private fun selectedPdf() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }

        openPdfFile.launch(intent)
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.ivFotoPerfil.setImageURI(uri)
//                Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "NO HA SELECCIONADO FOTO", Toast.LENGTH_SHORT).show()
            }
        }

    private val openPdfFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let { pdfUri ->
                    val cursor = this.contentResolver.query(uri, null, null, null, null)
                    cursor?.let {
                        if (it.moveToFirst()) {
                            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val fileName =
                                if (nameIndex >= 0) it.getString(nameIndex) else uri.lastPathSegment

                            binding.tvNombrePdf.text = fileName
                        }
                        it.close()
                    }
                }
            }
        }
}