package com.example.appinterface.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Api.Devolucion
import com.example.appinterface.R

class DevolucionesAdapter (

    private val devoluciones: MutableList<Devolucion>
) : RecyclerView.Adapter<DevolucionesAdapter.DevolucionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevolucionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_devolucion, parent, false)
        return DevolucionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DevolucionViewHolder, position: Int) {

        Log.d("AdapterBind", "Posición $position: ${devoluciones[position]}")
        Log.d("AdapterBind", "ID: ${devoluciones[position].id_devolucion}")
        holder.bind(devoluciones[position])
    }

    override fun getItemCount(): Int = devoluciones.size

    fun getDevolucion(position: Int): Devolucion = devoluciones[position]

    fun updateList(newList: List<Devolucion>){
        devoluciones.clear()
        devoluciones.addAll(newList)
        notifyDataSetChanged()
    }
    fun addDevolucion(devolucion: Devolucion) {
        devoluciones.add(devolucion)
        notifyItemInserted(devoluciones.size - 1)
    }

    class DevolucionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvId_devolucion: TextView = itemView.findViewById(R.id.tvId_devolucion)
        private val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
        private val tvMotivo: TextView = itemView.findViewById(R.id.tvMotivo)
        private val tvFecha_devolucion: TextView = itemView.findViewById(R.id.tvFecha_devolucion)
        private val tvId_ordensalida: TextView = itemView.findViewById(R.id.tvId_ordensalida)
        private val tvId_producto: TextView = itemView.findViewById(R.id.tvId_producto)
        private val card: CardView = itemView.findViewById(R.id.cardView)

        fun bind(devolucion: Devolucion) {
            tvId_devolucion.text = "Id devolucion: ${devolucion .id_devolucion}"
            tvCantidad.text = "Cantidad: ${devolucion.cantidad}"
            tvMotivo.text = "Motivo: ${ devolucion.motivo}"
            tvFecha_devolucion.text = "Fecha devolucion: ${devolucion.fecha_devolucion}"
            tvId_ordensalida.text = "Id orden salida: ${devolucion.id_ordensalida}"
            tvId_producto.text = "Id producto: ${devolucion.id_producto}"

            val context = itemView.context
            val colorFondo = context.getColor(R.color.card_background)
            card.setCardBackgroundColor(colorFondo)


        }
    }
}