package com.example.appinterface

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.appinterface.Api.ApiServicesKotlin
import com.example.appinterface.Api.Producto
import com.example.appinterface.Api.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EntradaProductosActivity : AppCompatActivity() {

    private lateinit var spinnerProducto: Spinner
    private lateinit var etCantidad: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var btnVolver: Button

    private var listaProductos: List<Producto> = emptyList()
    private lateinit var api: ApiServicesKotlin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada_productos)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Entrada de Productos"

        api = RetrofitInstance.api2kotlin   // tu instancia de Retrofit

        spinnerProducto = findViewById(R.id.spinnerProductoEntrada)
        etCantidad = findViewById(R.id.etCantidadEntrada)
        btnConfirmar = findViewById(R.id.btnConfirmarEntrada)
        btnVolver = findViewById(R.id.btnVolverEntrada)

        cargarProductos()

        btnConfirmar.setOnClickListener {
            registrarEntrada()
        }

        btnVolver.setOnClickListener {
            finish() // vuelve al módulo de productos
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun cargarProductos() {
        api.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(
                call: Call<List<Producto>>,
                response: Response<List<Producto>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    listaProductos = response.body()!!
                    llenarSpinner()
                } else {
                    Toast.makeText(
                        this@EntradaProductosActivity,
                        "Error al cargar productos (código ${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ENTRADA", "Error getProductos: code=${response.code()}, body=${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(
                    this@EntradaProductosActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("ENTRADA", "Fallo getProductos", t)
            }
        })
    }

    private fun llenarSpinner() {
        if (listaProductos.isEmpty()) {
            Toast.makeText(this, "No hay productos disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = listaProductos.map { it.nombre }  // campo 'nombre' de tu data class
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombres
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProducto.adapter = adapter
    }

    private fun registrarEntrada() {
        val pos = spinnerProducto.selectedItemPosition
        if (pos == Spinner.INVALID_POSITION || listaProductos.isEmpty()) {
            Toast.makeText(this, "Selecciona un producto", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidadStr = etCantidad.text.toString().trim()
        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Ingresa una cantidad", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidad = cantidadStr.toIntOrNull()
        if (cantidad == null || cantidad <= 0) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show()
            return
        }

        val producto = listaProductos[pos]

        // === AQUÍ HACEMOS LO MISMO QUE EN PHP ===
        val nuevoStock = producto.stock + cantidad
        val nuevoStockActual = producto.stockActual + cantidad

        // Advertencia si se pasa del stock máximo (igual que tu PHP)
        if (nuevoStockActual > producto.stockMaximo) {
            Toast.makeText(
                this,
                "ADVERTENCIA: La entrada excede el stock máximo permitido.",
                Toast.LENGTH_LONG
            ).show()
        }

        // Creamos una copia del producto con stock actualizado
        val productoActualizado = producto.copy(
            stock = nuevoStock,
            stockActual = nuevoStockActual
        )

        api.actualizarProducto(producto.idProducto, productoActualizado)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EntradaProductosActivity,
                            "Entrada registrada correctamente. Nuevo stock: $nuevoStockActual",
                            Toast.LENGTH_SHORT
                        ).show()
                        etCantidad.text.clear()
                    } else {
                        Toast.makeText(
                            this@EntradaProductosActivity,
                            "Error al actualizar producto (código ${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "ENTRADA",
                            "Error actualizarProducto: code=${response.code()}, body=${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@EntradaProductosActivity,
                        "Error de conexión: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ENTRADA", "Fallo actualizarProducto", t)
                }
            })
    }
}
