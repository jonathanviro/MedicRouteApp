package com.javr.medicrouteapp.core

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.javr.medicrouteapp.R
import dmax.dialog.SpotsDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Global {
    fun setErrorInTextInputLayout(editText: EditText, textInputLayout: TextInputLayout){
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                textInputLayout.setError(null)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    fun setErrorInTextInputLayout(textInputLayout: TextInputLayout, message: String){
        textInputLayout.setError(message)
        textInputLayout.requestFocus()
    }

    fun obtenerFechaActualConFormato(isFormatoNormal: Boolean): String {
        val dateFormat = if (isFormatoNormal) {
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "ES"))
        } else {
            SimpleDateFormat("ddMMyyyyHHmmss", Locale("es", "ES"))
        }

        val date = dateFormat.format(Date())
        return date.toString()
    }

    fun dialogoCarga(context: Context, titulo: String): AlertDialog {
        val dialog: AlertDialog =
            SpotsDialog.Builder().setContext(context).setTheme(R.style.CustomDialogoCarga)
                .setMessage(titulo).setCancelable(false).build()

        return dialog
    }

    fun timestampToDate(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }
}