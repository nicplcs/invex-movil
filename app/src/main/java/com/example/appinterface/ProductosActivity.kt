package com.example.appinterface

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.ProductosAdapter
import com.example.appinterface.Api.Producto
import com.example.appinterface.Api.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)

        recyclerView = findViewById(R.id.recyclerProductos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProductosAdapter(mutableListOf())
        recyclerView.adapter = adapter


        val swipe = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val producto = adapter.getProducto(pos)

                if (direction == ItemTouchHelper.RIGHT) {
                    // Editar
                    mostrarDialogoEditar(producto)
                    adapter.notifyItemChanged(pos)
                } else {
                    // Eliminar
                    confirmarEliminar(producto)
                }
            }
        }
        ItemTouchHelper(swipe).attachToRecyclerView(recyclerView)

        // GET inicial
        cargarProductos()
    }

    //GET

    private fun cargarProductos() {
        RetrofitInstance.api2kotlin.getProductos()
            .enqueue(object : Callback<List<Producto>> {
                override fun onResponse(
                    call: Call<List<Producto>>,
                    response: Response<List<Producto>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        adapter.updateList(response.body()!!)
                    } else {
                        Toast.makeText(
                            this@ProductosActivity,
                            "Error en la respuesta (HTTP ${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                    Toast.makeText(
                        this@ProductosActivity,
                        "Error de conexión: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // POST

    fun agregarProducto(v: View) {
        val cont = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etNombre = EditText(this).apply { hint = "Nombre" }
        val etPrecio = EditText(this).apply {
            hint = "Precio"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etStock = EditText(this).apply {
            hint = "Stock"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val etStockMinimo = EditText(this).apply {
            hint = "Stock mínimo"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val etStockMaximo = EditText(this).apply {
            hint = "Stock máximo"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val etStockActual = EditText(this).apply {
            hint = "Stock actual"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val etIdCategoria = EditText(this).apply {
            hint = "ID categoría"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val etIdProveedor = EditText(this).apply {
            hint = "ID proveedor"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val etEstado = EditText(this).apply {
            hint = "Estado (1 = activo, 0 = inactivo)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        cont.addView(etNombre)
        cont.addView(etPrecio)
        cont.addView(etStock)
        cont.addView(etStockMinimo)
        cont.addView(etStockMaximo)
        cont.addView(etStockActual)
        cont.addView(etIdCategoria)
        cont.addView(etIdProveedor)
        cont.addView(etEstado)

        AlertDialog.Builder(this)
            .setTitle("Nuevo producto")
            .setView(cont)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val precio = etPrecio.text.toString().toDoubleOrNull()
                val stock = etStock.text.toString().toIntOrNull()
                val stockMinimo = etStockMinimo.text.toString().toIntOrNull()
                val stockMaximo = etStockMaximo.text.toString().toIntOrNull()
                val stockActual = etStockActual.text.toString().toIntOrNull()
                val idCategoria = etIdCategoria.text.toString().toIntOrNull()
                val idProveedor = etIdProveedor.text.toString().toIntOrNull()
                val estado = etEstado.text.toString().trim()

                if (nombre.isEmpty() || precio == null || stock == null ||
                    stockMinimo == null || stockMaximo == null || stockActual == null ||
                    idCategoria == null || idProveedor == null || estado.isEmpty()
                ) {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val nuevo = Producto(
                    idProducto = 0,
                    nombre = nombre,
                    precio = precio,
                    stock = stock,
                    stockMinimo = stockMinimo,
                    stockMaximo = stockMaximo,
                    stockActual = stockActual,
                    idCategoria = idCategoria,
                    idProveedor = idProveedor,
                    estado = estado
                )

                RetrofitInstance.api2kotlin.crearProducto(nuevo)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ProductosActivity,
                                    "Producto creado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                cargarProductos()
                            } else {
                                val msg = response.errorBody()?.string()
                                    ?: "HTTP ${response.code()}"
                                Toast.makeText(
                                    this@ProductosActivity,
                                    "Error al crear: $msg",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(
                                this@ProductosActivity,
                                "Conexión fallida: ${t.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            }
            .show()
    }

    // PUT

    private fun mostrarDialogoEditar(producto: Producto) {
        val cont = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etNombre = EditText(this).apply {
            hint = "Nombre"
            setText(producto.nombre)
        }
        val etPrecio = EditText(this).apply {
            hint = "Precio"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(producto.precio.toString())
        }
        val etStock = EditText(this).apply {
            hint = "Stock"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(producto.stock.toString())
        }
        val etStockMinimo = EditText(this).apply {
            hint = "Stock mínimo"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(producto.stockMinimo.toString())
        }
        val etStockMaximo = EditText(this).apply {
            hint = "Stock máximo"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(producto.stockMaximo.toString())
        }
        val etStockActual = EditText(this).apply {
            hint = "Stock actual"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(producto.stockActual.toString())
        }
        val etIdCategoria = EditText(this).apply {
            hint = "ID categoría"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(producto.idCategoria.toString())
        }
        val etIdProveedor = EditText(this).apply {
            hint = "ID proveedor"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(producto.idProveedor.toString())
        }
        val etEstado = EditText(this).apply {
            hint = "Estado (1 = activo, 0 = inactivo)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(producto.estado)
        }

        cont.addView(etNombre)
        cont.addView(etPrecio)
        cont.addView(etStock)
        cont.addView(etStockMinimo)
        cont.addView(etStockMaximo)
        cont.addView(etStockActual)
        cont.addView(etIdCategoria)
        cont.addView(etIdProveedor)
        cont.addView(etEstado)

        AlertDialog.Builder(this)
            .setTitle("Editar producto")
            .setView(cont)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Actualizar") { _, _ ->

                val nombre = etNombre.text.toString().trim()
                val precio = etPrecio.text.toString().toDoubleOrNull()
                val stock = etStock.text.toString().toIntOrNull()
                val stockMinimo = etStockMinimo.text.toString().toIntOrNull()
                val stockMaximo = etStockMaximo.text.toString().toIntOrNull()
                val stockActual = etStockActual.text.toString().toIntOrNull()
                val idCategoria = etIdCategoria.text.toString().toIntOrNull()
                val idProveedor = etIdProveedor.text.toString().toIntOrNull()
                val estado = etEstado.text.toString().trim()

                if (nombre.isEmpty() || precio == null || stock == null ||
                    stockMinimo == null || stockMaximo == null || stockActual == null ||
                    idCategoria == null || idProveedor == null || estado.isEmpty()
                ) {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val productoActualizado = Producto(
                    idProducto = producto.idProducto,
                    nombre = nombre,
                    precio = precio,
                    stock = stock,
                    stockMinimo = stockMinimo,
                    stockMaximo = stockMaximo,
                    stockActual = stockActual,
                    idCategoria = idCategoria,
                    idProveedor = idProveedor,
                    estado = estado
                )

                RetrofitInstance.api2kotlin.actualizarProducto(
                    producto.idProducto,
                    productoActualizado
                ).enqueue(object : Callback<Void> {
                    override fun onResponse(
                        call: Call<Void>,
                        response: Response<Void>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@ProductosActivity,
                                "Producto actualizado",
                                Toast.LENGTH_SHORT
                            ).show()
                            cargarProductos()
                        } else {
                            Toast.makeText(
                                this@ProductosActivity,
                                "Error al actualizar: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(
                            this@ProductosActivity,
                            "Error al actualizar: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
            .show()
    }

    // DELETE

    private fun confirmarEliminar(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Seguro quieres eliminar '${producto.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                RetrofitInstance.api2kotlin.eliminarProducto(producto.idProducto)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(
                            call: Call<Void>,
                            response: Response<Void>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ProductosActivity,
                                    "Producto eliminado",
                                    Toast.LENGTH_SHORT
                                ).show()
                                cargarProductos()
                            } else {
                                Toast.makeText(
                                    this@ProductosActivity,
                                    "Error al eliminar: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(
                                this@ProductosActivity,
                                "Error al eliminar: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
            .setNegativeButton("Cancelar") { _, _ -> cargarProductos() }
            .show()
    }

    fun volverpag(v: View) {
        finish()
    }
}