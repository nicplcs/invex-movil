package com.example.appinterface

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
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
    private lateinit var btnRegresar: LinearLayout
    private lateinit var etBuscar: EditText
    private lateinit var btnLimpiarBusqueda: Button

    private var listaMovimientosOriginal = mutableListOf<Movimiento>()
    private var listaMovimientosFiltrada = mutableListOf<Movimiento>()

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

        btnRegresar = findViewById<LinearLayout>(R.id.btnRegresar)
        etBuscar = findViewById(R.id.etBuscar)
        btnLimpiarBusqueda = findViewById(R.id.btnLimpiarBusqueda)

        setupListeners()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestión de Movimientos"

        cargarMovimientos()
    }

    private fun setupListeners() {

        btnRegresar.setOnClickListener {
            finish()
        }
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnLimpiarBusqueda.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                filtrarMovimientos()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        btnLimpiarBusqueda.setOnClickListener {
            etBuscar.text.clear()
        }
    }


    fun cargarMovimientos() {

        recyclerView.visibility = View.VISIBLE

        RetrofitInstance.getApi(this).getMovimientos().enqueue(object : Callback<List<Movimiento>> {
            override fun onResponse(call: Call<List<Movimiento>>, response: Response<List<Movimiento>>) {
                Log.d("MovimientosActivity", "Respuesta recibida - Código: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("MovimientosActivity", "Datos: ${data?.size ?: 0} movimientos")

                    if (data != null && data.isNotEmpty()) {

                        listaMovimientosOriginal = data.toMutableList()
                        listaMovimientosFiltrada = listaMovimientosOriginal.toMutableList()

                        adapter = MovimientosAdapter(listaMovimientosFiltrada) { movimiento, position ->
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

    private fun filtrarMovimientos() {
        if (listaMovimientosOriginal.isEmpty()) return

        val textoBusqueda = etBuscar.text.toString().lowercase().trim()

        listaMovimientosFiltrada = if (textoBusqueda.isEmpty()) {
            listaMovimientosOriginal.toMutableList()
        } else {
            listaMovimientosOriginal.filter { movimiento ->
                (movimiento.descripcion?.lowercase()?.contains(textoBusqueda) ?: false) ||
                        (movimiento.usuario_responsable?.lowercase()?.contains(textoBusqueda) ?: false) ||
                        (movimiento.id_movimiento?.toString()?.contains(textoBusqueda) ?: false) ||
                        (movimiento.id_producto?.toString()?.contains(textoBusqueda) ?: false) ||
                        (movimiento.tipo?.lowercase()?.contains(textoBusqueda) ?: false) ||
                        (movimiento.accion?.lowercase()?.contains(textoBusqueda) ?: false)
            }.toMutableList()
        }

        actualizarRecyclerView()
    }

    private fun actualizarRecyclerView() {
        adapter = MovimientosAdapter(listaMovimientosFiltrada) { movimiento, position ->
            mostrarDialogoEliminar(movimiento, position)
        }
        recyclerView.adapter = adapter

        if (listaMovimientosFiltrada.isEmpty()) {
            Toast.makeText(this, "No se encontraron movimientos con esos filtros", Toast.LENGTH_SHORT).show()
        }
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

        RetrofitInstance.getApi(this).deleteMovimiento(movimiento).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("MovimientosActivity", "Movimiento eliminado exitosamente")

                    listaMovimientosOriginal.removeAll { it.id_movimiento == movimiento.id_movimiento }
                    listaMovimientosFiltrada.removeAt(position)

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
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}