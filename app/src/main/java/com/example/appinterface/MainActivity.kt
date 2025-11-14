/*package com.example.appinterface

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.PersonaAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.Api.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar los Spinners (Dropdowns)
        val spinnerRol = findViewById<Spinner>(R.id.spinnerRol)
        val spinnerEstado = findViewById<Spinner>(R.id.spinnerEstado)

        val roles = arrayOf("Administrador", "Empleado")
        val estados = arrayOf("Activo", "Inactivo")

        spinnerRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)

        // Configurar barra inferior de navegación
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_usuarios -> true // ya estamos aquí
                R.id.navigation_productos -> {
                    startActivity(Intent(this, ProductosActivity::class.java))
                    true
                }
                R.id.navigation_movimientos -> {
                    startActivity(Intent(this, MovimientosActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // POST: Crear usuario
    fun crearpersona(v: View) {
        val nombre = findViewById<EditText>(R.id.nombre).text.toString()
        val correo = findViewById<EditText>(R.id.correo).text.toString()
        val contrasena = findViewById<EditText>(R.id.contrasena).text.toString()
        val telefono = findViewById<EditText>(R.id.telefono).text.toString()
        val fechaNacimiento = findViewById<EditText>(R.id.fechaNacimiento).text.toString()

        val spinnerRol = findViewById<Spinner>(R.id.spinnerRol)
        val spinnerEstado = findViewById<Spinner>(R.id.spinnerEstado)

        val rolSeleccionado = spinnerRol.selectedItem.toString()
        val estadoSeleccionado = if (spinnerEstado.selectedItem.toString() == "Activo") "1" else "0"

        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevoUsuario = Usuario(
            id_usuario = 0,
            rol = rolSeleccionado,
            nombre = nombre,
            correo = correo,
            contrasena = contrasena,
            telefono = if (telefono.isEmpty()) null else telefono,
            fecha_Nacimiento = if (fechaNacimiento.isEmpty()) null else fechaNacimiento,
            estado = estadoSeleccionado
        )

        RetrofitInstance.api2kotlin.crearUsuario(nuevoUsuario)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Usuario registrado correctamente", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Error al registrar usuario", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error de conexión con el servidor", Toast.LENGTH_LONG).show()
                }
            })
    }

    // GET: Mostrar usuarios en el RecyclerView
    fun crearmostrarpersonas(v: View) {
        val recyclerView = findViewById<RecyclerView>(R.id.RecyPersonas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        RetrofitInstance.api2kotlin.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.isNotEmpty()) {
                        val adapter = PersonaAdapter(data)
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(this@MainActivity, "No hay usuarios disponibles", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error en la respuesta de la API", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error de conexión con la API", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
*/