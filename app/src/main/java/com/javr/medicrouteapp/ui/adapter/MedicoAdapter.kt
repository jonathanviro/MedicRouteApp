package com.javr.medicrouteapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.javr.medicrouteapp.R
import com.javr.medicrouteapp.data.network.model.Medico
import com.javr.medicrouteapp.databinding.ItemMedicoBinding

class MedicoAdapter(
    private val medicoList: List<Medico>, private val onClickItemMedico: (Medico) -> Unit
) : RecyclerView.Adapter<MedicoAdapter.MedicoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MedicoViewHolder(layoutInflater.inflate(R.layout.item_medico, parent, false))
    }

    override fun getItemCount(): Int = medicoList.size

    override fun onBindViewHolder(holder: MedicoViewHolder, position: Int) {
        val item = medicoList[position]
        holder.bind(item, onClickItemMedico)
    }

    class MedicoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemMedicoBinding.bind(view)

        fun bind(medico: Medico, onClickItemMedico: (Medico) -> Unit) {
            binding.tvNombreMedico.text = "${medico.nombres} ${medico.apellidos}"

            itemView.setOnClickListener {
                onClickItemMedico(medico)
            }
        }
    }
}