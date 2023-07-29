package com.javr.medicrouteapp.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.firebase.AuthProvider
import com.javr.medicrouteapp.data.network.firebase.GeoProvider
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.ui.paciente.MapPacienteActivity

class ModalBottomSolicitudForPaciente : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    private lateinit var tvNombreMedico: TextView
    private lateinit var tvNombreConsultorio: TextView
    private lateinit var tvConsultaPaciente: TextView
    private lateinit var tvValorConsulta: TextView
    private lateinit var tvHoraAtencion: TextView
    private lateinit var btnAceptarValor: Button
    private lateinit var btnCancelarValor: Button

    private val solicitudProvider = SolicitudProvider()
    private lateinit var solicitud: Solicitud

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_solicitud_for_paciente, container, false)

        tvNombreMedico = view.findViewById(R.id.tvNombreMedico)
        tvNombreConsultorio = view.findViewById(R.id.tvNombreConsultorio)
        tvConsultaPaciente = view.findViewById(R.id.tvConsultaPaciente)
        tvValorConsulta = view.findViewById(R.id.tvValorConsulta)
        tvHoraAtencion = view.findViewById(R.id.tvHoraAtencion)
        btnAceptarValor = view.findViewById(R.id.btnAceptarValor)
        btnCancelarValor = view.findViewById(R.id.btnCancelarValor)

        //  Datos de la solicitud recibida
        val dataSolicitud = arguments?.getString("solicitud")
        solicitud = Solicitud.fromJson(dataSolicitud!!)!!
        Log.d("FIRESTORE", "SolicitudesActivity/DATA ${solicitud?.toJson()}")

        //  Setear datos de la solicitud en el view
        tvNombreMedico.text = solicitud.nombreMedico
        tvNombreConsultorio.text = solicitud.nombreConsultorio
        tvConsultaPaciente.text = solicitud.consulta
        tvValorConsulta.text = solicitud.precio.toString()
        tvHoraAtencion.text = solicitud.horaAgendada

        //  Listeners
        btnAceptarValor.setOnClickListener { aceptarValorSolicitud(solicitud?.idPaciente!!) }
        btnCancelarValor.setOnClickListener { cancelarValorSolicitud(solicitud?.idPaciente!!) }

        return view
    }

    private fun aceptarValorSolicitud(idPaciente: String) {
        solicitudProvider.updateStatus(idPaciente, "aceptado").addOnCompleteListener {
//            (activity as? MapMedicoActivity)?.timer?.cancel() // Detengo el timer de la modal que se lanza en MapMedicoActivity
            dismiss()
        }
    }

    private fun cancelarValorSolicitud(idPaciente: String) {
        solicitudProvider.updateStatus(idPaciente, "cancelado").addOnCompleteListener {
//            (activity as? MapMedicoActivity)?.timer?.cancel() // Detengo el timer de la modal que se lanza en MapMedicoActivity
            goToMapPaciente()
            dismiss()
        }
    }

    private fun goToMapPaciente(){
        val intent = Intent(context, MapPacienteActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    //se ejecuta cunaod el usuario oculta el modal
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
//        (activity as? MapMedicoActivity)?.timer?.cancel() // Detengo el timer de la modal que se lanza en MapMedicoActivity
    }
}