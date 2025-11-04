package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Api.Movimiento
import com.example.appinterface.R

class MovimientosAdapter(private val movimientos: List<Movimiento>) :
    RecyclerView.Adapter<MovimientosAdapter.MovimientoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimientoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movimiento, parent, false)
        return MovimientoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovimientoViewHolder, position: Int) {
        holder.bind(movimientos[position])
    }

    override fun getItemCount(): Int = movimientos.size

    class MovimientoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTipo: TextView = itemView.findViewById(R.id.tvTipoMovimiento)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionMovimiento)
        private val tvUsuario: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFechaMovimiento)
        private val card: CardView = itemView.findViewById(R.id.cardView)

        fun bind(movimiento: Movimiento) {
            tvTipo.text = movimiento.tipo
            tvDescripcion.text = movimiento.descripcion
            tvUsuario.text = "Responsable: ${movimiento.usuario_responsable}"
            tvFecha.text = "Fecha: ${movimiento.fecha}"

            val context = itemView.context

            // 🔹 Diferenciar visualmente los tipos de movimiento
            val colorRegistro = context.getColor(R.color.purple_button)
            val colorOtro = context.getColor(R.color.card_background)
            val textColor = context.getColor(R.color.white)

            if (movimiento.tipo.lowercase() == "registro") {
                card.setCardBackgroundColor(colorRegistro)
                tvTipo.setTextColor(textColor)
                tvDescripcion.setTextColor(textColor)
                tvUsuario.setTextColor(textColor)
                tvFecha.setTextColor(textColor)
            } else {
                card.setCardBackgroundColor(colorOtro)
            }
        }
    }
}
