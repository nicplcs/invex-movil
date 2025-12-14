package com.example.appinterface

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appinterface.Api.LoginRequest
import com.example.appinterface.Api.LoginResponse
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvIrRegistro: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Inicializar SessionManager
        sessionManager = SessionManager(this)

        // 2. Verificar si ya hay sesión activa
        if (sessionManager.isLoggedIn()) {
            irAMainActivity()
            return
        }

        // 3. Conectar las vistas del XML con el código
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        tvIrRegistro = findViewById(R.id.tvIrRegistro)

        // 4. Configurar el click del botón de login
        btnLogin.setOnClickListener {
            if (validarCampos()) {
                realizarLogin()
            }
        }

        // 5. Configurar el click del TextView para ir al registro
        tvIrRegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validarCampos(): Boolean {
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        // Validar correo vacío
        if (correo.isEmpty()) {
            etCorreo.error = "Ingrese su correo"
            etCorreo.requestFocus()
            return false
        }

        // Validar formato de correo
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.error = "Ingrese un correo válido"
            etCorreo.requestFocus()
            return false
        }

        // Validar contraseña vacía
        if (contrasena.isEmpty()) {
            etContrasena.error = "Ingrese su contraseña"
            etContrasena.requestFocus()
            return false
        }

        return true
    }

    private fun realizarLogin() {
        // Mostrar loading
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        // Crear el request
        val loginRequest = LoginRequest(correo, contrasena)

        // Hacer la petición al backend
        RetrofitInstance.api2kotlin.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                // Ocultar loading
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                if (response.isSuccessful) {
                    // Login exitoso
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Guardar los datos en SessionManager
                        sessionManager.guardarSesion(
                            loginResponse.token,
                            loginResponse.nombre,
                            loginResponse.correo,
                            loginResponse.rol
                        )

                        // Mostrar mensaje de bienvenida
                        Toast.makeText(
                            this@LoginActivity,
                            "Bienvenido ${loginResponse.nombre}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navegar según el rol
                        irAMainActivity()
                    }
                } else {
                    // Login fallido
                    Toast.makeText(
                        this@LoginActivity,
                        "Credenciales incorrectas",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Error de conexión
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                Toast.makeText(
                    this@LoginActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun irAMainActivity() {
        val rol = sessionManager.getRol()

        val intent = when (rol) {
            "admin", "administrador" -> {
                // Si es admin, va a MainActivity con todos los módulos
                Intent(this, MainActivityy::class.java)
            }
            "usuario", "empleado" -> {
                // Si es empleado/usuario, va a EmpleadoActivity (módulos limitados)
                Intent(this, EmpleadoActivity::class.java)
            }
            else -> {
                // Si el rol es desconocido, por defecto va a empleado
                Toast.makeText(
                    this,
                    "Rol desconocido, accediendo como empleado",
                    Toast.LENGTH_SHORT
                ).show()
                Intent(this, EmpleadoActivity::class.java)
            }
        }

        startActivity(intent)
        finish() // Cierra LoginActivity para que no pueda volver con el botón atrás
    }
}