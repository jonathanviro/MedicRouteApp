package com.javr.medicrouteapp.ui.menus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.databinding.ActivityOpcionesAdministradorBinding
import com.javr.medicrouteapp.databinding.ActivityTipoUsuarioBinding
import com.javr.medicrouteapp.toolbar.Toolbar
import com.javr.medicrouteapp.ui.administrador.MedicosActivosActivity
import com.javr.medicrouteapp.ui.administrador.MedicosPendientesActivity
import com.javr.medicrouteapp.ui.signup.SignupConsultorioActivity
import com.javr.medicrouteapp.ui.signup.SignupUsuarioActivity
import layout.fragments.ModalBottomReportes
import layout.fragments.ModalBottomSolicitudForMedico

class OpcionesAdministradorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpcionesAdministradorBinding
    private val modalSolicitud = ModalBottomReportes()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcionesAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        initListener()
    }

    private fun initComponents() {
        Toolbar().showToolbar(this, "Opciones Administrador", false)
    }

    private fun initListener() {
        binding.cvMedicosPendientes.setOnClickListener {
            val intent = Intent(this, MedicosPendientesActivity::class.java)
            startActivity(intent)
        }

        binding.cvMedicosActivos.setOnClickListener {
            val intent = Intent(this, MedicosActivosActivity::class.java)
            startActivity(intent)
        }

        binding.cvReportes.setOnClickListener {
            showModalSolicitud()
        }
    }

    private fun showModalSolicitud() {
        val bundle = Bundle()
        modalSolicitud.arguments = bundle
        modalSolicitud.show(supportFragmentManager, ModalBottomReportes.TAG)
    }
}