package com.example.appinterface

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
    private lateinit var adapter: MovimientosAdapter

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

        val btnMostrar = findViewById<Button>(R.id.btnMostrarMovimientos)
        btnMostrar.setOnClickListener { view ->
            mostrarmovimientos(view)
        }

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
                Log.d("MovimientosActivity", "Respuesta recibida - Código: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("MovimientosActivity", "Datos: ${data?.size ?: 0} movimientos")

                    if (data != null && data.isNotEmpty()) {
                        val mutableData = data.toMutableList()

                        adapter = MovimientosAdapter(mutableData) { movimiento, position ->
                            mostrarDialogoEliminar(movimiento, position)
                        }

                        recyclerView.adapter = adapter
                        Toast.makeText(this@MovimientosActivity, "${data.size} movimientos cargados", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("MovimientosActivity", "Lista vacía")
                        Toast.makeText(this@MovimientosActivity, "No hay movimientos disponibles", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("MovimientosActivity", "Error: ${response.code()}")
                    Toast.makeText(this@MovimientosActivity, "Error en la respuesta de la API", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Movimiento>>, t: Throwable) {
                Log.e("MovimientosActivity", "Error de conexión: ${t.message}")
                Toast.makeText(this@MovimientosActivity, "Error de conexión con la API: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun mostrarDialogoEliminar(movimiento: Movimiento, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Movimiento")
            .setMessage("¿Estás seguro de que deseas eliminar este movimiento?\n\n${movimiento.descripcion}")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarMovimiento(movimiento, position)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarMovimiento(movimiento: Movimiento, position: Int) {
        Log.d("MovimientosActivity", "🗑Eliminando movimiento ID: ${movimiento.id_movimiento}")

        RetrofitInstance.api2kotlin.deleteMovimiento(movimiento).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("MovimientosActivity", "Movimiento eliminado exitosamente")

                    adapter.removeItem(position)

                    Toast.makeText(this@MovimientosActivity, "Movimiento eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("MovimientosActivity", "Error al eliminar: ${response.code()}")
                    Toast.makeText(this@MovimientosActivity, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("MovimientosActivity", "Error de conexión: ${t.message}")
                Toast.makeText(this@MovimientosActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}