package com.example.appinterface

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class Modulos_productosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modulos_productos_activity)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestión de Productos"

        // ENTRADA de productos
        findViewById<CardView>(R.id.cardEntrada).setOnClickListener {
            startActivity(Intent(this, EntradaProductosActivity::class.java))
        }

        // SALIDA de productos
        findViewById<CardView>(R.id.cardSalida).setOnClickListener {
            startActivity(Intent(this, SalidaProductosActivity::class.java))
        }

        // REGISTRAR producto
        findViewById<CardView>(R.id.cardRegistrar).setOnClickListener {
            startActivity(Intent(this, ProductosActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
