package com.javr.medicrouteapp.core

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

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
}