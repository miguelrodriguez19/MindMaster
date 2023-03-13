package com.miguelrodriguez19.mindmaster.calendar

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellCalendarEventsBinding
import de.hdodenhof.circleimageview.CircleImageView

class CalendarEventsAdapter(private val data: ArrayList<String>, val onClick: (String) -> Unit) :
    RecyclerView.Adapter<CalendarEventsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_calendar_events, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellCalendarEventsBinding.bind(v)
        private val clEventArea = bind.clEventArea
        private val tvEventType = bind.tvEventType
        private val tvEventTitle = bind.tvEventTitle
        private val civColorTag = bind.civColorTag

        private val typeEvents = listOf("Evento", "Recordatorio", "Tarea")
        fun bind(item: String) {
            tvEventTitle.text = item
            tvEventType.text = typeEvents.random()
            setCircleImageViewColor(civColorTag)
            clEventArea.setOnClickListener {
                onClick(item)
            }
        }

        private fun setCircleImageViewColor(circleImageView: CircleImageView) {
            val color = generateColor()
            val drawable = ColorDrawable(color)
            circleImageView.setImageDrawable(drawable)
        }


        private fun generateColor(): Int {
            val colors = arrayListOf(
                0xfffc9279,0xfff5b6c3,0xfff1a4fe,0xffbe9bff,
                0xffadb9ff,0xff2196f3,0xff03a9f4,0xff00bcd4,
                0xff009688,0xff4caf50,0xff8bc34a,0xffcddc39,
                0xffffeb3b,0xffffc107,0xffff9800,0xffff7421,
                0xffc58a75,0xff9e9e9e,0xffa3c8d9
            )
            return colors.random().toInt()
        }
    }
}