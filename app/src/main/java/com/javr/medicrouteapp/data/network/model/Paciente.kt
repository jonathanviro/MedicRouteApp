package com.javr.medicrouteapp.data.network.model

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Paciente (
    val id: String? = null,
    val nombres: String? = null,
    val apellidos: String? = null,
    val cedula: String? = null,
    val telefono: String? = null,
    val sexo: String? = null,
    var imagenUrl: String? = null
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Paciente>(json)
    }
}