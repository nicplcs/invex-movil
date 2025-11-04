package com.example.appinterface

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.MovimientosAdapter
import com.example.appinterface.Api.Movimiento
import com.example.appinterface.Api.RetrofitInstance
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovimientosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_movimientos)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerMovimientos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_movimientos

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_usuarios -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.navigation_productos -> {
                    startActivity(Intent(this, ProductosActivity::class.java))
                    true
                }
                R.id.navigation_movimientos -> true
                else -> false
            }
        }
    }

    fun mostrarmovimientos(v: View) {
        recyclerView.visibility = View.VISIBLE
        RetrofitInstance.api2kotlin.getMovimientos().enqueue(object : Callback<List<Movimiento>> {
            override fun onResponse(call: Call<List<Movimiento>>, response: Response<List<Movimiento>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.isNotEmpty()) {
                        recyclerView.adapter = MovimientosAdapter(data)
                    } else {
                        Toast.makeText(this@MovimientosActivity, "No hay movimientos disponibles", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MovimientosActivity, "Error en la respuesta de la API", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Movimiento>>, t: Throwable) {
                Toast.makeText(this@MovimientosActivity, "Error de conexión con la API", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
