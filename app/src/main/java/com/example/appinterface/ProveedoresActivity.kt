package com.example.appinterface

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.ProveedorAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.Api.Proveedor
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProveedoresActivity : AppCompatActivity() {

    private var proveedorEditando: Proveedor? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProveedorAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proveedores)

        val spinnerEstado = findViewById<AutoCompleteTextView>(R.id.spinnerEstado)
        val estados = arrayOf("Activo", "Inactivo")
        spinnerEstado.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estados))

        val cardFormulario = findViewById<CardView>(R.id.cardFormulario)
        val btnToggleForm = findViewById<MaterialButton>(R.id.btnToggleForm)

        btnToggleForm.setOnClickListener {
            if (cardFormulario.visibility == View.GONE) {
                cardFormulario.visibility = View.VISIBLE
                btnToggleForm.text = "- Ocultar Formulario"
            } else {
                cardFormulario.visibility = View.GONE
                btnToggleForm.text = "+ Agregar Nuevo Proveedor"
                limpiarFormulario()
            }
        }

        recyclerView = findViewById(R.id.RecyProveedores)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProveedorAdapter(mutableListOf())
        recyclerView.adapter = adapter

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val proveedor = adapter.getProveedor(position)

                if (direction == ItemTouchHelper.RIGHT) {
                    cargarProveedorEnFormulario(proveedor)
                    adapter.notifyItemChanged(position)
                } else {
                    confirmarEliminar(proveedor)
                }
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        cargarProveedores()
    }

    // GET: Cargar proveedores
    private fun cargarProveedores() {
        RetrofitInstance.api2kotlin.getProveedores().enqueue(object : Callback<List<Proveedor>> {
            override fun onResponse(call: Call<List<Proveedor>>, response: Response<List<Proveedor>>) {
                if (response.isSuccessful && response.body() != null) {
                    adapter.updateList(response.body()!!)
                    Toast.makeText(this@ProveedoresActivity, "Lista actualizada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProveedoresActivity, "Error al cargar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Proveedor>>, t: Throwable) {
                Toast.makeText(this@ProveedoresActivity, "Error conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // POST O PUT
    fun crearProveedor(v: View) {
        val nombre = findViewById<EditText>(R.id.nombre).text.toString()
        val direccion = findViewById<EditText>(R.id.direccion).text.toString()
        val contacto = findViewById<EditText>(R.id.contacto).text.toString()
        val estadoSeleccionado = if (findViewById<AutoCompleteTextView>(R.id.spinnerEstado).text.toString() == "Activo") "1" else "0"

        if (nombre.isEmpty() || contacto.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (proveedorEditando != null) {
            // PUT: Actualizar
            val proveedorActualizado = Proveedor(
                proveedorEditando!!.id, nombre,
                if (direccion.isEmpty()) null else direccion,
                contacto, estadoSeleccionado
            )

            RetrofitInstance.api2kotlin.actualizarProveedor(proveedorEditando!!.id, proveedorActualizado)
                .enqueue(object : Callback<Proveedor> {
                    override fun onResponse(call: Call<Proveedor>, response: Response<Proveedor>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProveedoresActivity, "Proveedor actualizado correctamente", Toast.LENGTH_SHORT).show()
                            limpiarFormulario()
                            findViewById<CardView>(R.id.cardFormulario).visibility = View.GONE
                            findViewById<MaterialButton>(R.id.btnToggleForm).text = "+ Agregar Nuevo Proveedor"
                            cargarProveedores()
                            proveedorEditando = null
                        } else {
                            Toast.makeText(this@ProveedoresActivity, "Error al actualizar: ${response.code()}", Toast.LENGTH_LONG).show()
                            cargarProveedores()
                        }
                    }

                    override fun onFailure(call: Call<Proveedor>, t: Throwable) {
                        Toast.makeText(this@ProveedoresActivity, "Error actualizar: ${t.message}", Toast.LENGTH_LONG).show()
                        cargarProveedores()
                    }
                })
        } else {
            // POST: Crear
            val nuevoProveedor = Proveedor(
                0, nombre,
                if (direccion.isEmpty()) null else direccion,
                contacto, estadoSeleccionado
            )

            RetrofitInstance.api2kotlin.crearProveedor(nuevoProveedor).enqueue(object : Callback<Proveedor> {
                override fun onResponse(call: Call<Proveedor>, response: Response<Proveedor>) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@ProveedoresActivity, "Proveedor registrado correctamente", Toast.LENGTH_SHORT).show()
                        cargarProveedores()
                        limpiarFormulario()
                        findViewById<CardView>(R.id.cardFormulario).visibility = View.GONE
                        findViewById<MaterialButton>(R.id.btnToggleForm).text = "+ Agregar Nuevo Proveedor"
                    } else {
                        Toast.makeText(this@ProveedoresActivity, "Error al crear: ${response.code()}", Toast.LENGTH_SHORT).show()
                        cargarProveedores()
                    }
                }

                override fun onFailure(call: Call<Proveedor>, t: Throwable) {
                    Toast.makeText(this@ProveedoresActivity, "Error crear: ${t.message}", Toast.LENGTH_LONG).show()
                    cargarProveedores()
                }
            })
        }
    }

    // DELETE
    private fun confirmarEliminar(proveedor: Proveedor) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Proveedor")
            .setMessage("¿Eliminar a ${proveedor.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                RetrofitInstance.api2kotlin.eliminarProveedor(proveedor.id)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ProveedoresActivity, "Proveedor eliminado", Toast.LENGTH_SHORT).show()
                                cargarProveedores()
                            } else {
                                Toast.makeText(this@ProveedoresActivity, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                                cargarProveedores()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@ProveedoresActivity, "Error eliminar: ${t.message}", Toast.LENGTH_LONG).show()
                            cargarProveedores()
                        }
                    })
            }
            .setNegativeButton("Cancelar") { _, _ -> cargarProveedores() }
            .show()
    }

    // Cargar en formulario para editar
    private fun cargarProveedorEnFormulario(proveedor: Proveedor) {
        proveedorEditando = proveedor
        findViewById<EditText>(R.id.nombre).setText(proveedor.nombre)
        findViewById<EditText>(R.id.direccion).setText(proveedor.direccion ?: "")
        findViewById<EditText>(R.id.contacto).setText(proveedor.contacto ?: "")
        findViewById<AutoCompleteTextView>(R.id.spinnerEstado)
            .setText(if (proveedor.estado == "1") "Activo" else "Inactivo", false)
        findViewById<CardView>(R.id.cardFormulario).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnToggleForm).text = "- Cancelar Edición"
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Actualizar Proveedor"
    }

    // Limpiar formulario
    private fun limpiarFormulario() {
        findViewById<EditText>(R.id.nombre).setText("")
        findViewById<EditText>(R.id.direccion).setText("")
        findViewById<EditText>(R.id.contacto).setText("")
        findViewById<AutoCompleteTextView>(R.id.spinnerEstado).setText("", false)
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Registrar proveedor"
        proveedorEditando = null
    }
}