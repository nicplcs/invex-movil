package com.example.appinterface.Api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiServicesKotlin {
    @GET("/usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    @POST("/usuarios")
    fun crearUsuario(@Body usuario: Usuario): Call<String>

    @GET("/movimientos")
    fun getMovimientos(): Call<List<Movimiento>>

    @DELETE("eliminarMovimiento")
    fun deleteMovimiento(@Body movimiento: Movimiento): Call<Void>
}