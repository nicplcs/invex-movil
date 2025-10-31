package com.example.appinterface.Api


data class Usuario(
    val id_usuario: Int,
    val rol: String,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val telefono: String?,
    val fecha_Nacimiento: String?,
    val estado: String
)