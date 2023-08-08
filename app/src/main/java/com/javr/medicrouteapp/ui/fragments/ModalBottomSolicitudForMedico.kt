package layout.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.firebase.SolicitudProvider
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.data.sharedpreferences.MedicoManager
import com.javr.medicrouteapp.ui.medico.SolicitudesActivity

class ModalBottomSolicitudForMedico : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    private var shpMedico: Medico? = null
    private lateinit var tvPaciente: TextView
    private lateinit var tvConsulta: TextView
    private lateinit var etValorConsulta: TextInputEditText
    private lateinit var etHoraAtencion: TextInputEditText
    private lateinit var btnEnviar: Button
    private lateinit var btnCancelar: Button

    private val solicitudProvider = SolicitudProvider()
    private lateinit var solicitud: Solicitud

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_solicitud_for_medico, container, false)

        shpMedico = MedicoManager.obtenerMedico(requireContext())

        tvPaciente = view.findViewById(R.id.tvPaciente)
        tvConsulta = view.findViewById(R.id.tvConsulta)
        etValorConsulta = view.findViewById(R.id.etValorConsulta)
        etHoraAtencion = view.findViewById(R.id.etHoraAtencion)
        btnEnviar = view.findViewById(R.id.btnEnviar)
        btnCancelar = view.findViewById(R.id.btnCancelar)

        //  Datos de la solicitud recibida
        val dataSolicitud = arguments?.getString("solicitud")
        solicitud = Solicitud.fromJson(dataSolicitud!!)!!
        Log.d("FIRESTORE", "SolicitudesActivity/DATA ${solicitud?.toJson()}")

        //  Setear datos de la solicitud en el view
        tvPaciente.text = solicitud.nombrePaciente
        tvConsulta.text = solicitud.consulta

        //  Listeners
        etHoraAtencion.setOnClickListener { openTimePicker() }
        btnEnviar.setOnClickListener { aceptarSolicitud(solicitud?.idPaciente!!) }
        btnCancelar.setOnClickListener { cancelarSolicitud(solicitud?.idPaciente!!) }

        return view
    }

    private fun aceptarSolicitud(idPaciente: String) {
        solicitudProvider.enviarPrecioConsulta(
            idPaciente,
            "valorado",
            "${shpMedico?.nombres} ${shpMedico?.apellidos}",
            "${shpMedico?.razonSocial}",
            etValorConsulta.text.toString().toDouble(),
            etHoraAtencion.text.toString(),
            shpMedico?.consultorioLat!!,
            shpMedico?.consultorioLng!!
        ).addOnCompleteListener {
            (activity as? SolicitudesActivity)?.isValorizando = false // Permito recibir cosnultas de la modal que se lanza en SolciitudesActivity
            if (it.isSuccessful) {
//                geoProvider.removeLocation(authProvider.getId())  DESCOMENTAR PARA UTILIZARLO DESPUES
                dismiss()
            }
        }
    }

    private fun cancelarSolicitud(idPaciente: String) {
        solicitudProvider.updateStatus(idPaciente, "cancelado").addOnCompleteListener {
//            (activity as? MapMedicoActivity)?.timer?.cancel() // Detengo el timer de la modal que se lanza en MapMedicoActivity
            dismiss()
        }
    }

    private fun limpiar() {
        tvPaciente.text = ""
        tvConsulta.text = ""
        etValorConsulta.setText(null)
        etHoraAtencion.setText(null)
    }

    //se ejecuta cunaod el usuario oculta el modal
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
//        (activity as? MapMedicoActivity)?.timer?.cancel() // Detengo el timer de la modal que se lanza en MapMedicoActivity
    }

    private fun openTimePicker() {
        val isSystem24Hour = is24HourFormat(requireContext())
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(10)
                .setTitleText("Select Appointment time")
                .build()

        picker.show(childFragmentManager, "TAG")

        picker.addOnPositiveButtonClickListener {
            Log.d("FRAGMENT HORA", "POSITIVE")
            val h = picker.hour
            val min = picker.minute
            Log.d("FRAGMENT HORA", "HORA: ${h} : ${min}")
            etHoraAtencion.setText("${h} : ${min}")
        }
        picker.addOnNegativeButtonClickListener {
            // call back code
        }
        picker.addOnCancelListener {
            // call back code
        }
        picker.addOnDismissListener {
            // call back code
        }
    }
}