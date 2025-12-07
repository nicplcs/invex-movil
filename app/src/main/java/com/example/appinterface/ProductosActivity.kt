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

        val spinnerEstado = findViewById<AutoCompleteTextView>(R.id.spinnerEstado)
        val estados = arrayOf("1", "0")
        spinnerEstado.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estados))

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
        val stock = findViewById<EditText>(R.id.etStock).text.toString().toIntOrNull()
        val stockMinimo = findViewById<EditText>(R.id.etStockMinimo).text.toString().toIntOrNull()
        val stockMaximo = findViewById<EditText>(R.id.etStockMaximo).text.toString().toIntOrNull()
        val stockActual = findViewById<EditText>(R.id.etStockActual).text.toString().toIntOrNull()
        val idCategoria = findViewById<EditText>(R.id.etIdCategoria).text.toString().toIntOrNull()
        val idProveedor = findViewById<EditText>(R.id.etIdProveedor).text.toString().toIntOrNull()
        val estado = findViewById<AutoCompleteTextView>(R.id.spinnerEstado).text.toString().trim()

        if (nombre.isEmpty() || precio == null || stock == null || stockMinimo == null ||
            stockMaximo == null || stockActual == null || idCategoria == null ||
            idProveedor == null || estado.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (productoEditando != null) {
            val productoActualizado = Producto(
                productoEditando!!.idProducto, nombre, precio, stock,
                stockMinimo, stockMaximo, stockActual, idCategoria, idProveedor, estado
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
            val nuevo = Producto(0, nombre, precio, stock, stockMinimo, stockMaximo, stockActual, idCategoria, idProveedor, estado)

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
        findViewById<EditText>(R.id.etStock).setText(producto.stock.toString())
        findViewById<EditText>(R.id.etStockMinimo).setText(producto.stockMinimo.toString())
        findViewById<EditText>(R.id.etStockMaximo).setText(producto.stockMaximo.toString())
        findViewById<EditText>(R.id.etStockActual).setText(producto.stockActual.toString())
        findViewById<EditText>(R.id.etIdCategoria).setText(producto.idCategoria.toString())
        findViewById<EditText>(R.id.etIdProveedor).setText(producto.idProveedor.toString())
        findViewById<AutoCompleteTextView>(R.id.spinnerEstado).setText(producto.estado, false)
        findViewById<CardView>(R.id.cardFormulario).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnToggleForm).text = "- Cancelar Edición"
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Actualizar Producto"
    }

    private fun limpiarFormulario() {
        findViewById<EditText>(R.id.etNombre).setText("")
        findViewById<EditText>(R.id.etPrecio).setText("")
        findViewById<EditText>(R.id.etStock).setText("")
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