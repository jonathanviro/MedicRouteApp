package com.javr.medicrouteapp.data.network.model

import android.os.Parcelable
import com.beust.klaxon.*
import kotlinx.parcelize.Parcelize

private val klaxon = Klaxon()

@Parcelize
data class Solicitud (
    val idPaciente: String? = null,
    val idMedico: String? = null,
    val status: String? = null,
    val nombrePaciente: String? = null,
    val nombreMedico: String? = null,
    val nombreConsultorio: String? = null,
    val consulta: String? = null,
    val horaAgendada: String? = null,
    val diagnostico: String? = null,
    val receta: String? = null,
    val precio: Double? = null,
    val pacienteLat: Double? = null,
    val pacienteLng: Double? = null,
    val consultorioLat: Double? = null,
    val consultorioLng: Double? = null


) :Parcelable {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Solicitud>(json)
    }
}