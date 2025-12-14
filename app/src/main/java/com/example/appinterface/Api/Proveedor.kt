package com.example.appinterface.Api

data class Proveedor(
    val id: Int,
    val nombre: String,
    val direccion: String?,
    val telefono: String?,
    val correo: String?,
    val estado: String
)