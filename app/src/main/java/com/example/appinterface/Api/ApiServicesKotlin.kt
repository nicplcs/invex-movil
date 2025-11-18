package com.example.appinterface.Api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiServicesKotlin {

    //USUARIOS

    @GET("/usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    @POST("/usuarios")
    fun crearUsuario(@Body usuario: Usuario): Call<Usuario>

    @PUT("/usuarios/{id}")
    fun actualizarUsuario(@Path("id") id: Int, @Body usuario: Usuario): Call<Usuario>

    @DELETE("/usuarios/{id}")
    fun eliminarUsuario(@Path("id") id: Int): Call<Void>


    // MOVIMIENTOS

    @GET("/movimientos")
    fun getMovimientos(): Call<List<Movimiento>>

    @HTTP(method = "DELETE", path = "eliminarMovimiento", hasBody = true)
    fun deleteMovimiento(@Body movimiento: Movimiento): Call<Void>


    //PRODUCTOS

    @GET("/productos")
    fun getProductos(): Call<List<Producto>>

    @POST("/productos")
    fun crearProducto(@Body producto: Producto): Call<Void>

    @PUT("/productos/{id}")
    fun actualizarProducto(
        @Path("id") id: Int,
        @Body producto: Producto
    ): Call<Void>

    @DELETE("/productos/{id}")
    fun eliminarProducto(
        @Path("id") id: Int
    ): Call<Void>
  
      //DEVOLUCIONES

    @GET("/devoluciones")
    fun getDevoluciones(): Call<List<Devolucion>>

}