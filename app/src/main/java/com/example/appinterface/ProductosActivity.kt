package com.example.appinterface

import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.ProductosAdapter
import com.example.appinterface.Api.Producto
import com.example.appinterface.Api.RetrofitInstance
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductosAdapter
    private var productoEditando: Producto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)

        recyclerView = findViewById(R.id.recyclerProductos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProductosAdapter(mutableListOf())
        recyclerView.adapter = adapter

        // FIX 1: Configurar el spinner de estado correctamente
        val spinnerEstado = findViewById<AutoCompleteTextView>(R.id.spinnerEstado)
        val estados = arrayOf("Activo", "Inactivo")
        val adapterEstado = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estados)
        spinnerEstado.setAdapter(adapterEstado)
        spinnerEstado.threshold = 1 // Muestra las opciones desde el primer carácter


        spinnerEstado.setOnClickListener {
            spinnerEstado.showDropDown()
        }

        val cardFormulario = findViewById<CardView>(R.id.cardFormulario)
        val btnToggleForm = findViewById<MaterialButton>(R.id.btnToggleForm)
        val btnGuardar = findViewById<MaterialButton>(R.id.btnGuardar)

        btnToggleForm.setOnClickListener {
            if (cardFormulario.visibility == View.GONE) {
                cardFormulario.visibility = View.VISIBLE
                btnToggleForm.text = "- Ocultar Formulario"
            } else {
                cardFormulario.visibility = View.GONE
                btnToggleForm.text = "+ Agregar Nuevo Producto"
                limpiarFormulario()
            }
        }

        btnGuardar.setOnClickListener {
            guardarProducto()
        }

        val swipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val producto = adapter.getProducto(pos)

                if (direction == ItemTouchHelper.RIGHT) {
                    cargarProductoEnFormulario(producto)
                    adapter.notifyItemChanged(pos)
                } else {
                    confirmarEliminar(producto)
                }
            }
        }
        ItemTouchHelper(swipe).attachToRecyclerView(recyclerView)

        cargarProductos()
    }

    private fun cargarProductos() {
        RetrofitInstance.getApi(this).getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful && response.body() != null) {
                    adapter.updateList(response.body()!!)
                } else {
                    Toast.makeText(this@ProductosActivity, "Error al cargar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(this@ProductosActivity, "Error conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun guardarProducto() {
        val nombre = findViewById<EditText>(R.id.etNombre).text.toString().trim()
        val precio = findViewById<EditText>(R.id.etPrecio).text.toString().toDoubleOrNull()
        // FIX 2: Eliminamos stock, solo usamos stockActual
        val stockMinimo = findViewById<EditText>(R.id.etStockMinimo).text.toString().toIntOrNull()
        val stockMaximo = findViewById<EditText>(R.id.etStockMaximo).text.toString().toIntOrNull()
        val stockActual = findViewById<EditText>(R.id.etStockActual).text.toString().toIntOrNull()
        val idCategoria = findViewById<EditText>(R.id.etIdCategoria).text.toString().toIntOrNull()
        val idProveedor = findViewById<EditText>(R.id.etIdProveedor).text.toString().toIntOrNull()
        val estadoTexto = findViewById<AutoCompleteTextView>(R.id.spinnerEstado).text.toString().trim()

        // FIX 3: Convertir "Activo"/"Inactivo" a "1"/"0"
        val estado = when(estadoTexto) {
            "Activo" -> "1"
            "Inactivo" -> "0"
            else -> ""
        }

        // FIX 4: Validación actualizada sin stock
        if (nombre.isEmpty() || precio == null || stockMinimo == null ||
            stockMaximo == null || stockActual == null || idCategoria == null ||
            idProveedor == null || estado.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        if (productoEditando != null) {
            // FIX 5: Actualizar producto sin el campo stock
            val productoActualizado = Producto(
                productoEditando!!.idProducto,
                nombre,
                precio,
                stockActual, // Ahora el tercer parámetro es stockActual (antes era stock)
                stockMinimo,
                stockMaximo,
                stockActual,
                idCategoria,
                idProveedor,
                estado
            )

            RetrofitInstance.getApi(this).actualizarProducto(productoEditando!!.idProducto, productoActualizado)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProductosActivity, "Producto actualizado", Toast.LENGTH_SHORT).show()
                            limpiarFormulario()
                            findViewById<CardView>(R.id.cardFormulario).visibility = View.GONE
                            findViewById<MaterialButton>(R.id.btnToggleForm).text = "+ Agregar Nuevo Producto"
                            cargarProductos()
                            productoEditando = null
                        } else {
                            Toast.makeText(this@ProductosActivity, "Error al actualizar: ${response.code()}", Toast.LENGTH_SHORT).show()
                            cargarProductos()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@ProductosActivity, "Error actualizar: ${t.message}", Toast.LENGTH_SHORT).show()
                        cargarProductos()
                    }
                })
        } else {
            // FIX 6: Crear producto sin el campo stock
            val nuevo = Producto(
                0,
                nombre,
                precio,
                stockActual, // Aquí también
                stockMinimo,
                stockMaximo,
                stockActual,
                idCategoria,
                idProveedor,
                estado
            )

            RetrofitInstance.getApi(this).crearProducto(nuevo).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProductosActivity, "Producto creado", Toast.LENGTH_SHORT).show()
                        cargarProductos()
                        limpiarFormulario()
                        findViewById<CardView>(R.id.cardFormulario).visibility = View.GONE
                        findViewById<MaterialButton>(R.id.btnToggleForm).text = "+ Agregar Nuevo Producto"
                    } else {
                        Toast.makeText(this@ProductosActivity, "Error al crear: ${response.code()}", Toast.LENGTH_SHORT).show()
                        cargarProductos()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ProductosActivity, "Error crear: ${t.message}", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                }
            })
        }
    }

    private fun confirmarEliminar(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Eliminar '${producto.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                RetrofitInstance.getApi(this).eliminarProducto(producto.idProducto)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ProductosActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                                cargarProductos()
                            } else {
                                Toast.makeText(this@ProductosActivity, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                                cargarProductos()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@ProductosActivity, "Error eliminar: ${t.message}", Toast.LENGTH_SHORT).show()
                            cargarProductos()
                        }
                    })
            }
            .setNegativeButton("Cancelar") { _, _ -> cargarProductos() }
            .show()
    }

    private fun cargarProductoEnFormulario(producto: Producto) {
        productoEditando = producto
        findViewById<EditText>(R.id.etNombre).setText(producto.nombre)
        findViewById<EditText>(R.id.etPrecio).setText(producto.precio.toString())
        // FIX 7: Ya no cargamos stock, solo stockActual
        findViewById<EditText>(R.id.etStockMinimo).setText(producto.stockMinimo.toString())
        findViewById<EditText>(R.id.etStockMaximo).setText(producto.stockMaximo.toString())
        findViewById<EditText>(R.id.etStockActual).setText(producto.stockActual.toString())
        findViewById<EditText>(R.id.etIdCategoria).setText(producto.idCategoria.toString())
        findViewById<EditText>(R.id.etIdProveedor).setText(producto.idProveedor.toString())

        // FIX 8: Convertir "1"/"0" de vuelta a "Activo"/"Inactivo" para mostrar
        val estadoTexto = when(producto.estado) {
            "1" -> "Activo"
            "0" -> "Inactivo"
            else -> producto.estado
        }
        findViewById<AutoCompleteTextView>(R.id.spinnerEstado).setText(estadoTexto, false)

        findViewById<CardView>(R.id.cardFormulario).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnToggleForm).text = "- Cancelar Edición"
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Actualizar Producto"
    }

    private fun limpiarFormulario() {
        findViewById<EditText>(R.id.etNombre).setText("")
        findViewById<EditText>(R.id.etPrecio).setText("")
        // FIX 9: Ya no limpiamos etStock porque no existe
        findViewById<EditText>(R.id.etStockMinimo).setText("")
        findViewById<EditText>(R.id.etStockMaximo).setText("")
        findViewById<EditText>(R.id.etStockActual).setText("")
        findViewById<EditText>(R.id.etIdCategoria).setText("")
        findViewById<EditText>(R.id.etIdProveedor).setText("")
        findViewById<AutoCompleteTextView>(R.id.spinnerEstado).setText("", false)
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Registrar Producto"
        productoEditando = null
    }
}