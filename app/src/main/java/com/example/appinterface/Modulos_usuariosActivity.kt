package com.example.appinterface

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appinterface.R.id

class Modulos_usuariosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.modulos_usuarios_activity)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestión de Usuarios"

        // Botón regresar
        val btnRegresar = findViewById<LinearLayout>(id.btnRegresar)
        btnRegresar.setOnClickListener {
            finish()
        }

        // Card Usuarios
        val cardUsuarios = findViewById<CardView>(id.cardUsuarios)
        cardUsuarios.setOnClickListener {
            val intent = Intent(this, usuariosActivity::class.java)
            startActivity(intent)
        }

        // Card Proveedores
        val cardProveedores = findViewById<CardView>(id.cardProveedores)
        cardProveedores.setOnClickListener {
            val intent = Intent(this, ProveedoresActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}