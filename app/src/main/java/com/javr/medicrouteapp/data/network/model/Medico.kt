package com.javr.medicrouteapp.data.network.model

import android.os.Parcelable
import com.beust.klaxon.Klaxon
import kotlinx.parcelize.Parcelize

private val klaxon = Klaxon()

@Parcelize
data class Medico (
    val id: String? = null,
    val nombres: String? = null,
    val apellidos: String? = null,
    val cedula: String? = null,
    val telefono: String? = null,
    val sexo: String? = null,
    val razonSocial: String? = null,
    val ruc: String? = null,
    val registroSanitario: String? = null,
    val consultorioLat: Double? = null,
    val consultorioLng: Double? = null,
    var imagenUrl: String? = null,
    var pdfUrl: String? = null,
    val status: String? = null
) : Parcelable {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Medico>(json)
    }
}