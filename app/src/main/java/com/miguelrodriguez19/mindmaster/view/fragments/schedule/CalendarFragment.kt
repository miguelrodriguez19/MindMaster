package com.miguelrodriguez19.mindmaster.view.fragments.schedule

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.LinearLayout
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
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade.deleteInSchedule
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade.loadScheduleByDate
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade.saveInSchedule
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.DEFAULT_DATE_FORMAT
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.getCurrentDate
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.showUndoSnackBar
import com.miguelrodriguez19.mindmaster.view.adapters.schedule.CalendarEventsAdapter
import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet
import com.miguelrodriguez19.mindmaster.view.bottomSheets.EventBS
import com.miguelrodriguez19.mindmaster.view.bottomSheets.ReminderBS
import com.miguelrodriguez19.mindmaster.view.bottomSheets.TaskBS
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

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
    private lateinit var gifView: GifImageView
    private lateinit var llNoEvents: LinearLayout
    private val data = ArrayList<AbstractActivity>()
    private val listGifs = listOf(R.drawable.gif_no_data_found_1, R.drawable.gif_no_data_found_2)
    private lateinit var formattedDate:String

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
        CoroutineScope(Dispatchers.Main).launch {
            formattedDate = getCurrentDate()
            setUpData(formattedDate)
        }

        btnAddEvent.setOnClickListener {
            disableButtons()
            showBottomSheet(EventBS::class.java.name, formattedDate)
        }

        btnAddReminder.setOnClickListener {
            disableButtons()
            showBottomSheet(ReminderBS::class.java.name, formattedDate)
        }

        btnAddTask.setOnClickListener {
            disableButtons()
            showBottomSheet(TaskBS::class.java.name, formattedDate)
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
            formattedDate = dateFormat.format(selectedDate.time)
            CoroutineScope(Dispatchers.Main).launch { setUpData(formattedDate) }
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
                            deleteInSchedule(adapter.getItemAt(position)) { absActivity ->
                                showUndoSnackBar(requireContext(), requireView()) { ok ->
                                    if (ok) {
                                        saveInSchedule(absActivity) { item ->
                                            adapter.addItem(item)
                                            llNoEvents.visibility = View.GONE
                                            tvCountOfEvents.text = adapter.itemCount.toString()
                                        }
                                    }else{
                                        absActivity.removeNotifications(requireContext())
                                    }
                                }
                            }
                            adapter.removeAt(position)
                            if (adapter.itemCount < 1) {
                                tvCountOfEvents.text = adapter.itemCount.toString()
                                setNoDataVisible()
                            }
                        } else {
                            adapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onChildDraw(
                    c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                    dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
                ) {
                    val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_24)
                        ?.mutate()
                    icon?.setTint(ContextCompat.getColor(requireContext(), android.R.color.white))
                    val background =
                        ColorDrawable(requireContext().getColor(R.color.red_bittersweet_200))
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
                        c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                    )
                }
            })
        itemTouchHelper.attachToRecyclerView(rvCalendarEvents)
    }

    private fun showBottomSheet(name: String, dateStr: String) {
        val activityBS = CustomBottomSheet.get<AbstractActivity>(name)
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT))

        activityBS?.showViewDetailBS(requireActivity(), null, date) { absActivity ->
            val activityDate = AbstractActivity.getFormattedDateOf(absActivity)
            if (tvSelectedDateEvents.text == activityDate) {
                adapter.addItem(absActivity)
                tvCountOfEvents.text = adapter.itemCount.toString()
                llNoEvents.visibility = View.GONE
            }
        }

        btnMenuEvents.close(false)
        enableButtons()
    }

    private fun disableButtons() {
        btnAddEvent.isEnabled = false
        btnAddReminder.isEnabled = false
        btnAddTask.isEnabled = false
    }

    private fun enableButtons() {
        btnAddEvent.isEnabled = true
        btnAddReminder.isEnabled = true
        btnAddTask.isEnabled = true
    }

    private suspend fun setUpData(date: String) {
        pbLoading.visibility = View.VISIBLE
        tvSelectedDateEvents.text = date
        this@CalendarFragment.data.clear()
        val dayList = loadScheduleByDate(date)
        this@CalendarFragment.data.addAll(dayList)
        adapter.setData(dayList)
        tvCountOfEvents.text = this.data.size.toString()
        if (data.size == 0) {
            setNoDataVisible()
        } else {
            llNoEvents.visibility = View.GONE
        }
        pbLoading.visibility = View.GONE
    }

    private fun setNoDataVisible() {
        llNoEvents.visibility = View.VISIBLE
        if (isAdded) {
            val gifDrawable = GifDrawable.createFromResource(
                requireContext().applicationContext.resources,
                listGifs.shuffled()[0]
            )
            gifView.setImageDrawable(gifDrawable)
        }
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
        gifView = binding.givNoEvents
        llNoEvents = binding.llNoEvents
        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvCalendarEvents.layoutManager = mLayoutManager
        adapter = CalendarEventsAdapter(requireActivity(), data) { item ->
            Log.i(TAG, "onViewCreated - event: ${item.title}")
        }

        rvCalendarEvents.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
