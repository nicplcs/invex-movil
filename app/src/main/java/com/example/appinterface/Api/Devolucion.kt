package com.example.appinterface.Api

import com.google.gson.annotations.SerializedName

data class Devolucion(

        @SerializedName("idDevolucion")
        val id_devolucion: Int,

        val cantidad: Int,
        val motivo: String,

        @SerializedName("fechaDevolucion")
        val fecha_devolucion: String,

        @SerializedName("idOrdenSalida")
        val id_ordensalida: Int,

        @SerializedName("idProducto")
        val id_producto: Int,
)