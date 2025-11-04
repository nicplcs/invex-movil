package com.example.appinterface.Api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiServicesKotlin {
    @GET("/usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    @POST("/usuarios")
    fun crearUsuario(@Body usuario: Usuario): Call<String>


    @GET("/productos")
    fun getProductos(): Call<List<Producto>>


    @POST("/productos")
    fun crearProducto(@Body producto: Producto): Call<Void>

}