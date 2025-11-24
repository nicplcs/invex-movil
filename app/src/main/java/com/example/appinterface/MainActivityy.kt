package com.example.appinterface

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import android.widget.Toast

class MainActivityy : AppCompatActivity() {

    private lateinit var cardMovimientos: MaterialCardView
    private lateinit var cardProductos: MaterialCardView
    private lateinit var cardUsuarios: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainn)

        initializeViews()
        setupClickListeners()
        animateCards()
    }

    private fun initializeViews() {
        cardMovimientos = findViewById(R.id.cardMovimientos)
        cardProductos = findViewById(R.id.cardProductos)
        cardUsuarios = findViewById(R.id.cardUsuarios)
    }

    private fun setupClickListeners() {
        cardMovimientos.setOnClickListener {
            animateCardClick(it as MaterialCardView)
            Toast.makeText(this, "Gestión de Movimientos", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, Modulos_movimientosActivity::class.java)
            startActivity(intent)
        }

        cardProductos.setOnClickListener {
            animateCardClick(it as MaterialCardView)
            Toast.makeText(this, "Gestión de Productos", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, Modulos_productosActivity::class.java)
            startActivity(intent)
        }


        cardUsuarios.setOnClickListener {
            animateCardClick(it as MaterialCardView)
            Toast.makeText(this, "Gestión de Usuarios", Toast.LENGTH_SHORT).show()

            val intent = Intent(this,  Modulos_usuariosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun animateCards() {
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeIn.duration = 500

        cardMovimientos.startAnimation(fadeIn)

        cardProductos.postDelayed({
            cardProductos.startAnimation(fadeIn)
        }, 150)

        cardUsuarios.postDelayed({
            cardUsuarios.startAnimation(fadeIn)
        }, 300)
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

    override fun onBackPressed() {
        Toast.makeText(this, "Presiona nuevamente para salir", Toast.LENGTH_SHORT).show()
        super.onBackPressed()
    }
}