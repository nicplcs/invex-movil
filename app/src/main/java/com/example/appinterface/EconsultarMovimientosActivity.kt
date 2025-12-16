package com.example.appinterface

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EconsultarMovimientosActivity : AppCompatActivity() {

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
                setContentView(R.layout.activity_econsultar_movimientos)

        recyclerView = findViewById(R.id.recyclerMovimientos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnRegresar = findViewById(R.id.btnRegresar)
        etBuscar = findViewById(R.id.etBuscar)
        btnLimpiarBusqueda = findViewById(R.id.btnLimpiarBusqueda)

        setupListeners()
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

    private fun cargarMovimientos() {

        RetrofitInstance.getApi(this).getMovimientos()
            .enqueue(object : Callback<List<Movimiento>> {

                override fun onResponse(
                    call: Call<List<Movimiento>>,
                    response: Response<List<Movimiento>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()

                        if (!data.isNullOrEmpty()) {

                            listaMovimientosOriginal = data.toMutableList()
                            listaMovimientosFiltrada = data.toMutableList()

                            adapter = MovimientosAdapter(listaMovimientosFiltrada){ _, _ ->
                            }
                            recyclerView.adapter = adapter

                            recyclerView.post {
                                for (i in 0 until recyclerView.childCount) {
                                    val itemView = recyclerView.getChildAt(i)
                                    val btnEliminar = itemView.findViewById<View>(R.id.btnEliminar)
                                    btnEliminar?.visibility = View.GONE
                                }
                            }

                        } else {
                            Toast.makeText(
                                this@EconsultarMovimientosActivity,
                                "No hay movimientos disponibles",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@EconsultarMovimientosActivity,
                            "Error en la respuesta del servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Movimiento>>, t: Throwable) {
                    Log.e("EmpleadoMovimientos", t.message ?: "Error")
                    Toast.makeText(
                        this@EconsultarMovimientosActivity,
                        "Error de conexión",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun filtrarMovimientos() {

        val texto = etBuscar.text.toString().lowercase().trim()

        listaMovimientosFiltrada.clear()

        if (texto.isEmpty()) {
            listaMovimientosFiltrada.addAll(listaMovimientosOriginal)
        } else {
            listaMovimientosFiltrada.addAll(
                listaMovimientosOriginal.filter { movimiento ->
                    (movimiento.descripcion?.lowercase()?.contains(texto) ?: false) ||
                            (movimiento.usuario_responsable?.lowercase()?.contains(texto) ?: false) ||
                            (movimiento.tipo?.lowercase()?.contains(texto) ?: false) ||
                            (movimiento.accion?.lowercase()?.contains(texto) ?: false)
                }
            )
        }

        adapter.notifyDataSetChanged()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
