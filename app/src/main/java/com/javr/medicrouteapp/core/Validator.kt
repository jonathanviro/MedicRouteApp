package com.javr.medicrouteapp.core

object Validator {

    fun isValidCedula(cedula: String): Boolean {
        if (cedula.length != 10) {
            return false
        }

        val cedulaDigits = cedula.map { it.toString().toIntOrNull() ?: return false }
        val verificationDigit = cedulaDigits.last()

        val multiplierSequence = listOf(2, 1, 2, 1, 2, 1, 2, 1, 2)
        val multipliedDigits =
            cedulaDigits.dropLast(1).zip(multiplierSequence).map { (digit, multiplier) -> digit * multiplier }
        val multipliedSum = multipliedDigits.map { if (it > 9) it - 9 else it }.sum()

        val computedVerificationDigit = if (multipliedSum % 10 == 0) 0 else 10 - (multipliedSum % 10)

        return verificationDigit == computedVerificationDigit
    }

    fun isValidRUC(ruc: String): Boolean {
        // Verificar que el RUC tenga 13 dígitos
        if (ruc.length != 13 || !ruc.all { it.isDigit() }) {
            return false
        }

        // Verificar el dígito de control
        val coeficientes = intArrayOf(2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2)
        var suma = 0
        for (i in 0 until 10) {
            val digito = Character.getNumericValue(ruc[i])
            val producto = digito * coeficientes[i]
            suma += if (producto >= 10) producto - 9 else producto
        }
        val digitoControlCalculado = (10 - (suma % 10)) % 10
        val digitoControlReal = Character.getNumericValue(ruc[10])

        return digitoControlCalculado == digitoControlReal
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""")
        return emailRegex.matches(email)
    }

    fun isValidPassword(password: String): Boolean = password.length < 6
}