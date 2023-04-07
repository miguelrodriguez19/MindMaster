package com.miguelrodriguez19.mindmaster.diary

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.calendar.CalendarEventsAdapter
import com.miguelrodriguez19.mindmaster.databinding.CellDayAllEventsBinding
import com.miguelrodriguez19.mindmaster.expenses.LastMovementsAdapter
import com.miguelrodriguez19.mindmaster.models.AbstractEvents
import com.miguelrodriguez19.mindmaster.models.EventsResponse
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse


class AllEventsAdapter(
    private val context: Context,
    var data: ArrayList<EventsResponse>
) :
    RecyclerView.Adapter<AllEventsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cell_day_all_events, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellDayAllEventsBinding.bind(v)
        private val btnMonth = bind.btnMonthTitle
        private val tvCounterEvents = bind.tvCountOfEvents
        private val rvMonthEvents = bind.rvMonthEvents
        private lateinit var adapter: CalendarEventsAdapter

        fun bind(item: EventsResponse) {
            initRecyclerView(item.allEventsList)
            btnMonth.text = item.date
            tvCounterEvents.text = item.allEventsList.count().toString()
            btnMonth.setOnClickListener {
                if (rvMonthEvents.visibility == View.GONE) {
                    rvMonthEvents.visibility = View.VISIBLE
                } else {
                    rvMonthEvents.visibility = View.GONE
                }
            }
        }

        private fun initRecyclerView(data: List<AbstractEvents>) {
            val mLayoutManager = StaggeredGridLayoutManager(1, 1)
            rvMonthEvents.layoutManager = mLayoutManager

            adapter = CalendarEventsAdapter(context, ArrayList(data)) {

            }

            rvMonthEvents.adapter = adapter
        }

    }
}