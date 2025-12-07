package com.example.appinterface

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
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

        api = RetrofitInstance.getApi(this)

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
        Toast.makeText(this, "Cargando productos...", Toast.LENGTH_SHORT).show()

        api.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(
                call: Call<List<Producto>>,
                response: Response<List<Producto>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    listaProductos = response.body()!!

                    Log.d("SALIDA", "Productos cargados: ${listaProductos.size}")

                    if (listaProductos.isNotEmpty()) {
                        llenarSpinner()
                        Toast.makeText(
                            this@SalidaProductosActivity,
                            "${listaProductos.size} productos cargados",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@SalidaProductosActivity,
                            "No hay productos disponibles",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                    Toast.makeText(
                        this@SalidaProductosActivity,
                        "Error al cargar productos (código ${response.code()})",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("SALIDA", "Error getProductos: code=${response.code()}, body=$errorBody")
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(
                    this@SalidaProductosActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
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

        val nombres = listaProductos.map {
            "${it.nombre} (Stock: ${it.stockActual}/${it.stockMaximo})"
        }

        Log.d("SALIDA", "Llenando spinner con ${nombres.size} items")

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            nombres
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(Color.WHITE)
                textView.setBackgroundColor(Color.parseColor("#2B2B2B"))
                textView.setPadding(30, 25, 30, 25)
                textView.textSize = 14f
                return view
            }

            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(Color.WHITE)
                textView.setPadding(15, 15, 15, 15)
                textView.textSize = 14f
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProducto.adapter = adapter

        Log.d("SALIDA", "Spinner llenado correctamente")
    }

    private fun registrarSalida() {
        val pos = spinnerProducto.selectedItemPosition

        if (pos == Spinner.INVALID_POSITION || pos < 0) {
            Toast.makeText(this, "Selecciona un producto", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaProductos.isEmpty()) {
            Toast.makeText(this, "No hay productos cargados", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidadStr = etCantidad.text.toString().trim()
        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Ingresa una cantidad", Toast.LENGTH_SHORT).show()
            etCantidad.requestFocus()
            return
        }

        val cantidad = cantidadStr.toIntOrNull()
        if (cantidad == null || cantidad <= 0) {
            Toast.makeText(this, "Cantidad inválida (debe ser mayor a 0)", Toast.LENGTH_SHORT).show()
            etCantidad.requestFocus()
            return
        }

        val producto = listaProductos[pos]

        val nuevoStock = producto.stock - cantidad
        val nuevoStockActual = producto.stockActual - cantidad

        if (nuevoStock < 0 || nuevoStockActual < 0) {
            Toast.makeText(
                this,
                "No hay stock suficiente para esa salida\nStock disponible: ${producto.stockActual}",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (nuevoStockActual < producto.stockMinimo) {
            Toast.makeText(
                this,
                "ADVERTENCIA: El stock quedará por debajo del mínimo (${producto.stockMinimo})",
                Toast.LENGTH_LONG
            ).show()
        }

        val productoActualizado = producto.copy(
            stock = nuevoStock,
            stockActual = nuevoStockActual
        )

        Log.d("SALIDA", "Actualizando producto ${producto.idProducto}: stockActual ${producto.stockActual} -> $nuevoStockActual")

        api.actualizarProducto(producto.idProducto, productoActualizado)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@SalidaProductosActivity,
                            "Salida registrada correctamente\n" +
                                    "Producto: ${producto.nombre}\n" +
                                    "Cantidad: -$cantidad\n" +
                                    "Stock restante: $nuevoStockActual",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.d("SALIDA", "Producto actualizado exitosamente")

                        etCantidad.text.clear()
                        cargarProductos()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                        Toast.makeText(
                            this@SalidaProductosActivity,
                            "Error al actualizar producto (código ${response.code()})",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("SALIDA", "Error actualizarProducto: code=${response.code()}, body=$errorBody")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@SalidaProductosActivity,
                        "Error de conexión: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("SALIDA", "Fallo actualizarProducto", t)
                }
            })
    }
}