package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Api.Proveedor
import com.example.appinterface.R

class ProveedorAdapter(private val proveedores: MutableList<Proveedor>) :
    RecyclerView.Adapter<ProveedorAdapter.ProveedorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProveedorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_proveedor, parent, false)
        return ProveedorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProveedorViewHolder, position: Int) {
        holder.bind(proveedores[position])
    }

    override fun getItemCount(): Int = proveedores.size

    fun getProveedor(position: Int): Proveedor = proveedores[position]

    fun updateList(nuevaLista: List<Proveedor>) {
        proveedores.clear()
        proveedores.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun addProveedor(proveedor: Proveedor) {
        proveedores.add(proveedor)
        notifyItemInserted(proveedores.size - 1)
    }

    class ProveedorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        private val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefono)
        private val tvCorreo: TextView = itemView.findViewById(R.id.tvCorreo)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)

        fun bind(proveedor: Proveedor) {
            tvNombre.text = proveedor.nombre
            tvDireccion.text = proveedor.direccion ?: "Sin dirección"
            tvTelefono.text = proveedor.telefono
            tvCorreo.text = proveedor.correo
            tvEstado.text = if (proveedor.estado == "1") "Activo" else "Inactivo"

            val card = itemView.findViewById<View>(R.id.cardView)
            val context = itemView.context
            val colorActivo = context.getColor(R.color.purple_button)
            val colorInactivo = context.getColor(R.color.card_background)
            card.setBackgroundColor(if (proveedor.estado == "1") colorActivo else colorInactivo)
        }
    }
}