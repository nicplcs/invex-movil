package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Api.Movimiento
import com.example.appinterface.R

class MovimientosAdapter(
    private val movimientos: MutableList<Movimiento>,
    private val onDeleteClick: (Movimiento, Int) -> Unit
) : RecyclerView.Adapter<MovimientosAdapter.MovimientoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimientoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movimiento, parent, false)
        return MovimientoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovimientoViewHolder, position: Int) {
        holder.bind(movimientos[position], position, onDeleteClick)
    }

    override fun getItemCount(): Int = movimientos.size

    fun removeItem(position: Int) {
        movimientos.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, movimientos.size)
    }

    class MovimientoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvId_movimiento: TextView = itemView.findViewById(R.id.tvId_movimiento)
        private val tvTipo: TextView = itemView.findViewById(R.id.tvTipoMovimiento)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionMovimiento)
        private val tvResponsable: TextView = itemView.findViewById(R.id.tvResponsable)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFechaMovimiento)
        private val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
        private val tvId_producto: TextView = itemView.findViewById(R.id.tvId_producto)
        private val tvAccion: TextView = itemView.findViewById(R.id.tvAccion)
        private val card: CardView = itemView.findViewById(R.id.cardView)
        private val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)

        fun bind(movimiento: Movimiento, position: Int, onDeleteClick: (Movimiento, Int) -> Unit) {
            tvId_movimiento.text = "Id movimiento: ${movimiento.id_movimiento}"
            tvTipo.text = "Tipo: ${movimiento.tipo}"
            tvDescripcion.text = "Descripcion: ${ movimiento.descripcion}"
            tvResponsable.text = "Responsable: ${movimiento.usuario_responsable}"
            tvFecha.text = "Fecha: ${movimiento.fecha}"
            tvCantidad.text = "Cantidad: ${movimiento.cantidad}"
            tvId_producto.text = "Id Producto: ${movimiento.id_producto}"
            tvAccion.text = "Accion: ${movimiento.accion}"

            val context = itemView.context
            val colorRegistro = context.getColor(R.color.purple_button)
            val colorOtro = context.getColor(R.color.card_background)
            val textColor = context.getColor(R.color.white)

            if (movimiento.tipo.lowercase() == "registro") {
                card.setCardBackgroundColor(colorRegistro)
                tvId_movimiento.setTextColor(textColor)
                tvTipo.setTextColor(textColor)
                tvDescripcion.setTextColor(textColor)
                tvResponsable.setTextColor(textColor)
                tvFecha.setTextColor(textColor)
                tvCantidad.setTextColor(textColor)
                tvId_producto.setTextColor(textColor)
                tvAccion.setTextColor(textColor)

            } else {
                card.setCardBackgroundColor(colorOtro)
            }

            btnEliminar.setOnClickListener {
                onDeleteClick(movimiento, position)
            }
        }
    }
}