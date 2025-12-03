package com.example.appinterface.Api

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class LoginResponse(
    val token: String,
    val nombre: String,
    val correo: String,
    val rol: String
)

data class ErrorResponse(
    val mensaje: String
)