package com.javr.medicrouteapp.ui.medico

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.databinding.ActivityDetailPacienteBinding

class DetailPacienteActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PACIENTE = "DetailPacienteActivity:Paciente"
    }

    private lateinit var binding: ActivityDetailPacienteBinding
    private lateinit var pacienteSelected: Solicitud

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pacienteSelected = intent.getParcelableExtra<Solicitud>(EXTRA_PACIENTE)!!

//        initViewEmpresa(pacienteSelected)
    }

//    private fun initViewEmpresa(paciente: Solicitud) {
//        binding.nombreEmpresa.setText(paciente.nombrePaciente)
//        binding.correoEmpresa.setText(paciente.correo)
//        binding.telefonoEmpresa.setText(paciente.telefono)
//        binding.encargadoEmpresa.setText(paciente.personaEncargada)
//
//        popupEmpresa(paciente)
//    }
}