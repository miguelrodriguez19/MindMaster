package com.miguelrodriguez19.mindmaster.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.miguelrodriguez19.mindmaster.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalendarAdapter(
    private val context: Context,
    private val month: String,
    private val data: ArrayList<String>,
    val onClick: (String) -> Unit
) :
    RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cell_calendar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], month)
    }

    override fun getItemCount(): Int = data.size
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val card = v.findViewById<MaterialCardView>(R.id.cardview_calendarCell)
        private val cardActualDay = v.findViewById<MaterialCardView>(R.id.cardview_actualday)
        private val day = v.findViewById<TextView>(R.id.cellDayText)
        fun bind(item: String, month: String) {
            if (item == getCurrentDate()[0] && month == getCurrentDate()[1]) {
                cardActualDay.visibility = View.VISIBLE
                day.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            day.text = item
            card.setOnClickListener {
                onClick(item)
            }
        }
        private fun getCurrentDate(): Array<String> {
            // TODO("TENER CUIDADO CON Locale.US luego eso vendra del SharedPreferences")
            val monthAndYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.US)
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
            val monthAndYear = monthAndYearFormatter.format(calendar.time)
            return arrayOf(day, monthAndYear)
        }
    }
}