package com.miguelrodriguez19.mindmaster.calendar

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellCalendarEventsBinding
import com.miguelrodriguez19.mindmaster.models.AbstractEvents
import com.miguelrodriguez19.mindmaster.models.Sender

class CalendarEventsAdapter(
    private val context: Context,
    private val data: ArrayList<AbstractEvents>,
    private val sender:Sender,
    val onClick: (AbstractEvents) -> Unit
) :
    RecyclerView.Adapter<CalendarEventsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_calendar_events, parent, false)
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

        fun bind(item: AbstractEvents) {
            tvEventTitle.text = item.title
            tvEventType.text = item.getItemType(context, item.type)
            civColorTag.setCardBackgroundColor(item.getColor(context, item.color_tag))
            clEventArea.setOnClickListener {
                onClick(item)
            }
        }
    }
}