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

class SalidaProductosActivity : AppCompatActivity() {

    private lateinit var spinnerProducto: Spinner
    private lateinit var etCantidad: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var btnVolver: Button

    private var listaProductos: List<Producto> = emptyList()
    private lateinit var api: ApiServicesKotlin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salida_productos)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Salida de Productos"

        api = RetrofitInstance.api2kotlin

        spinnerProducto = findViewById(R.id.spinnerProductoSalida)
        etCantidad = findViewById(R.id.etCantidadSalida)
        btnConfirmar = findViewById(R.id.btnConfirmarSalida)
        btnVolver = findViewById(R.id.btnVolverSalida)

        cargarProductos()

        btnConfirmar.setOnClickListener {
            registrarSalida()
        }

        btnVolver.setOnClickListener {
            finish()
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
                        this@SalidaProductosActivity,
                        "Error al cargar productos (código ${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("SALIDA", "Error getProductos: code=${response.code()}, body=${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(
                    this@SalidaProductosActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("SALIDA", "Fallo getProductos", t)
            }
        })
    }

    private fun llenarSpinner() {
        if (listaProductos.isEmpty()) {
            Toast.makeText(this, "No hay productos disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = listaProductos.map { it.nombre }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombres
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProducto.adapter = adapter
    }

    private fun registrarSalida() {
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

        val nuevoStock = producto.stock - cantidad
        val nuevoStockActual = producto.stockActual - cantidad

        // No dejar que baje de cero
        if (nuevoStock < 0 || nuevoStockActual < 0) {
            Toast.makeText(
                this,
                "No hay stock suficiente para esa salida",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Advertencia si baja del stock mínimo
        if (nuevoStockActual < producto.stockMinimo) {
            Toast.makeText(
                this,
                "ADVERTENCIA: el stock queda por debajo del mínimo.",
                Toast.LENGTH_LONG
            ).show()
        }

        val productoActualizado = producto.copy(
            stock = nuevoStock,
            stockActual = nuevoStockActual
        )

        api.actualizarProducto(producto.idProducto, productoActualizado)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@SalidaProductosActivity,
                            "Salida registrada correctamente. Stock actual: $nuevoStockActual",
                            Toast.LENGTH_SHORT
                        ).show()
                        etCantidad.text.clear()
                    } else {
                        Toast.makeText(
                            this@SalidaProductosActivity,
                            "Error al actualizar producto (código ${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "SALIDA",
                            "Error actualizarProducto: code=${response.code()}, body=${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@SalidaProductosActivity,
                        "Error de conexión: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("SALIDA", "Fallo actualizarProducto", t)
                }
            })
    }
}
