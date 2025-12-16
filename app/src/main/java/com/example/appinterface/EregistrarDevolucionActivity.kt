package com.example.appinterface

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.DevolucionesAdapter
import com.example.appinterface.Api.Devolucion
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.DevolucionesActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EregistrarDevolucionActivity : AppCompatActivity() {

    private var devolucionEditando: Devolucion? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DevolucionesAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoData: TextView
    private lateinit var btnRegresar: LinearLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devoluciones)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerDevoluciones)
        progressBar = findViewById(R.id.progressBar)
        tvNoData = findViewById(R.id.tvNoData)
        btnRegresar = findViewById<LinearLayout>(R.id.btnRegresar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DevolucionesAdapter(mutableListOf())
        recyclerView.adapter = adapter
        val btnRegresar = findViewById<LinearLayout>(R.id.btnRegresar)
        btnRegresar.setOnClickListener {
            finish()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestión de Devoluciones"

        val cardFormulario = findViewById<CardView>(R.id.cardFormulario)
        val btnToggleForm = findViewById<MaterialButton>(R.id.btnToggleForm)

        btnToggleForm.setOnClickListener {
            if (cardFormulario.visibility == View.GONE) {
                cardFormulario.visibility = View.VISIBLE
                btnToggleForm.text = "Ocultar Formulario"
            } else {
                cardFormulario.visibility = View.GONE
                btnToggleForm.text = "Agregar Nueva Devolución"
                limpiarFormulario()
            }
        }

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val devolucion = adapter.getDevolucion(position)

                if (direction == ItemTouchHelper.RIGHT) {
                    cargarDevolucionEnFormulario(devolucion)
                    adapter.notifyItemChanged(position)
                } else {
                    confirmarEliminar(devolucion)
                }
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)


        cargarDevoluciones()
    }

    private fun cargarDevoluciones() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvNoData.visibility = View.GONE

        Log.d("DevolucionesActivity", "Cargando devoluciones...")

        RetrofitInstance.getApi(this).getDevoluciones().enqueue(object : Callback<List<Devolucion>> {
            override fun onResponse(call: Call<List<Devolucion>>, response: Response<List<Devolucion>>) {
                progressBar.visibility = View.GONE

                Log.d("DevolucionesActivity", "Respuesta - Código: ${response.code()}")
                Log.d("DevolucionesActivity", "Exitosa: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    Log.d("DevolucionesActivity", "Total devoluciones: ${data.size}")

                    data.forEachIndexed { index, devolucion ->
                        Log.d("DevolucionesActivity", "[$index] ID: ${devolucion.id_devolucion}, Motivo: ${devolucion.motivo}")
                    }
                    if (data.isNotEmpty()) {
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateList(data)
                        Toast.makeText(this@EregistrarDevolucionActivity, "Lista actualizada", Toast.LENGTH_SHORT).show()
                    } else {
                        tvNoData.visibility = View.VISIBLE
                        Toast.makeText(this@EregistrarDevolucionActivity, "No hay devoluciones", Toast.LENGTH_SHORT).show()
                    }
                } else {

                    Log.e("DevolucionesActivity", "Error al cargar: ${response.code()}")
                    Log.e("DevolucionesActivity", "Mensaje: ${response.message()}")
                    Log.e("DevolucionesActivity", "ErrorBody: ${response.errorBody()?.string()}")

                    tvNoData.visibility = View.VISIBLE
                    tvNoData.text = "Error al cargar datos"
                    Toast.makeText(this@EregistrarDevolucionActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Devolucion>>, t: Throwable) {
                progressBar.visibility = View.GONE
                tvNoData.visibility = View.VISIBLE
                tvNoData.text = "Error de conexión"
                Log.e("DevolucionesActivity", "Error de conexión: ${t.message}", t)
                Toast.makeText(this@EregistrarDevolucionActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun crearDevolucion(v: View) {
        val cantidadStr = findViewById<TextInputEditText>(R.id.etCantidad).text.toString()
        val motivo = findViewById<TextInputEditText>(R.id.etMotivo).text.toString()
        val fechaDevolucion = findViewById<TextInputEditText>(R.id.etFechaDevolucion).text.toString()
        val idOrdenSalidaStr = findViewById<TextInputEditText>(R.id.etIdOrdenSalida).text.toString()
        val idProductoStr = findViewById<TextInputEditText>(R.id.etIdProducto).text.toString()

        Log.d("DevolucionForm", "=== DATOS DEL FORMULARIO ===")
        Log.d("DevolucionForm", "Cantidad: '$cantidadStr'")
        Log.d("DevolucionForm", "Motivo: '$motivo'")
        Log.d("DevolucionForm", "Fecha: '$fechaDevolucion'")
        Log.d("DevolucionForm", "ID Orden: '$idOrdenSalidaStr'")
        Log.d("DevolucionForm", "ID Producto: '$idProductoStr'")
        Log.d("DevolucionForm", "Editando? ${devolucionEditando != null}")
        Log.d("DevolucionForm", "ID Editando: ${devolucionEditando?.id_devolucion}")

        if (cantidadStr.isEmpty() || motivo.isEmpty() || fechaDevolucion.isEmpty() ||
            idOrdenSalidaStr.isEmpty() || idProductoStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidad = cantidadStr.toIntOrNull()
        val idOrdenSalida = idOrdenSalidaStr.toIntOrNull()
        val idProducto = idProductoStr.toIntOrNull()

        if (cantidad == null || cantidad <= 0) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show()
            return
        }

        if (idOrdenSalida == null || idProducto == null) {
            Toast.makeText(this, "IDs inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        if (devolucionEditando != null) {
            val devolucionActualizada = Devolucion(
                devolucionEditando!!.id_devolucion,
                cantidad,
                motivo,
                fechaDevolucion,
                idOrdenSalida,
                idProducto
            )
            Log.d("DevolucionPUT", "Enviando actualización: $devolucionActualizada")

            RetrofitInstance.getApi(this).updateDevolucion(devolucionEditando!!.id_devolucion, devolucionActualizada)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {

                        Log.d("DevolucionPUT", "Código respuesta: ${response.code()}")
                        Log.d("DevolucionPUT", "Respuesta exitosa: ${response.isSuccessful}")
                        Log.d("DevolucionPUT", "Body: ${response.body()}")
                        Log.d("DevolucionPUT", "ErrorBody: ${response.errorBody()?.string()}")

                        if (response.isSuccessful) {
                            Toast.makeText(this@EregistrarDevolucionActivity, "Devolución actualizada", Toast.LENGTH_SHORT).show()
                            limpiarFormulario()
                            findViewById<CardView>(R.id.cardFormulario).visibility = View.GONE
                            findViewById<MaterialButton>(R.id.btnToggleForm).text = "+ Agregar Nueva Devolución"
                            cargarDevoluciones()
                            devolucionEditando = null
                        } else {
                            Toast.makeText(this@EregistrarDevolucionActivity, "Error al actualizar: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@EregistrarDevolucionActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        } else {

            val nuevaDevolucion = Devolucion(
                0,
                cantidad,
                motivo,
                fechaDevolucion,
                idOrdenSalida,
                idProducto
            )

            Log.d("DevolucionPOST", "Enviando nueva devolución: $nuevaDevolucion")

            RetrofitInstance.getApi(this).createDevolucion(nuevaDevolucion)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {

                        Log.d("DevolucionPOST", "Código respuesta: ${response.code()}")
                        Log.d("DevolucionPOST", "Respuesta exitosa: ${response.isSuccessful}")
                        Log.d("DevolucionPOST", "Body: ${response.body()}")
                        Log.d("DevolucionPOST", "ErrorBody: ${response.errorBody()?.string()}")

                        if (response.isSuccessful ) {
                            Toast.makeText(
                                this@EregistrarDevolucionActivity,
                                "Devolución registrada",
                                Toast.LENGTH_SHORT
                            ).show()

                            cargarDevoluciones()

                            limpiarFormulario()
                            findViewById<CardView>(R.id.cardFormulario).visibility = View.GONE
                            findViewById<MaterialButton>(R.id.btnToggleForm).text = "+ Agregar Nueva Devolución"
                        } else {
                            Toast.makeText(this@EregistrarDevolucionActivity, "Error al crear: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@EregistrarDevolucionActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }

    private fun confirmarEliminar(devolucion: Devolucion) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Devolución")
            .setMessage("¿Eliminar la devolución?\nMotivo: ${devolucion.motivo}")
            .setPositiveButton("Eliminar") { _, _ ->
                RetrofitInstance.getApi(this).deleteDevolucion(devolucion.id_devolucion)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@EregistrarDevolucionActivity, "Devolución eliminada", Toast.LENGTH_SHORT).show()
                                cargarDevoluciones()
                            } else {
                                Toast.makeText(this@EregistrarDevolucionActivity, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@EregistrarDevolucionActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
            .setNegativeButton("Cancelar") { _, _ -> cargarDevoluciones() }
            .show()
    }
    private fun cargarDevolucionEnFormulario(devolucion: Devolucion) {
        devolucionEditando = devolucion
        findViewById<TextInputEditText>(R.id.etCantidad).setText(devolucion.cantidad.toString())
        findViewById<TextInputEditText>(R.id.etMotivo).setText(devolucion.motivo)
        findViewById<TextInputEditText>(R.id.etFechaDevolucion).setText(devolucion.fecha_devolucion)
        findViewById<TextInputEditText>(R.id.etIdOrdenSalida).setText(devolucion.id_ordensalida.toString())
        findViewById<TextInputEditText>(R.id.etIdProducto).setText(devolucion.id_producto.toString())
        findViewById<CardView>(R.id.cardFormulario).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnToggleForm).text = "- Cancelar Edición"
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Actualizar Devolución"
    }
    private fun limpiarFormulario() {
        findViewById<TextInputEditText>(R.id.etCantidad).setText("")
        findViewById<TextInputEditText>(R.id.etMotivo).setText("")
        findViewById<TextInputEditText>(R.id.etFechaDevolucion).setText("")
        findViewById<TextInputEditText>(R.id.etIdOrdenSalida).setText("")
        findViewById<TextInputEditText>(R.id.etIdProducto).setText("")
        findViewById<MaterialButton>(R.id.btnGuardar).text = "Registrar Devolución"
        devolucionEditando = null
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}