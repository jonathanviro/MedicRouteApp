package com.javr.medicrouteapp.data.network.model

import com.beust.klaxon.Klaxon
import java.sql.Timestamp

private val klaxon = Klaxon()

data class Historial (
    var id: String? = null,
    val idPaciente: String? = null,
    val idMedicoAsignado: String? = null,
    val origin: String? = null,
    val destination: String? = null,
    val calificationToClient: Double? = null,
    val calificationToMedico: Double? = null,
    val originLat: Double? = null,
    val originLng: Double? = null,
    val destinationLat: Double? = null,
    val destinationLng: Double? = null,
    val timestamp: Long? = null
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Historial>(json)
    }
}