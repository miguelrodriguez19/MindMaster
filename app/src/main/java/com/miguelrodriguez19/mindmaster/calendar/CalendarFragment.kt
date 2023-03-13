package com.miguelrodriguez19.mindmaster.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.miguelrodriguez19.mindmaster.databinding.FragmentCalendarBinding
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
    private lateinit var adapter: CalendarEventsAdapter
    private lateinit var pbLoading: View
    var data = arrayListOf("Examen de Mates", "Examen de Lengua","Examen de Ingles", "Examen de Historia", "Examen de Fisica")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidgets()
        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvCalendarEvents.layoutManager = mLayoutManager

        adapter = CalendarEventsAdapter(data){ title ->
            Log.i(TAG, "onViewCreated - event: $title")
        }

        rvCalendarEvents.adapter = adapter

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            tvSelectedDateEvents.text = formattedDate
        }

        pbLoading.visibility = View.GONE

    }

    private fun initWidgets() {
        calendarView = binding.calendarView
        tvSelectedDateEvents = binding.tvSelectedDateEvents
        tvCountOfEvents = binding.tvCountOfEvents
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
