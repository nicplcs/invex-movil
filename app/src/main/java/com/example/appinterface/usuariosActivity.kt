package com.example.appinterface

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class usuariosActivity : AppCompatActivity() {

    private var usuarioEditando: Usuario? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsuarioAdapter
    private lateinit var btnRegresar: LinearLayout
    private lateinit var etBuscar: EditText
    private lateinit var btnLimpiarBusqueda: Button

    private var listaUsuariosOriginal = mutableListOf<Usuario>()
    private var listaUsuariosFiltrada = mutableListOf<Usuario>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios)

        // Configurar el spinner de rol
        val spinnerRol = findViewById<Spinner>(R.id.spinnerRol)
        val roles = arrayOf("Administrador", "Empleado")
        val adapterRol = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapterRol.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRol.adapter = adapterRol

        // Configurar el spinner de estado
        val spinnerEstado = findViewById<Spinner>(R.id.spinnerEstado)
        val estados = arrayOf("activo", "inactivo")
        val adapterEstado = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = adapterEstado

        val cardFormulario = findViewById<CardView>(R.id.cardFormulario)
        val btnToggleForm = findViewById<MaterialButton>(R.id.btnToggleForm)

        // Referencias a elementos de búsqueda
        btnRegresar = findViewById(R.id.btnRegresar)
        etBuscar = findViewById(R.id.etBuscar)
        btnLimpiarBusqueda = findViewById(R.id.btnLimpiarBusqueda)

        setupListeners()

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

        recyclerView = findViewById(R.id.RecyUsuarios)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UsuarioAdapter(mutableListOf())
        recyclerView.adapter = adapter

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val usuario = listaUsuariosFiltrada[position]

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
    }

    private fun setupListeners() {
        btnRegresar.setOnClickListener {
            finish()
        }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnLimpiarBusqueda.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                filtrarUsuarios()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnLimpiarBusqueda.setOnClickListener {
            etBuscar.text.clear()
        }
    }

    private fun filtrarUsuarios() {
        if (listaUsuariosOriginal.isEmpty()) return

        val textoBusqueda = etBuscar.text.toString().lowercase().trim()

        listaUsuariosFiltrada = if (textoBusqueda.isEmpty()) {
            listaUsuariosOriginal.toMutableList()
        } else {
            listaUsuariosOriginal.filter { usuario ->
                (usuario.nombre?.lowercase()?.contains(textoBusqueda) ?: false) ||
                        (usuario.correo?.lowercase()?.contains(textoBusqueda) ?: false) ||
                        (usuario.rol?.lowercase()?.contains(textoBusqueda) ?: false) ||
                        (usuario.id_usuario?.toString()?.contains(textoBusqueda) ?: false)
            }.toMutableList()
        }

        actualizarRecyclerView()
    }

    private fun actualizarRecyclerView() {
        adapter.updateList(listaUsuariosFiltrada)

        if (listaUsuariosFiltrada.isEmpty()) {
            Toast.makeText(this, "No se encontraron usuarios con esos filtros", Toast.LENGTH_SHORT).show()
        }
    }

    // GET: Cargar usuarios
    private fun cargarUsuarios() {
        RetrofitInstance.getApi(this).getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful && response.body() != null) {
                    listaUsuariosOriginal = response.body()!!.toMutableList()
                    listaUsuariosFiltrada = listaUsuariosOriginal.toMutableList()
                    adapter.updateList(listaUsuariosFiltrada)
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

    // POST O PUT
    fun crearUsuario(@Suppress("UNUSED_PARAMETER") v: View) {
        val nombre = findViewById<EditText>(R.id.nombre).text.toString()
        val correo = findViewById<EditText>(R.id.correo).text.toString()
        val contrasena = findViewById<EditText>(R.id.contrasena).text.toString()
        val spinnerRol = findViewById<Spinner>(R.id.spinnerRol)
        val rolSeleccionado = spinnerRol.selectedItem.toString()
        val spinnerEstado = findViewById<Spinner>(R.id.spinnerEstado)
        val estadoSeleccionado = if (spinnerEstado.selectedItemPosition == 0) "1" else "0"

        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (usuarioEditando != null) {
            // PUT: Actualizar
            val usuarioActualizado = Usuario(
                id_usuario = usuarioEditando!!.id_usuario,
                rol = rolSeleccionado,
                nombre = nombre,
                correo = correo,
                contrasena = contrasena,
                telefono = usuarioEditando!!.telefono,
                fecha_Nacimiento = usuarioEditando!!.fecha_Nacimiento,
                estado = estadoSeleccionado
            )

            RetrofitInstance.getApi(this).actualizarUsuario(usuarioEditando!!.id_usuario, usuarioActualizado)
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
                            cargarUsuarios()
                        }
                    }

                    override fun onFailure(call: Call<Usuario>, t: Throwable) {
                        Toast.makeText(this@usuariosActivity, "Error actualizar: ${t.message}", Toast.LENGTH_LONG).show()
                        cargarUsuarios()
                    }
                })
        } else {
            // POST: Crear
            val nuevoUsuario = Usuario(
                id_usuario = 0,
                rol = rolSeleccionado,
                nombre = nombre,
                correo = correo,
                contrasena = contrasena,
                telefono = null,
                fecha_Nacimiento = null,
                estado = estadoSeleccionado
            )

            RetrofitInstance.getApi(this).crearUsuario(nuevoUsuario).enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@usuariosActivity, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                        cargarUsuarios()
                        limpiarFormulario()
                        findViewById<CardView>(R.id.cardFormulario).visibility = View.GONE
                        findViewById<MaterialButton>(R.id.btnToggleForm).text = "+ Agregar Nuevo Usuario"
                    } else {
                        Toast.makeText(this@usuariosActivity, "Error al crear: ${response.code()}", Toast.LENGTH_SHORT).show()
                        cargarUsuarios()
                    }
                }

                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    Toast.makeText(this@usuariosActivity, "Error crear: ${t.message}", Toast.LENGTH_LONG).show()
                    cargarUsuarios()
                }
            })
        }
    }

    // DELETE
    private fun confirmarEliminar(usuario: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Eliminar a ${usuario.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                RetrofitInstance.getApi(this).eliminarUsuario(usuario.id_usuario)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@usuariosActivity, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                                cargarUsuarios()
                            } else {
                                Toast.makeText(this@usuariosActivity, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                                cargarUsuarios()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@usuariosActivity, "Error eliminar: ${t.message}", Toast.LENGTH_LONG).show()
                            cargarUsuarios()
                        }
                    })
            }
            .setNegativeButton("Cancelar") { _, _ -> cargarUsuarios() }
            .show()
    }

    // Cargar en formulario para editar
    private fun cargarUsuarioEnFormulario(usuario: Usuario) {
        usuarioEditando = usuario
        findViewById<EditText>(R.id.nombre).setText(usuario.nombre)
        findViewById<EditText>(R.id.correo).setText(usuario.correo)
        findViewById<EditText>(R.id.contrasena).setText(usuario.contrasena)

        val spinnerRol = findViewById<Spinner>(R.id.spinnerRol)
        val roles = arrayOf("Administrador", "Empleado")
        val posicionRol = roles.indexOf(usuario.rol)
        if (posicionRol >= 0) spinnerRol.setSelection(posicionRol)

        val spinnerEstado = findViewById<Spinner>(R.id.spinnerEstado)
        val estados = arrayOf("1", "0")
        val posicionEstado = estados.indexOf(usuario.estado)
        if (posicionEstado >= 0) spinnerEstado.setSelection(posicionEstado)

        findViewById<CardView>(R.id.cardFormulario).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnToggleForm).text = "- Cancelar Edición"
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Actualizar Usuario"
    }

    // Limpiar formulario
    private fun limpiarFormulario() {
        findViewById<EditText>(R.id.nombre).setText("")
        findViewById<EditText>(R.id.correo).setText("")
        findViewById<EditText>(R.id.contrasena).setText("")
        findViewById<Spinner>(R.id.spinnerRol).setSelection(0)
        findViewById<Spinner>(R.id.spinnerEstado).setSelection(0)
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Registrar usuario"
        usuarioEditando = null
    }
}