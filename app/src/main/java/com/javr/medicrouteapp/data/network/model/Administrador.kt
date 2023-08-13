package com.javr.medicrouteapp.data.network.model

import android.os.Parcelable
import com.beust.klaxon.Klaxon
import kotlinx.parcelize.Parcelize

private val klaxon = Klaxon()

@Parcelize
data class Administrador (
    val id: String? = null,
    val nombres: String? = null,
    val status: String? = null
) : Parcelable {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Administrador>(json)
    }
}