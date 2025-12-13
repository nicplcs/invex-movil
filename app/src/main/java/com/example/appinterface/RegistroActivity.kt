package com.example.appinterface

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.Api.Usuario
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var etConfirmarContrasena: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etFechaNacimiento: TextInputEditText
    private lateinit var btnRegistrar: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvIniciarSesion: TextView
    private lateinit var btnVolver: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena)
        etTelefono = findViewById(R.id.etTelefono)
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        progressBar = findViewById(R.id.progressBar)
        tvIniciarSesion = findViewById(R.id.tvIniciarSesion)
        btnVolver = findViewById(R.id.btnVolver)

        // Botón volver
        btnVolver.setOnClickListener {
            finish()
        }

        // Botón registrar
        btnRegistrar.setOnClickListener {
            if (validarCampos()) {
                registrarUsuario()
            }
        }

        // Link iniciar sesión
        tvIniciarSesion.setOnClickListener {
            finish()
        }
    }

    private fun validarCampos(): Boolean {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()
        val confirmar = etConfirmarContrasena.text.toString().trim()

        if (nombre.isEmpty()) {
            etNombre.error = "Ingrese su nombre"
            etNombre.requestFocus()
            return false
        }

        if (correo.isEmpty()) {
            etCorreo.error = "Ingrese su correo"
            etCorreo.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.error = "Ingrese un correo válido"
            etCorreo.requestFocus()
            return false
        }

        if (contrasena.isEmpty()) {
            etContrasena.error = "Ingrese una contraseña"
            etContrasena.requestFocus()
            return false
        }

        if (contrasena.length < 6) {
            etContrasena.error = "Mínimo 6 caracteres"
            etContrasena.requestFocus()
            return false
        }

        if (contrasena != confirmar) {
            etConfirmarContrasena.error = "Las contraseñas no coinciden"
            etConfirmarContrasena.requestFocus()
            return false
        }

        return true
    }

    //  FUNCIÓN AGREGADA: Convertir fecha de dd-MM-yyyy a yyyy-MM-dd
    private fun convertirFechaAFormatoISO(fecha: String?): String? {
        if (fecha.isNullOrEmpty()) return null

        val partes = fecha.split("-")
        if (partes.size == 3) {
            val dia = partes[0]
            val mes = partes[1]
            val anio = partes[2]
            return "$anio-$mes-$dia" // Formato ISO: yyyy-MM-dd
        }
        return fecha
    }

    private fun registrarUsuario() {
        mostrarCargando(true)

        //  MODIFICACIÓN: Convertir la fecha antes de enviarla
        val fechaOriginal = etFechaNacimiento.text.toString().trim().ifEmpty { null }
        val fechaConvertida = convertirFechaAFormatoISO(fechaOriginal)

        val usuario = Usuario(
            id_usuario = 0,
            rol = "empleado",
            nombre = etNombre.text.toString().trim(),
            correo = etCorreo.text.toString().trim(),
            contrasena = etContrasena.text.toString().trim(),
            telefono = etTelefono.text.toString().trim().ifEmpty { null },
            fecha_Nacimiento = fechaConvertida, //
            estado = "0"
        )

        RetrofitInstance.api2kotlin.crearUsuario(usuario).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                mostrarCargando(false)

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@RegistroActivity,
                        "¡Registro exitoso! Espera a que un Administrador te confirme",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@RegistroActivity,
                        "Error al registrar usuario: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                mostrarCargando(false)
                Toast.makeText(
                    this@RegistroActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun mostrarCargando(mostrar: Boolean) {
        progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        btnRegistrar.isEnabled = !mostrar
        etNombre.isEnabled = !mostrar
        etCorreo.isEnabled = !mostrar
        etContrasena.isEnabled = !mostrar
        etConfirmarContrasena.isEnabled = !mostrar
        etTelefono.isEnabled = !mostrar
        etFechaNacimiento.isEnabled = !mostrar
    }
}