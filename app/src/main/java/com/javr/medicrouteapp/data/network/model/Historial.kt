package com.javr.medicrouteapp.data.network.model

import android.os.Parcelable
import com.beust.klaxon.Klaxon
import kotlinx.parcelize.Parcelize

private val klaxon = Klaxon()

@Parcelize
data class Historial (
    var id: String? = null,
    val idPaciente: String? = null,
    val idMedico: String? = null,
    val calificacionParaPaciente: Float? = null,
    val calificacionParaMedico: Float? = null,
    val nombrePaciente: String? = null,
    val nombreMedico: String? = null,
    val nombreConsultorio: String? = null,
    val consulta: String? = null,
    val diagnostico: String? = null,
    val receta: String? = null,
    val horaAgendada: String? = null,
    val precio: Double? = null,
    val timestamp: Long? = null
) : Parcelable {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Historial>(json)
    }
}