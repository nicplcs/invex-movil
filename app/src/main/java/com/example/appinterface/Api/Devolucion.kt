package com.example.appinterface.Api

data class Devolucion(

        val id_devolucion: Int,
        val cantidad: Int,
        val motivo: String,
        val fecha_devolucion: String,
        val id_ordensalida: Int,
        val id_producto: Int,
)