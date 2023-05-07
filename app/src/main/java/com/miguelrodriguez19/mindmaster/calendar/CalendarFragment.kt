package com.miguelrodriguez19.mindmaster.calendar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentCalendarBinding
import com.miguelrodriguez19.mindmaster.models.*
import com.miguelrodriguez19.mindmaster.utils.AllBottomSheets
import com.miguelrodriguez19.mindmaster.utils.AllDialogs
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private val TAG = "CalendarFragment"
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var tvSelectedDateEvents: TextView
    private lateinit var tvCountOfEvents: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var rvCalendarEvents: RecyclerView
    private lateinit var btnAddEvent: FloatingActionButton
    private lateinit var btnAddReminder: FloatingActionButton
    private lateinit var btnAddTask: FloatingActionButton
    private lateinit var btnMenuEvents: FloatingActionMenu
    private lateinit var adapter: CalendarEventsAdapter
    private lateinit var pbLoading: View
    var data = ArrayList<AbstractEvents>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as MainActivity).unlockDrawer()
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidgets()
        createFakeData()
        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvCalendarEvents.layoutManager = mLayoutManager

        adapter = CalendarEventsAdapter(requireContext(), data) { item ->
            Log.i(TAG, "onViewCreated - event: ${item.title}")
        }

        rvCalendarEvents.adapter = adapter

        btnAddEvent.setOnClickListener {
            AllBottomSheets.showEventsBS(requireContext(), null)
            btnMenuEvents.close(true)
        }

        btnAddReminder.setOnClickListener {
            AllBottomSheets.showRemindersBS(requireContext(), null)
            btnMenuEvents.close(true)
        }

        btnAddTask.setOnClickListener {
            AllBottomSheets.showTasksBS(requireContext(), null)
            btnMenuEvents.close(true)
        }

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            tvSelectedDateEvents.text = formattedDate
        }

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
                        requireContext(),
                        requireContext().getString(R.string.delete_confirmation),
                        requireContext().getString(R.string.delete_event_message)
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
                    val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_24)?.mutate()
                    icon?.setTint(ContextCompat.getColor(requireContext(), android.R.color.white))
                    val background = ColorDrawable(requireContext().getColor(R.color.red_bittersweet_200))
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
        itemTouchHelper.attachToRecyclerView(rvCalendarEvents)


        pbLoading.visibility = View.GONE

    }

    private fun createFakeData() {
        data.clear()
        data.add(
            Event(
                cod = "1",
                title = "Birthday Party",
                start_time = "2022-05-21T19:00:00",
                end_time = "2022-05-21T19:00:00",
                location = "123 Main St.",
                description = "Celebrate Jane's 30th birthday",
                participants = listOf("Jane", "John", "Samantha"),
                category = listOf("Celebration"),
                repetition = Repetition.ONCE,
                color_tag = "#F44336",
                type = EventType.EVENT
            )
        )

        data.add(
            Reminder(
                cod = "2",
                title = "Meeting with Manager",
                date_time = "2022-05-23T10:00:00",
                description = "Discuss project progress",
                category = listOf("Work"),
                color_tag = "#4CAF50",
                type = EventType.REMINDER
            )
        )

        data.add(
            Task(
                cod = "3",
                title = "Finish Report",
                due_date = "2022-05-25",
                description = "Complete final report for project",
                priority = Priority.HIGH,
                status = Status.IN_PROGRESS,
                category = listOf("Work"),
                color_tag = "#F44876",
                type = EventType.TASK
            )
        )

        tvCountOfEvents.text = data.size.toString()
    }

    private fun initWidgets() {
        calendarView = binding.calendarView
        tvSelectedDateEvents = binding.tvSelectedDateEvents
        tvCountOfEvents = binding.tvCountOfEvents
        btnAddEvent = binding.fabAddEvent
        btnAddTask = binding.fabAddTask
        btnAddReminder = binding.fabAddReminder
        btnMenuEvents = binding.fambAddMenu
        rvCalendarEvents = binding.rvEvents
        pbLoading = binding.pbLoading

        tvSelectedDateEvents.text = getCurrentDate()
        tvCountOfEvents.text = data.size.toString()

    }

    private fun getCurrentDate(): CharSequence? {
        val calendar = Calendar.getInstance()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return String.format("%02d-%02d-%04d", dayOfMonth, month, year)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
