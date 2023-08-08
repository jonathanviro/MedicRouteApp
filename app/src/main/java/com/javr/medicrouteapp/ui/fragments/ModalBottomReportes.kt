package layout.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.firebase.HistorialProvider
import com.javr.medicrouteapp.data.network.model.Historial

class ModalBottomReportes : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    private val historialProvider = HistorialProvider()
    private lateinit var historial: Historial

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_reportes, container, false)

        return view
    }

    private fun reporteMedicos() {

    }

    private fun reportePacientes() {

    }

    //se ejecuta cunaod el usuario oculta el modal
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
//        (activity as? MapMedicoActivity)?.timer?.cancel() // Detengo el timer de la modal que se lanza en MapMedicoActivity
    }
}