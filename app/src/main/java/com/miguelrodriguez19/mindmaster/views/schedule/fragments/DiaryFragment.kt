package com.miguelrodriguez19.mindmaster.views.schedule.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.miguelrodriguez19.mindmaster.databinding.FragmentDiaryBinding
import com.miguelrodriguez19.mindmaster.models.structures.abstractClasses.AbstractEvent
import com.miguelrodriguez19.mindmaster.models.structures.abstractClasses.EventsResponse
import com.miguelrodriguez19.mindmaster.models.utils.AllBottomSheets
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager
import com.miguelrodriguez19.mindmaster.views.schedule.adapters.AllEventsAdapter


class DiaryFragment : Fragment() {

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private lateinit var btnAddEvent: FloatingActionButton
    private lateinit var btnAddReminder: FloatingActionButton
    private lateinit var btnAddTask: FloatingActionButton
    private lateinit var btnMenuEvents: FloatingActionMenu
    private lateinit var progressBarSchedule: ProgressBar
    private lateinit var rvEventsPerMonth: RecyclerView
    private lateinit var adapter: AllEventsAdapter
    private val data: ArrayList<EventsResponse> = ArrayList()
    private var dataFiltered: ArrayList<EventsResponse> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
        setUpData()

        btnAddEvent.setOnClickListener {
            AllBottomSheets.showEventsBS(requireContext(), null) {
                adapter.addItem(it)
            }
            btnMenuEvents.close(true)

        }

        btnAddReminder.setOnClickListener {
            AllBottomSheets.showRemindersBS(requireContext(), null) {
                adapter.addItem(it)
            }
            btnMenuEvents.close(true)
        }

        btnAddTask.setOnClickListener {
            AllBottomSheets.showTasksBS(requireContext(), null) {
                adapter.addItem(it)
            }
            btnMenuEvents.close(true)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { search(it) }
                return true
            }
        })
    }

    private fun setUpData() {
        progressBarSchedule.visibility = View.VISIBLE
        this@DiaryFragment.data.clear()
        FirebaseManager.loadAllSchedule() { allEvents ->
            this@DiaryFragment.data.addAll(allEvents)
            dataFiltered = data
            adapter.setData(allEvents)
            progressBarSchedule.visibility = View.GONE
        }
    }

    private fun search(text: String) {
        val filteredData = ArrayList<EventsResponse>()
        for (item in data) {
            val filteredEvents = ArrayList<AbstractEvent>()
            for (absEvent in item.allEventsList) {
                if (absEvent.title.contains(text, true)) {
                    filteredEvents.add(absEvent)
                }
            }
            if (filteredEvents.isNotEmpty()) {
                filteredData.add(EventsResponse(item.date, filteredEvents.toList()))
            }
        }
        adapter.data = filteredData
        adapter.notifyDataSetChanged()
    }

    private fun initWidget() {
        searchView = binding.searchView
        btnAddEvent = binding.fabAddEvent
        btnAddTask = binding.fabAddTask
        btnAddReminder = binding.fabAddReminder
        btnMenuEvents = binding.fambAddMenu
        rvEventsPerMonth = binding.rvAllEvents
        progressBarSchedule = binding.progressBarSchedule
        rvEventsPerMonth.layoutManager = StaggeredGridLayoutManager(1, 1)
        adapter = AllEventsAdapter(requireContext(), data)
        rvEventsPerMonth.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


