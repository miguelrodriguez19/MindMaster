package com.miguelrodriguez19.mindmaster.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var monthYearText: TextView
    private lateinit var btnNextMonth: Button
    private lateinit var btnBackMonth: Button
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private lateinit var selectedDate: Calendar
    private lateinit var userLocale: Locale
    private val monthDays = ArrayList<String>()
    private lateinit var dateFormatter: SimpleDateFormat
    private val TAG = "CalendarFragment"

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
        val rvDaysOfMonth = binding.calendarRecyclerView
        val mLayoutManager = GridLayoutManager(context, 7)
        rvDaysOfMonth.layoutManager = mLayoutManager

        // TODO("Tener en cuenta el idioma establecido")
        userLocale = Locale.US
        dateFormatter = SimpleDateFormat("MMMM yyyy", userLocale)

        initWidgets()
        selectedDate = Calendar.getInstance()
        setMonthView()

        btnBackMonth.setOnClickListener {
            goToBackMonth()
        }
        btnNextMonth.setOnClickListener {
            goToForwardMonth()
        }

    }

    private fun initWidgets() {
        btnBackMonth = binding.btnBackMonth
        btnNextMonth = binding.btnNextMonth
        calendarRecyclerView = binding.calendarRecyclerView
        monthYearText = binding.monthYearTV
    }

    private fun setMonthView() {
        monthYearText.text = monthYearFromDate(selectedDate)
        monthDays.clear()

        val daysInMonth: Int = selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstOfMonth = Calendar.getInstance()

        firstOfMonth.timeInMillis = selectedDate.timeInMillis
        firstOfMonth.set(Calendar.DAY_OF_MONTH, 1)

        val dayOfWeek = firstOfMonth.get(Calendar.DAY_OF_WEEK)
        for (i in 1..35) {
            if (i <= dayOfWeek - 1 || i > daysInMonth + dayOfWeek - 1) {
                monthDays.add("")
            } else {
                monthDays.add((i - dayOfWeek + 1).toString())
            }
        }

        val mLayoutManager = GridLayoutManager(context, 7)
        calendarRecyclerView.layoutManager = mLayoutManager
        //Creamos el adapter y lo vinculamos con el recycler
        adapter = CalendarAdapter(requireContext(), monthYearFromDate(selectedDate), monthDays) {
            Log.i(TAG, it)
        }
        calendarRecyclerView.adapter = adapter

    }

    private fun monthYearFromDate(date: Calendar): String {
        return dateFormatter.format(date.time)
    }

    private fun goToForwardMonth() {
        selectedDate.add(Calendar.MONTH, 1)
        setMonthView()
    }

    private fun goToBackMonth() {
        selectedDate.add(Calendar.MONTH, -1)
        setMonthView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
