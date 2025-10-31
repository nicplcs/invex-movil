package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Api.Usuario
import com.example.appinterface.R

class PersonaAdapter(private val usuarios: List<Usuario>) :
    RecyclerView.Adapter<PersonaAdapter.PersonaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return PersonaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonaViewHolder, position: Int) {
        holder.bind(usuarios[position])
    }

    override fun getItemCount(): Int = usuarios.size

    class PersonaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvCorreo: TextView = itemView.findViewById(R.id.tvCorreo)
        private val tvRol: TextView = itemView.findViewById(R.id.tvRol)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)

        fun bind(usuario: Usuario) {
            tvNombre.text = usuario.nombre
            tvCorreo.text = usuario.correo
            tvRol.text = usuario.rol
            tvEstado.text = if (usuario.estado == "1") "Activo" else "Inactivo"

            val card = itemView.findViewById<View>(R.id.cardView)
            val context = itemView.context
            val colorActivo = context.getColor(R.color.purple_button)
            val colorInactivo = context.getColor(R.color.card_background)
            card.setBackgroundColor(if (usuario.estado == "1") colorActivo else colorInactivo)
        }
    }
}
