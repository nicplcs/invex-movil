package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Api.Producto
import com.example.appinterface.R

class ProductosAdapter(private val productos: List<Producto>) :
    RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val p = productos[position]


        holder.txtNombre.text = p.nombre


        val estadoLegible = if (p.estado == "1") "Activo" else "Inactivo"
        holder.txtIdEstado.text = "ID: ${p.idProducto}   •   Estado: $estadoLegible"


        holder.txtPrecio.text = "Precio: ${"%.2f".format(p.precio)}"


        holder.txtStocks.text = "Stock: ${p.stock}   •   Min: ${p.stockMinimo}   •   Max: ${p.stockMaximo}   •   Actual: ${p.stockActual}"


        holder.txtRelaciones.text = "Categoría: ${p.idCategoria}   •   Proveedor: ${p.idProveedor}"
    }

    override fun getItemCount(): Int = productos.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombreProducto)
        val txtIdEstado: TextView = itemView.findViewById(R.id.txtIdEstado)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecioProducto)
        val txtStocks: TextView = itemView.findViewById(R.id.txtStocks)
        val txtRelaciones: TextView = itemView.findViewById(R.id.txtRelaciones)
    }
}
