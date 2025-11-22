package com.example.appinterface.Api

data class Proveedor(
    val id: Int,
    val nombre: String,
    val direccion: String?,
    val contacto: String?,
    val estado: String
)