package com.javr.medicrouteapp.ui.signup

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.javr.medicrouteapp.ui.medico.EsperaMedicoActivity
import com.javr.medicrouteapp.ui.medico.SolicitudesActivity
import com.javr.medicrouteapp.ui.paciente.MapPacienteActivity

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
    private var uriImagen: Uri? = null
    private var uriPdf: Uri? = null

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
        if(uriImagen != null) {
            if (validarFormulario()) {
                var flagContinuar = false
                if (extraTipoUsuario.equals("MEDICO")) {
                    if(uriPdf != null) {
                        flagContinuar = true
                    }else{
                        Toast.makeText(this@SignupUsuarioActivity, "PDF OBLIGATORIO", Toast.LENGTH_LONG).show()
                    }
                }else{
                    flagContinuar = true
                }

                if(flagContinuar){
                    val dialogoCarga = Global.dialogoCarga(this, "Creando Usuario")
                    dialogoCarga.show()

                    authProvider.checkEmailExists(binding.etCorreo.text.toString()).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val signInMethods = task.result?.signInMethods
                            if (signInMethods != null && signInMethods.isNotEmpty()) {
                                dialogoCarga.dismiss()
                                Toast.makeText(this@SignupUsuarioActivity, "Correo ingresado ya se encuentra en uso", Toast.LENGTH_LONG).show()
                            } else {
                                authProvider.registrer(binding.etCorreo.text.toString(), binding.etPassword.text.toString()).addOnCompleteListener {
                                    if (it.isSuccessful){
                                        if (extraTipoUsuario.equals("PACIENTE")) {
                                            pacienteProvider.uploadImagen(authProvider.getId(), uriImagen!!).addOnSuccessListener {taskSnapshot ->
                                                pacienteProvider.getImagenUrl(authProvider.getId()).addOnSuccessListener {url ->
                                                    val imageUrl = url.toString()
                                                    Log.d("STORAGE", "URL: $url")

                                                    val paciente = Paciente(
                                                        id = authProvider.getId(),
                                                        nombres = binding.etNombres.text.toString(),
                                                        apellidos = binding.etApellidos.text.toString(),
                                                        cedula = binding.etCedula.text.toString(),
                                                        telefono = binding.etTelefono.text.toString(),
                                                        sexo = binding.tvSexo.text.toString(),
                                                        imagenUrl = imageUrl
                                                    )

                                                    pacienteProvider.create(paciente).addOnCompleteListener {
                                                        if(it.isSuccessful){
                                                            dialogoCarga.dismiss()
                                                            Toast.makeText(this@SignupUsuarioActivity, "REGISTRO EXITOSO", Toast.LENGTH_LONG).show()
                                                            goToVistaPaciente()
                                                        }else{
                                                            dialogoCarga.dismiss()
                                                            Log.e("FIRESTORE", "ERROR: Error almacenando los datos del paciente. ${it.exception.toString()}")
                                                        }
                                                    }
                                                }
                                            }
                                        }else{
                                            medicoProvider.uploadImagen(authProvider.getId(), uriImagen!!).addOnSuccessListener {taskSnapshot ->
                                                medicoProvider.getImagenUrl(authProvider.getId()).addOnSuccessListener {urlImage ->
                                                    val imageUrl = urlImage.toString()
                                                    Log.d("STORAGE", "URL IMAGE: $urlImage")

                                                    medicoProvider.uploadPdf(authProvider.getId(), uriPdf!!).addOnSuccessListener {taskSnapshotPdf ->
                                                        medicoProvider.getPdfUrl(authProvider.getId()).addOnSuccessListener { urlPdf ->
                                                            val pfUrl = urlPdf.toString()
                                                            Log.d("STORAGE", "URL PDF: $pfUrl")

                                                            val medico = Medico(
                                                                id = authProvider.getId(),
                                                                nombres = binding.etNombres.text.toString(),
                                                                apellidos = binding.etApellidos.text.toString(),
                                                                cedula = binding.etCedula.text.toString(),
                                                                telefono = binding.etTelefono.text.toString(),
                                                                sexo = binding.tvSexo.text.toString(),
                                                                razonSocial = extraRazonSocial,
                                                                ruc = extraRuc,
                                                                registroSanitario = extraRegistroSanitario,
                                                                consultorioLat = extraConsultorioLat,
                                                                consultorioLng = extraConsultorioLng,
                                                                imagenUrl = imageUrl,
                                                                pdfUrl = pfUrl,
                                                                status = "pendiente")

                                                            medicoProvider.create(medico).addOnCompleteListener {
                                                                if(it.isSuccessful){
                                                                    Toast.makeText(this@SignupUsuarioActivity, "REGISTRO EXITOSO", Toast.LENGTH_LONG).show()
                                                                    if(medico.status == "activo"){
                                                                        dialogoCarga.dismiss()
                                                                        goToVistaMedico()
                                                                    }else{
                                                                        dialogoCarga.dismiss()
                                                                        goToVistaEsperaMedico()
                                                                    }
                                                                }else{
                                                                    dialogoCarga.dismiss()
                                                                    Log.e("FIRESTORE", "ERROR: Error almacenando los datos del medico. ${it.exception.toString()}")
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }else{
                                        dialogoCarga.dismiss()
                                        Log.e("FIRESTORE", "Registro fallido ${it.exception.toString()}")
                                    }
                                }
                            }
                        } else {
                            dialogoCarga.dismiss()
                            val exception = task.exception
                            Log.e("FIRESTORE", "Error al verificar el correo electrónico: $exception")
                        }
                    }
                }
            }
        }else{
            Toast.makeText(this@SignupUsuarioActivity, "FOTO DE PERFIL OBLIGATORIA", Toast.LENGTH_LONG).show()
        }
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

        pickPdfFile.launch(intent)
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.ivFotoPerfil.setImageURI(uri)
                uriImagen = uri
            } else {
                Toast.makeText(this, "NO HA SELECCIONADO FOTO", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickPdfFile =
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
                            uriPdf = uri
                        }
                        it.close()
                    }
                }
            }
        }
}