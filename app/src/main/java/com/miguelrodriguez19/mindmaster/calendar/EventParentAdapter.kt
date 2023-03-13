package com.miguelrodriguez19.mindmaster.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.squareup.picasso.Picasso

/*
class EventParentAdapter(private val data: ArrayList<MoviesResponse.Movie>, val onClick: (MoviesResponse.Movie) -> Unit) :
    RecyclerView.Adapter<EventParentAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movies_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val card = v.findViewById<CardView>(R.id.card)
        val titulo = v.findViewById<TextView>(R.id.titulo)
        val img = v.findViewById<ImageView>(R.id.caratula)
        fun bind(item: MoviesResponse.Movie) {
            titulo.text = item.title
            Picasso.get().load("${ApiRest.URL_IMAGES}${item.posterPath}").into(img)
            card.setCardBackgroundColor(generateColor().toInt())
            card.setOnClickListener {
                onClick(item)
            }
        }

    }
}*/