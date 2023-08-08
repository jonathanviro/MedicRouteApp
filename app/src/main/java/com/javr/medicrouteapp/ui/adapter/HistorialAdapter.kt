package com.javr.medicrouteapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.data.network.model.Historial
import com.javr.medicrouteapp.databinding.ItemHistorialPacienteBinding

class HistorialAdapter(
    private val historialList: List<Historial>, private val onClickItemHistorial: (Historial) -> Unit
) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HistorialViewHolder(layoutInflater.inflate(R.layout.item_historial_paciente, parent, false))
    }

    override fun getItemCount(): Int = historialList.size

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val item = historialList[position]
        holder.bind(item, onClickItemHistorial)
    }

    class HistorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemHistorialPacienteBinding.bind(view)

        fun bind(historial: Historial, onClickItemHistorial: (Historial) -> Unit) {
            binding.tvHpNombreMedico.text = "Dr. ${historial.nombreMedico}"
            binding.tvHpHorarioAtendido.text = "${Global.timestampToDate(historial.timestamp!!)}"

            itemView.setOnClickListener {
                onClickItemHistorial(historial)
            }
        }
    }
}