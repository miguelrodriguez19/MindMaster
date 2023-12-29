package com.miguelrodriguez19.mindmaster.view.fragments.schedule

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
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.EventsResponse
import com.miguelrodriguez19.mindmaster.view.adapters.schedule.AllEventsAdapter
import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet
import com.miguelrodriguez19.mindmaster.view.bottomSheets.EventBS
import com.miguelrodriguez19.mindmaster.view.bottomSheets.ReminderBS
import com.miguelrodriguez19.mindmaster.view.bottomSheets.TaskBS


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
            showBottomSheet(EventBS::class.java.name)
        }

        btnAddReminder.setOnClickListener {
            showBottomSheet(ReminderBS::class.java.name)
        }

        btnAddTask.setOnClickListener {
            showBottomSheet(TaskBS::class.java.name)
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

    private fun showBottomSheet(name: String) {
        val activityBS = CustomBottomSheet.get<AbstractActivity>(name)

        activityBS?.showViewDetailBS(requireContext(), null) { absEvent ->
            adapter.addItem(absEvent)
        }

        btnMenuEvents.close(true)
    }

    private fun setUpData() {
        progressBarSchedule.visibility = View.VISIBLE
        this@DiaryFragment.data.clear()
        FirestoreManagerFacade.loadAllSchedule() { allEvents ->
            this@DiaryFragment.data.addAll(allEvents)
            dataFiltered = data
            adapter.setData(allEvents)
            progressBarSchedule.visibility = View.GONE
        }
    }

    private fun search(text: String) {
        val filteredData = ArrayList<EventsResponse>()
        for (item in data) {
            val filteredEvents = ArrayList<AbstractActivity>()
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


