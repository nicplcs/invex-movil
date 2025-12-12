package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Api.Usuario
import com.example.appinterface.R

class UsuarioAdapter(private val usuarios: MutableList<Usuario>) :
    RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(usuarios[position])
    }

    override fun getItemCount(): Int = usuarios.size

    fun getUsuario(position: Int): Usuario = usuarios[position]

    fun updateList(nuevaLista: List<Usuario>) {
        usuarios.clear()
        usuarios.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun addUsuario(usuario: Usuario) {
        usuarios.add(usuario)
        notifyItemInserted(usuarios.size - 1)
    }

    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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