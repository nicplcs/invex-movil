package com.example.appinterface.Api

data class Movimiento(
    val id_movimiento: Int,
    val tipo: String,
    val descripcion: String,
    val cantidad: Int,
    val fecha: String,
    val usuario_responsable: String,
    val accion: String,
    val id_producto: Int?
)