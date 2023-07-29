package com.javr.medicrouteapp.data.sharedpreferences

import android.content.Context
import com.google.gson.Gson
import com.javr.medicrouteapp.data.network.model.Paciente

object PacienteManager {
    private const val PREFS_NAME = "MiApp"
    private const val KEY_PACIENTE = "paciente"

    private var paciente: Paciente? = null

    fun guardarPaciente(context: Context, paciente: Paciente) {
        this.paciente = paciente

        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val pacienteJson = gson.toJson(paciente)
        editor.putString(KEY_PACIENTE, pacienteJson)
        editor.apply()
    }

    fun obtenerPaciente(context: Context): Paciente? {
        if (paciente == null) {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val pacienteJson = sharedPreferences.getString(KEY_PACIENTE, null)
            paciente = if (pacienteJson != null) {
                val gson = Gson()
                gson.fromJson(pacienteJson, Paciente::class.java)
            } else {
                null
            }
        }
        return paciente
    }
}
