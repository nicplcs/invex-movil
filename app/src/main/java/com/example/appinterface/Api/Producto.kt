package com.example.appinterface.Api

data class Producto(

    val idProducto: Int,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val stockMinimo: Int,
    val stockMaximo: Int,
    val stockActual: Int,
    val idCategoria: Int,
    val idProveedor: Int,
    val estado: String
)
