package com.example.appinterface

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import android.widget.Toast
import android.widget.Button
import com.example.appinterface.EregistrarSalidaActivity
import com.example.appinterface.utils.SessionManager

class EmpleadoActivity : AppCompatActivity() {

    private lateinit var cardConsultarProveedores: MaterialCardView
    private lateinit var cardConsultarProductos: MaterialCardView
    private lateinit var cardRegistrarSalida: MaterialCardView
    private lateinit var cardRegistrarDevolucion: MaterialCardView
    private lateinit var cardConsultarMovimientos: MaterialCardView
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empleado)

        initializeViews()
        setupClickListeners()
        animateCards()
    }

    private fun initializeViews() {
        cardConsultarProveedores = findViewById(R.id.cardConsultarProveedores)
        cardConsultarProductos = findViewById(R.id.cardConsultarProductos)
        cardRegistrarSalida = findViewById(R.id.cardRegistrarSalida)
        cardRegistrarDevolucion = findViewById(R.id.cardRegistrarDevolucion)
        cardConsultarMovimientos = findViewById(R.id.cardConsultarMovimientos)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
    }

    private fun setupClickListeners() {
        // 1. Consultar Proveedores
        cardConsultarProveedores.setOnClickListener {
            animateCardClick(it as MaterialCardView)
            Toast.makeText(this, "Consultar Proveedores", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, EconsultarProveedoresActivity::class.java)
            startActivity(intent)
        }

        // 2. Consultar Productos
        cardConsultarProductos.setOnClickListener {
            animateCardClick(it as MaterialCardView)
            Toast.makeText(this, "Consultar Productos", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, EconsultarProductosActivity::class.java)
            startActivity(intent)
        }

        // 3. Registrar Salida
        cardRegistrarSalida.setOnClickListener {
            animateCardClick(it as MaterialCardView)
            Toast.makeText(this, "Registrar Salida", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, EregistrarSalidaActivity::class.java)
            startActivity(intent)
        }

        // 4. Registrar Devolución
        cardRegistrarDevolucion.setOnClickListener {
            animateCardClick(it as MaterialCardView)
            Toast.makeText(this, "Registrar Devolución", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, EregistrarDevolucionActivity::class.java)
            startActivity(intent)
        }

        // 5. Consultar Movimientos
        cardConsultarMovimientos.setOnClickListener {
            animateCardClick(it as MaterialCardView)
            Toast.makeText(this, "Consultar Movimientos", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, EconsultarMovimientosActivity::class.java)
            startActivity(intent)
        }

        // Botón cerrar sesión
        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun animateCards() {
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeIn.duration = 500

        cardConsultarProveedores.startAnimation(fadeIn)

        cardConsultarProductos.postDelayed({
            cardConsultarProductos.startAnimation(fadeIn)
        }, 150)

        cardRegistrarSalida.postDelayed({
            cardRegistrarSalida.startAnimation(fadeIn)
        }, 300)

        cardRegistrarDevolucion.postDelayed({
            cardRegistrarDevolucion.startAnimation(fadeIn)
        }, 450)

        cardConsultarMovimientos.postDelayed({
            cardConsultarMovimientos.startAnimation(fadeIn)
        }, 600)
    }

    private fun animateCardClick(card: MaterialCardView) {
        card.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                card.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun cerrarSesion() {
        val sessionManager = SessionManager(this)
        sessionManager.cerrarSesion()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Presiona nuevamente para salir", Toast.LENGTH_SHORT).show()
        super.onBackPressed()
    }
}