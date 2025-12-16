package com.example.appinterface

import android.os.Bundle
import android.widget.Toast
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.EmpleadoProductosAdapter
import com.example.appinterface.Api.Producto
import com.example.appinterface.Api.RetrofitInstance
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EconsultarProductosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmpleadoProductosAdapter
    private lateinit var searchView: SearchView
    private var listaCompleta = listOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_econsultar_productos)

        initializeViews()
        setupRecyclerView()
        setupSearchView()
        cargarProductos()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerEmpleadoProductos)
        searchView = findViewById(R.id.searchViewProductos)

        findViewById<MaterialCardView>(R.id.btnRegresar).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EmpleadoProductosAdapter(mutableListOf())
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarProductos(newText ?: "")
                return true
            }
        })
    }

    private fun cargarProductos() {
        RetrofitInstance.getApi(this).getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful && response.body() != null) {
                    listaCompleta = response.body()!!
                    adapter.updateList(listaCompleta)
                } else {
                    Toast.makeText(
                        this@EconsultarProductosActivity,
                        "Error al cargar productos: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(
                    this@EconsultarProductosActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun filtrarProductos(texto: String) {
        val listaFiltrada = if (texto.isEmpty()) {
            listaCompleta
        } else {
            listaCompleta.filter { producto ->
                producto.nombre.contains(texto, ignoreCase = true) ||
                        producto.idProducto.toString().contains(texto)
            }
        }
        adapter.updateList(listaFiltrada)
    }
}