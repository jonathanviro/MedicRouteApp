package com.javr.medicrouteapp.data.network.model

import com.beust.klaxon.Klaxon

private val klaxon = Klaxon()

data class Medico (
    val id: String? = null,
    val nombres: String? = null,
    val apellidos: String? = null,
    val cedula: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val sexo: String? = null,
    val razonSocial: String? = null,
    val ruc: String? = null,
    val registroSanitario: String? = null,
    val consultorioLat: Double? = null,
    val consultorioLng: Double? = null
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Medico>(json)
    }
}