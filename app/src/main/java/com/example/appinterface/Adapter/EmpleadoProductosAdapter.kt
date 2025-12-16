package com.example.appinterface.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Api.Producto
import com.example.appinterface.R

class EmpleadoProductosAdapter(
    private var productos: MutableList<Producto>
) : RecyclerView.Adapter<EmpleadoProductosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardProducto: CardView = view.findViewById(R.id.cardEmpleadoProducto)
        val tvIdProducto: TextView = view.findViewById(R.id.tvEmpleadoIdProducto)
        val tvNombre: TextView = view.findViewById(R.id.tvEmpleadoNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvEmpleadoPrecio)
        val tvStockActual: TextView = view.findViewById(R.id.tvEmpleadoStockActual)
        val tvStockMinimo: TextView = view.findViewById(R.id.tvEmpleadoStockMinimo)
        val tvStockMaximo: TextView = view.findViewById(R.id.tvEmpleadoStockMaximo)
        val tvCategoria: TextView = view.findViewById(R.id.tvEmpleadoCategoria)
        val tvProveedor: TextView = view.findViewById(R.id.tvEmpleadoProveedor)
        val tvEstado: TextView = view.findViewById(R.id.tvEmpleadoEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empleado_producto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productos[position]

        holder.tvIdProducto.text = "ID: ${producto.idProducto}"
        holder.tvNombre.text = producto.nombre
        holder.tvPrecio.text = "$" + String.format("%.2f", producto.precio)
        holder.tvStockActual.text = "Stock: ${producto.stockActual}"
        holder.tvStockMinimo.text = "Mín: ${producto.stockMinimo}"
        holder.tvStockMaximo.text = "Máx: ${producto.stockMaximo}"
        holder.tvCategoria.text = "Cat: ${producto.idCategoria}"
        holder.tvProveedor.text = "Prov: ${producto.idProveedor}"

        // Estado visual
        val estadoTexto = when (producto.estado) {
            "1" -> "Activo"
            "0" -> "Inactivo"
            else -> producto.estado
        }
        holder.tvEstado.text = estadoTexto

        // Color del estado
        if (producto.estado == "1") {
            holder.tvEstado.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvEstado.setTextColor(Color.parseColor("#FF5252"))
        }

        // Alerta de stock bajo
        if (producto.stockActual <= producto.stockMinimo) {
            holder.cardProducto.setCardBackgroundColor(Color.parseColor("#33FF5252"))
        } else {
            holder.cardProducto.setCardBackgroundColor(Color.parseColor("#1AFFFFFF"))
        }
    }

    override fun getItemCount() = productos.size

    fun updateList(nuevaLista: List<Producto>) {
        productos.clear()
        productos.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun getProducto(position: Int): Producto = productos[position]
}