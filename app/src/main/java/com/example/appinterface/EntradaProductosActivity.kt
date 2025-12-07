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

        // Configurar ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Entrada de Productos"

        //  USAR LA INSTANCIA CON JWT AUTHENTICATION
        api = RetrofitInstance.getApi(this)

        // Inicializar vistas
        spinnerProducto = findViewById(R.id.spinnerProductoEntrada)
        etCantidad = findViewById(R.id.etCantidadEntrada)
        btnConfirmar = findViewById(R.id.btnConfirmarEntrada)
        btnVolver = findViewById(R.id.btnVolverEntrada)

        // Cargar productos
        cargarProductos()

        // Listeners
        btnConfirmar.setOnClickListener {
            registrarEntrada()
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
        // Mostrar mensaje de carga
        Toast.makeText(this, "Cargando productos...", Toast.LENGTH_SHORT).show()
        Log.d("ENTRADA", "Iniciando carga de productos...")

        api.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(
                call: Call<List<Producto>>,
                response: Response<List<Producto>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    listaProductos = response.body()!!

                    Log.d("ENTRADA", " Productos cargados exitosamente: ${listaProductos.size}")

                    if (listaProductos.isNotEmpty()) {
                        llenarSpinner()
                        Toast.makeText(
                            this@EntradaProductosActivity,
                            " ${listaProductos.size} productos cargados",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@EntradaProductosActivity,
                            "️ No hay productos disponibles",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                    Toast.makeText(
                        this@EntradaProductosActivity,
                        " Error al cargar productos (código ${response.code()})",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("ENTRADA", " Error getProductos: code=${response.code()}, body=$errorBody")
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(
                    this@EntradaProductosActivity,
                    " Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("ENTRADA", " Fallo getProductos", t)
            }
        })
    }

    private fun llenarSpinner() {
        if (listaProductos.isEmpty()) {
            Toast.makeText(this, "No hay productos disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear lista de nombres con info adicional
        val nombres = listaProductos.map {
            "${it.nombre} (Stock: ${it.stockActual}/${it.stockMaximo})"
        }

        Log.d("ENTRADA", "Llenando spinner con ${nombres.size} items")

        // Crear adaptador personalizado
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

        Log.d("ENTRADA", " Spinner llenado correctamente")
    }

    private fun registrarEntrada() {
        val pos = spinnerProducto.selectedItemPosition

        // Validar selección del spinner
        if (pos == Spinner.INVALID_POSITION || pos < 0) {
            Toast.makeText(this, " Selecciona un producto", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaProductos.isEmpty()) {
            Toast.makeText(this, "No hay productos cargados", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar cantidad
        val cantidadStr = etCantidad.text.toString().trim()
        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, " Ingresa una cantidad", Toast.LENGTH_SHORT).show()
            etCantidad.requestFocus()
            return
        }

        val cantidad = cantidadStr.toIntOrNull()
        if (cantidad == null || cantidad <= 0) {
            Toast.makeText(this, " Cantidad inválida (debe ser mayor a 0)", Toast.LENGTH_SHORT).show()
            etCantidad.requestFocus()
            return
        }

        // Obtener producto seleccionado
        val producto = listaProductos[pos]

        // Calcular nuevos valores de stock
        val nuevoStock = producto.stock + cantidad
        val nuevoStockActual = producto.stockActual + cantidad

        // Advertencia si excede stock máximo
        if (nuevoStockActual > producto.stockMaximo) {
            Toast.makeText(
                this,
                " ADVERTENCIA: La entrada excede el stock máximo permitido (${producto.stockMaximo})",
                Toast.LENGTH_LONG
            ).show()
        }

        // Crear producto actualizado
        val productoActualizado = producto.copy(
            stock = nuevoStock,
            stockActual = nuevoStockActual
        )

        // Realizar actualización
        Log.d("ENTRADA", "Actualizando producto ${producto.idProducto}: stockActual ${producto.stockActual} -> $nuevoStockActual")

        api.actualizarProducto(producto.idProducto, productoActualizado)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EntradaProductosActivity,
                            " Entrada registrada correctamente\n" +
                                    "Producto: ${producto.nombre}\n" +
                                    "Cantidad: +$cantidad\n" +
                                    "Nuevo stock: $nuevoStockActual",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.d("ENTRADA", " Producto actualizado exitosamente")

                        // Limpiar campo
                        etCantidad.text.clear()

                        // Recargar productos para actualizar el spinner
                        cargarProductos()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                        Toast.makeText(
                            this@EntradaProductosActivity,
                            " Error al actualizar producto (código ${response.code()})",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("ENTRADA", " Error actualizarProducto: code=${response.code()}, body=$errorBody")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@EntradaProductosActivity,
                        " Error de conexión: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("ENTRADA", " Fallo actualizarProducto", t)
                }
            })
    }
}