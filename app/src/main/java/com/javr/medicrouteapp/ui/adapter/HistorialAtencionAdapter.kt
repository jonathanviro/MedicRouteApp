package com.javr.medicrouteapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.core.Global
import com.javr.medicrouteapp.data.network.model.Historial
import com.javr.medicrouteapp.databinding.ItemHistorialAtencionBinding

class HistorialAtencionAdapter(
    private val historialAtencionList: List<Historial>, private val onClickItemHistorialAtencion: (Historial) -> Unit
) : RecyclerView.Adapter<HistorialAtencionAdapter.HistorialAtencionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialAtencionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HistorialAtencionViewHolder(layoutInflater.inflate(R.layout.item_historial_atencion, parent, false))
    }

    override fun getItemCount(): Int = historialAtencionList.size

    override fun onBindViewHolder(holder: HistorialAtencionViewHolder, position: Int) {
        val item = historialAtencionList[position]
        holder.bind(item, onClickItemHistorialAtencion)
    }

    class HistorialAtencionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemHistorialAtencionBinding.bind(view)

        fun bind(historial: Historial, onClickItemHistorialAtencion: (Historial) -> Unit) {
            binding.tvHpNombrePaciente.text = "${historial.nombrePaciente}"
            binding.tvHpHorarioAtendido.text = "${Global.timestampToDate(historial.timestamp!!)}"

            itemView.setOnClickListener {
                onClickItemHistorialAtencion(historial)
            }
        }
    }
}