package com.example.appinterface

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.ProveedorAdapter
import com.example.appinterface.Api.Proveedor
import com.example.appinterface.Api.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EconsultarProveedoresActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProveedorAdapter
    private lateinit var btnRegresar: LinearLayout
    private lateinit var etBuscar: EditText
    private lateinit var btnLimpiarBusqueda: Button

    private var listaProveedoresOriginal = mutableListOf<Proveedor>()
    private var listaProveedoresFiltrada = mutableListOf<Proveedor>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_econsultar_proveedores)

        // Referencias a elementos de búsqueda
        btnRegresar = findViewById(R.id.btnRegresar)
        etBuscar = findViewById(R.id.etBuscar)
        btnLimpiarBusqueda = findViewById(R.id.btnLimpiarBusqueda)

        setupListeners()

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.RecyProveedores)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProveedorAdapter(mutableListOf())
        recyclerView.adapter = adapter

        // Cargar los proveedores
        cargarProveedores()
    }

    private fun setupListeners() {
        // Botón regresar
        btnRegresar.setOnClickListener {
            finish()
        }

        // Búsqueda en tiempo real
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnLimpiarBusqueda.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                filtrarProveedores()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Limpiar búsqueda
        btnLimpiarBusqueda.setOnClickListener {
            etBuscar.text.clear()
        }
    }

    private fun filtrarProveedores() {
        if (listaProveedoresOriginal.isEmpty()) return

        val textoBusqueda = etBuscar.text.toString().lowercase().trim()

        listaProveedoresFiltrada = if (textoBusqueda.isEmpty()) {
            listaProveedoresOriginal.toMutableList()
        } else {
            listaProveedoresOriginal.filter { proveedor ->
                (proveedor.nombre?.lowercase()?.contains(textoBusqueda) ?: false) ||
                        (proveedor.correo?.lowercase()?.contains(textoBusqueda) ?: false) ||
                        (proveedor.telefono?.contains(textoBusqueda) ?: false) ||
                        (proveedor.direccion?.lowercase()?.contains(textoBusqueda) ?: false) ||
                        (proveedor.id?.toString()?.contains(textoBusqueda) ?: false)
            }.toMutableList()
        }

        actualizarRecyclerView()
    }

    private fun actualizarRecyclerView() {
        adapter.updateList(listaProveedoresFiltrada)

        if (listaProveedoresFiltrada.isEmpty()) {
            Toast.makeText(this, "No se encontraron proveedores con esos filtros", Toast.LENGTH_SHORT).show()
        }
    }

    // GET: Cargar proveedores (SOLO LECTURA)
    private fun cargarProveedores() {
        RetrofitInstance.getApi(this).getProveedores().enqueue(object : Callback<List<Proveedor>> {
            override fun onResponse(call: Call<List<Proveedor>>, response: Response<List<Proveedor>>) {
                if (response.isSuccessful && response.body() != null) {
                    listaProveedoresOriginal = response.body()!!.toMutableList()
                    listaProveedoresFiltrada = listaProveedoresOriginal.toMutableList()
                    adapter.updateList(listaProveedoresFiltrada)
                    Toast.makeText(this@EconsultarProveedoresActivity, "Proveedores cargados", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@EconsultarProveedoresActivity, "Error al cargar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Proveedor>>, t: Throwable) {
                Toast.makeText(this@EconsultarProveedoresActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}