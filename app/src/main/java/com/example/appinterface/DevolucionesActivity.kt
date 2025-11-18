package com.example.appinterface

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.DevolucionesAdapter
import com.example.appinterface.Api.Devolucion
import com.example.appinterface.Api.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DevolucionesActivity : AppCompatActivity(){

    private lateinit var recyclerView: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_devoluciones)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerDevoluciones)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnMostrar = findViewById<Button>(R.id.btnMostrarDevoluciones)
        btnMostrar.setOnClickListener { view ->
            mostrardevoluciones(view)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestión de Devoluciones"
    }

    fun mostrardevoluciones(v: View) {
        recyclerView.visibility = View.VISIBLE

        RetrofitInstance.api2kotlin.getDevoluciones().enqueue(object : Callback<List<Devolucion>> {
            override fun onResponse(call: Call<List<Devolucion>>, response: Response<List<Devolucion>>) {
                Log.d("DevolucionesActivity", "Respuesta recibida - Código: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("DevolucionesActivity", "Datos: ${data?.size ?: 0} devoluciones")

                    if (data != null && data.isNotEmpty()) {

                        val adapter = DevolucionesAdapter(data)
                        recyclerView.adapter = adapter
                        Toast.makeText(this@DevolucionesActivity, "${data.size} devoluciones cargadss", Toast.LENGTH_SHORT).show()
                        } else {
                        Log.e("DevolucionesActivity", "Lista vacía")
                        Toast.makeText(this@DevolucionesActivity, "No hay devoluciones disponibles", Toast.LENGTH_SHORT).show()
                        }
                } else {
                        Log.e("DevolucionesActivity", "Error: ${response.code()}")
                        Toast.makeText(this@DevolucionesActivity, "Error en la respuesta de la API", Toast.LENGTH_SHORT).show()
                        }
                }
            override fun onFailure(call: Call<List<Devolucion>>, t: Throwable) {
                Log.e("DevolucionesActivity", "Error de conexión: ${t.message}")
                Toast.makeText(this@DevolucionesActivity, "Error de conexión con la API: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
 }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
