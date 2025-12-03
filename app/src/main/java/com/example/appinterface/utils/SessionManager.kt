package com.example.appinterface.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("AppInventario", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_CORREO = "correo"
        private const val KEY_ROL = "rol"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }

    fun guardarSesion(token: String, nombre: String, correo: String, rol: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_NOMBRE, nombre)
            putString(KEY_CORREO, correo)
            putString(KEY_ROL, rol)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getNombre(): String? = prefs.getString(KEY_NOMBRE, null)

    fun getCorreo(): String? = prefs.getString(KEY_CORREO, null)

    fun getRol(): String? = prefs.getString(KEY_ROL, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}