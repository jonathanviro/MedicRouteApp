package com.javr.medicrouteapp.data.sharedpreferences

import android.content.Context
import com.google.gson.Gson
import com.javr.medicrouteapp.data.network.model.Medico

object MedicoManager {
    private const val PREFS_NAME = "MiApp"
    private const val KEY_MEDICO = "medico"

    private var medico: Medico? = null

    fun guardarMedico(context: Context, medico: Medico) {
        this.medico = medico

        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val medicoJson = gson.toJson(medico)
        editor.putString(KEY_MEDICO, medicoJson)
        editor.apply()
    }

    fun obtenerMedico(context: Context): Medico? {
        if (medico == null) {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val medicoJson = sharedPreferences.getString(KEY_MEDICO, null)
            medico = if (medicoJson != null) {
                val gson = Gson()
                gson.fromJson(medicoJson, Medico::class.java)
            } else {
                null
            }
        }
        return medico
    }
}
