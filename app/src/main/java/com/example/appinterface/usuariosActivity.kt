package com.example.appinterface

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.UsuarioAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.Api.Usuario
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class usuariosActivity : AppCompatActivity() {

    private var usuarioEditando: Usuario? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsuarioAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios)


        val spinnerRol = findViewById<AutoCompleteTextView>(R.id.spinnerRol)
        val spinnerEstado = findViewById<AutoCompleteTextView>(R.id.spinnerEstado)
        val roles = arrayOf("Administrador", "Empleado")
        val estados = arrayOf("Activo", "Inactivo")
        spinnerRol.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles))
        spinnerEstado.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estados))

        val cardFormulario = findViewById<CardView>(R.id.cardFormulario)
        val btnToggleForm = findViewById<MaterialButton>(R.id.btnToggleForm)

        btnToggleForm.setOnClickListener {
            if (cardFormulario.visibility == View.GONE) {
                cardFormulario.visibility = View.VISIBLE
                btnToggleForm.text = "- Ocultar Formulario"
            } else {
                cardFormulario.visibility = View.GONE
                btnToggleForm.text = "+ Agregar Nuevo Usuario"
                limpiarFormulario()
            }
        }

        recyclerView = findViewById(R.id.RecyPersonas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UsuarioAdapter(mutableListOf())
        recyclerView.adapter = adapter

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val usuario = adapter.getUsuario(position)

                if (direction == ItemTouchHelper.RIGHT) {
                    cargarUsuarioEnFormulario(usuario)
                    adapter.notifyItemChanged(position)
                } else {
                    confirmarEliminar(usuario)
                }
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        cargarUsuarios()


       /* val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_usuarios -> true
                R.id.navigation_productos -> {
                    startActivity(Intent(this, ProductosActivity::class.java))
                    true
                }
                R.id.navgation_movimientos -> {
                    startActivity(Intent(this, MovimientosActivity::class.java))
                    true
                }
                else -> false
            }
        }*/
    }


    // GET: Cargar usuarios
    private fun cargarUsuarios() {
        RetrofitInstance.api2kotlin.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful && response.body() != null) {
                    adapter.updateList(response.body()!!)
                    Toast.makeText(this@usuariosActivity, "Lista actualizada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@usuariosActivity, "Error al cargar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Toast.makeText(this@usuariosActivity, "Error conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // POST O GUARDAR USUARIOS
    fun crearpersona(v: View) {
        val nombre = findViewById<EditText>(R.id.nombre).text.toString()
        val correo = findViewById<EditText>(R.id.correo).text.toString()
        val contrasena = findViewById<EditText>(R.id.contrasena).text.toString()
        val telefono = findViewById<EditText>(R.id.telefono).text.toString()
        val fechaNacimiento = findViewById<EditText>(R.id.fechaNacimiento).text.toString()
        val rolSeleccionado = findViewById<AutoCompleteTextView>(R.id.spinnerRol).text.toString()
        val estadoSeleccionado = if (findViewById<AutoCompleteTextView>(R.id.spinnerEstado).text.toString() == "Activo") "1" else "0"

        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (usuarioEditando != null) {

            // PUT:
            val usuarioActualizado = Usuario(
                usuarioEditando!!.id_usuario, rolSeleccionado, nombre, correo,
                contrasena, if (telefono.isEmpty()) null else telefono,
                if (fechaNacimiento.isEmpty()) null else fechaNacimiento, estadoSeleccionado
            )

            RetrofitInstance.api2kotlin.actualizarUsuario(usuarioEditando!!.id_usuario, usuarioActualizado)
                .enqueue(object : Callback<Usuario> {
                    override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@usuariosActivity, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()
                            limpiarFormulario()
                            findViewById<CardView>(R.id.cardFormulario).visibility = View.GONE
                            findViewById<MaterialButton>(R.id.btnToggleForm).text = "+ Agregar Nuevo Usuario"
                            cargarUsuarios()
                            usuarioEditando = null
                        } else {
                            Toast.makeText(this@usuariosActivity, "Error al actualizar: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<Usuario>, t: Throwable) {
                        Toast.makeText(this@usuariosActivity, "Error actualizar: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        } else {

            // POST:
            val nuevoUsuario = Usuario(
                0, rolSeleccionado, nombre, correo, contrasena,
                if (telefono.isEmpty()) null else telefono,
                if (fechaNacimiento.isEmpty()) null else fechaNacimiento, estadoSeleccionado
            )

            RetrofitInstance.api2kotlin.crearUsuario(nuevoUsuario).enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@usuariosActivity, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                        adapter.addUsuario(response.body()!!)
                        limpiarFormulario()
                        findViewById<CardView>(R.id.cardFormulario).visibility = View.GONE
                        findViewById<MaterialButton>(R.id.btnToggleForm).text = "+ Agregar Nuevo Usuario"
                    } else {
                        Toast.makeText(this@usuariosActivity, "Error al crear: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    Toast.makeText(this@usuariosActivity, "Error crear: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    // DELETE:
    private fun confirmarEliminar(usuario: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Eliminar a ${usuario.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                RetrofitInstance.api2kotlin.eliminarUsuario(usuario.id_usuario)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@usuariosActivity, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                                cargarUsuarios()
                            } else {
                                Toast.makeText(this@usuariosActivity, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@usuariosActivity, "Error eliminar: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
            .setNegativeButton("Cancelar") { _, _ -> cargarUsuarios() }
            .show()
    }

    // FORM PARA EDITAR
    private fun cargarUsuarioEnFormulario(usuario: Usuario) {
        usuarioEditando = usuario
        findViewById<EditText>(R.id.nombre).setText(usuario.nombre)
        findViewById<EditText>(R.id.correo).setText(usuario.correo)
        findViewById<EditText>(R.id.contrasena).setText(usuario.contrasena)
        findViewById<EditText>(R.id.telefono).setText(usuario.telefono ?: "")
        findViewById<EditText>(R.id.fechaNacimiento).setText(usuario.fecha_Nacimiento ?: "")
        findViewById<AutoCompleteTextView>(R.id.spinnerRol).setText(usuario.rol, false)
        findViewById<AutoCompleteTextView>(R.id.spinnerEstado)
            .setText(if (usuario.estado == "1") "Activo" else "Inactivo", false)
        findViewById<CardView>(R.id.cardFormulario).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnToggleForm).text = "- Cancelar Edición"
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Actualizar Usuario"
    }

    // Limpiar formulario
    private fun limpiarFormulario() {
        findViewById<EditText>(R.id.nombre).setText("")
        findViewById<EditText>(R.id.correo).setText("")
        findViewById<EditText>(R.id.contrasena).setText("")
        findViewById<EditText>(R.id.telefono).setText("")
        findViewById<EditText>(R.id.fechaNacimiento).setText("")
        findViewById<AutoCompleteTextView>(R.id.spinnerRol).setText("", false)
        findViewById<AutoCompleteTextView>(R.id.spinnerEstado).setText("", false)
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Registrar usuario"
        usuarioEditando = null
    }
}
