package com.learning.app.utils.validation

import android.util.Patterns

object InputValidator {
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        // At least 8 characters, 1 uppercase, 1 lowercase, 1 digit, 1 special char
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }
    }

    fun isValidName(name: String): Boolean {
        return name.isNotEmpty() && name.length >= 2
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.isNotEmpty() && Patterns.PHONE.matcher(phone).matches()
    }
}
