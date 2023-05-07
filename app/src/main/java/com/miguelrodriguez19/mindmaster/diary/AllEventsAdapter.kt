package com.miguelrodriguez19.mindmaster.diary

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.calendar.CalendarEventsAdapter
import com.miguelrodriguez19.mindmaster.databinding.CellDayAllEventsBinding
import com.miguelrodriguez19.mindmaster.models.*
import com.miguelrodriguez19.mindmaster.utils.AllDialogs


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
        private val rvMonthEvents = bind.rvMonthEvents
        private lateinit var adapter: CalendarEventsAdapter

        fun bind(item: EventsResponse) {
            initRecyclerView(item.allEventsList)
            btnMonth.text = item.date
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

            val itemTouchHelper =
                ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        AllDialogs.showConfirmationDialog(
                            context,
                            context.getString(R.string.delete_confirmation),
                            context.getString(R.string.delete_event_message)
                        ) {
                            val position = viewHolder.adapterPosition
                            if (it) {
                                adapter.removeAt(position)
                            } else {
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onChildDraw(
                        c: Canvas,
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        dX: Float,
                        dY: Float,
                        actionState: Int,
                        isCurrentlyActive: Boolean
                    ) {
                        val icon = ContextCompat.getDrawable(context, R.drawable.ic_delete_24)?.mutate()
                        icon?.setTint(ContextCompat.getColor(context, android.R.color.white))
                        val background = ColorDrawable(context.getColor(R.color.red_bittersweet_200))
                        val itemView = viewHolder.itemView
                        val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
                        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                        val iconBottom = iconTop + icon.intrinsicHeight

                        // Swipe to right
                        if (dX > 0) {
                            val iconLeft = itemView.left + iconMargin
                            val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                            background.setBounds(
                                itemView.left,
                                itemView.top,
                                itemView.left + dX.toInt(),
                                itemView.bottom
                            )
                        } else { // Other swipes
                            background.setBounds(0, 0, 0, 0)
                        }
                        background.draw(c)
                        icon.draw(c)
                        super.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }
                })
            itemTouchHelper.attachToRecyclerView(rvMonthEvents)
        }

    }
}