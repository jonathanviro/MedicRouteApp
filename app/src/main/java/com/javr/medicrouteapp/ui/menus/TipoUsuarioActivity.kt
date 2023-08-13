package com.javr.medicrouteapp.ui.menus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.javr.medicrouteapp.databinding.ActivityTipoUsuarioBinding
import com.javr.medicrouteapp.toolbar.Toolbar
import com.javr.medicrouteapp.ui.signup.SignupConsultorioActivity
import com.javr.medicrouteapp.ui.signup.SignupUsuarioActivity

class TipoUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTipoUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipoUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        initListener()
    }

    private fun initComponents() {
        Toolbar().showToolbar(this, "Tipo de Usuario", true)
    }

    private fun initListener() {
        binding.cvPaciente.setOnClickListener {
            val intent = Intent(this, SignupUsuarioActivity::class.java)
            intent.putExtra(SignupUsuarioActivity.EXTRA_TIPO_USUARIO, "PACIENTE")
            startActivity(intent)
        }

        binding.cvMedico.setOnClickListener {
            startActivity(Intent(this, SignupConsultorioActivity::class.java))
        }


    }
}