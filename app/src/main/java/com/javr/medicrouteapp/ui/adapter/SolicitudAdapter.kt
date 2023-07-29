package com.javr.medicrouteapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.model.Solicitud
import com.javr.medicrouteapp.databinding.ItemSolicitudBinding

class SolicitudAdapter(
    private val solicitudList: List<Solicitud>, private val onClickItemSolicitud: (Solicitud) -> Unit
) : RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SolicitudViewHolder(layoutInflater.inflate(R.layout.item_solicitud, parent, false))
    }

    override fun getItemCount(): Int = solicitudList.size

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val item = solicitudList[position]
        holder.bind(item, onClickItemSolicitud)
    }

    class SolicitudViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemSolicitudBinding.bind(view)

        fun bind(solicitud: Solicitud, onClickItemSolicitud: (Solicitud) -> Unit) {
            binding.tvNombrePaciente.text = solicitud.nombrePaciente

            itemView.setOnClickListener {
                onClickItemSolicitud(solicitud)
            }
        }
    }
}