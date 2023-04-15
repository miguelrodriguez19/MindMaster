package com.miguelrodriguez19.mindmaster.diary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.clans.fab.FloatingActionButton
import com.miguelrodriguez19.mindmaster.databinding.FragmentDiaryBinding
import com.miguelrodriguez19.mindmaster.models.*


class DiaryFragment : Fragment() {

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private lateinit var btnAddEvent: FloatingActionButton
    private lateinit var btnAddReminder: FloatingActionButton
    private lateinit var btnAddTask: FloatingActionButton
    private lateinit var rvEventsPerMonth: RecyclerView
    private lateinit var adapter: AllEventsAdapter
    var data: ArrayList<EventsResponse> = ArrayList()
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

    private fun search(text: String) {
        val filteredData = ArrayList<EventsResponse>()
        for (item in data) {
            val filteredEvents = ArrayList<AbstractEvents>()
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
        createFakeData()
        searchView = binding.searchView
        btnAddEvent = binding.fabAddEvent
        btnAddTask = binding.fabAddTask
        btnAddReminder = binding.fabAddReminder
        rvEventsPerMonth = binding.rvAllEvents

        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvEventsPerMonth.layoutManager = mLayoutManager

        adapter = AllEventsAdapter(requireContext(), data, Sender.SCHEDULE)
        dataFiltered.addAll(data)

        rvEventsPerMonth.adapter = adapter
    }

    private fun createFakeData() {
        val eventsList = arrayListOf(
            EventsResponse(
                date = "2022-06-25",
                allEventsList = listOf(
                    Event(
                        cod = "1",
                        title = "Concierto de rock",
                        start_time = "2022-06-25 19:00",
                        end_time = "2022-06-25 19:00",
                        location = "Estadio de futbol",
                        description = "Un gran concierto con muchas bandas famosas",
                        participants = listOf("Banda 1", "Banda 2", "Banda 3"),
                        category = "Entretenimiento",
                        repetition = Repetition.ONCE,
                        color_tag = "#F44336",
                        type = EventType.EVENT
                    ),
                    Reminder(
                        cod ="2",
                        title = "Recordatorio de cumpleaños",
                        reminder_time = "2022-06-25 09:00",
                        description = "El cumpleaños de mi mejor amigo",
                        category = "Personal",
                        color_tag = "#FF9800",
                        type = EventType.REMINDER
                    ),
                    Task(
                        cod = "3",
                        title = "Enviar informe de ventas",
                        due_date = "2022-06-25 17:00",
                        description = "Informe mensual de ventas de la compañía",
                        priority = Priority.HIGH,
                        status = Status.PENDING,
                        category = "Trabajo",
                        color_tag = "#4CAF50",
                        type = EventType.TASK
                    )
                )
            ),
            EventsResponse(
                date = "2022-06-26",
                allEventsList = listOf(
                    Event(
                        cod = "4",
                        title = "Festival de comida",
                        start_time = "2022-06-26 12:00",
                        end_time = "2022-06-26 12:00",
                        location = "Parque central",
                        description = "Disfruta de diferentes comidas de todo el mundo",
                        participants = null,
                        category = "Entretenimiento",
                        repetition = Repetition.ANNUAL,
                        color_tag = "#F44336",
                        type = EventType.EVENT
                    ),
                    Reminder(
                        cod = "5",
                        title = "Recordatorio de cita médica",
                        reminder_time = "2022-06-26 16:30",
                        description = "Ir al dentista",
                        category = "Salud",
                        color_tag = "#FF9800",
                        type = EventType.REMINDER
                    ),
                    Task(
                        cod = "6",
                        title = "Comprar boletos de avión",
                        due_date = "2022-06-26 23:59",
                        description = null,
                        priority = Priority.MEDIUM,
                        status = Status.PENDING,
                        category = "Viajes",
                        color_tag = "#4CAF50",
                        type = EventType.TASK
                    )
                )
            )
        )
        data = eventsList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


