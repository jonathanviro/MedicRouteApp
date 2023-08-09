package com.javr.medicrouteapp.ui.paciente

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.core.Validator
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.PacienteProvider
import com.javr.medicrouteapp.data.network.model.Paciente
import com.javr.medicrouteapp.data.sharedpreferences.PacienteManager
import com.javr.medicrouteapp.databinding.ActivityPerfilPacienteBinding
import com.javr.medicrouteapp.toolbar.Toolbar
import com.javr.medicrouteapp.ui.LoginActivity

class PerfilPacienteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilPacienteBinding
    private var shpPaciente: Paciente? = null
    private val authProvider = AuthProvider()
    private val pacienteProvider = PacienteProvider()
    private var uriImagen: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Toolbar().showToolbar(this, "Mi Perfil", false)

        shpPaciente = PacienteManager.obtenerPaciente(this)

        initComponents()
        initListener()
    }

    private fun initComponents() {
        iniWatchers()
        binding.etNombres.setText(shpPaciente?.nombres)
        binding.etApellidos.setText(shpPaciente?.apellidos)
        binding.etCedula.setText(shpPaciente?.cedula)
        binding.etTelefono.setText(shpPaciente?.telefono)
        binding.tvSexo.setText(shpPaciente?.sexo)

        if(shpPaciente?.imagenUrl != null){
            Glide.with(this).load(shpPaciente?.imagenUrl).into(binding.ivFotoPerfil)
        }
    }

    private fun iniWatchers() {
        //Watcher Errores
        Global.setErrorInTextInputLayout(binding.etNombres, binding.tilNombres)
        Global.setErrorInTextInputLayout(binding.etApellidos, binding.tilApellidos)
        Global.setErrorInTextInputLayout(binding.etCedula, binding.tilCedula)
        Global.setErrorInTextInputLayout(binding.etTelefono, binding.tilTelefono)
    }

    private fun initListener() {
        binding.ivFotoPerfil.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnActualizarDatosPaciente.setOnClickListener {
            actualizarUsuario()
        }
    }

    private fun actualizarUsuario() {
        if (validarFormulario()) {
            val dialogoCarga = Global.dialogoCarga(this, "Actualizando Datos")
            dialogoCarga.show()

            val paciente = Paciente(
                id = authProvider.getId(),
                nombres = binding.etNombres.text.toString(),
                apellidos = binding.etApellidos.text.toString(),
                cedula = binding.etCedula.text.toString(),
                telefono = binding.etTelefono.text.toString(),
                sexo = binding.tvSexo.text.toString(),
                imagenUrl = shpPaciente?.imagenUrl)

            if(uriImagen != null){
                pacienteProvider.uploadImagen(authProvider.getId(), uriImagen!!).addOnSuccessListener {taskSnapshot ->
                    pacienteProvider.getImagenUrl(authProvider.getId()).addOnSuccessListener {url ->
                        val imageUrl = url.toString()
                        paciente.imagenUrl = imageUrl
                        Log.d("STORAGE", "URL IMAGE: $imageUrl")

                        pacienteProvider.update(paciente).addOnCompleteListener {
                            if(it.isSuccessful){
                                dialogoCarga.dismiss()
                                PacienteManager.guardarPaciente(this, paciente)
                                Toast.makeText(this@PerfilPacienteActivity, "Datos actualizados", Toast.LENGTH_LONG).show()
                            }else{
                                dialogoCarga.dismiss()
                                Toast.makeText(this@PerfilPacienteActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }else{
                pacienteProvider.update(paciente).addOnCompleteListener {
                    if(it.isSuccessful){
                        dialogoCarga.dismiss()
                        PacienteManager.guardarPaciente(this, paciente)
                        Toast.makeText(this@PerfilPacienteActivity, "Datos actualizados", Toast.LENGTH_LONG).show()
                    }else{
                        dialogoCarga.dismiss()
                        Toast.makeText(this@PerfilPacienteActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
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

        return true
    }

    private fun goToMain() {
        authProvider.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun goToHistorial() {
        val intent = Intent(this, HistorialPacienteActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_paciente, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_one) {
            val intent = Intent(this, PerfilPacienteActivity::class.java)
            startActivity(intent)
        }

        if (item.itemId == R.id.option_two) {
            goToHistorial()
        }

        if (item.itemId == R.id.option_three) {
            goToMain()
        }

        return super.onOptionsItemSelected(item)
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

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        startActivity(Intent(this, MapPacienteActivity::class.java))
        finish()
    }
}