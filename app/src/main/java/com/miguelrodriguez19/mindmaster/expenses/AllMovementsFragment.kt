package com.miguelrodriguez19.mindmaster.expenses

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.miguelrodriguez19.mindmaster.databinding.FragmentAllMovementsBinding
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.utils.AllBottomSheets.Companion.showMovementBS

class AllMovementsFragment : Fragment() {

    private var _binding: FragmentAllMovementsBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private lateinit var btnAddMov: ExtendedFloatingActionButton
    private lateinit var rvAllMovements: RecyclerView
    private lateinit var adapter : AllMovementsAdapter
    var data: ArrayList<MonthMovementsResponse> = ArrayList()
    private var dataFiltered: ArrayList<MonthMovementsResponse> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllMovementsBinding.inflate(inflater, container, false)
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

        btnAddMov.setOnClickListener {
            showMovementBS(requireContext(), null, null)
        }
    }

    private fun search(text: String) {
        val filteredData = ArrayList<MonthMovementsResponse>()
        for (item in data) {
            val filteredMovements = ArrayList<MonthMovementsResponse.Movement>()
            for (movement in item.movementsList) {
                if (movement.concept.contains(text, true) || movement.date.contains(text, true)) {
                    filteredMovements.add(movement)
                }
            }
            if (filteredMovements.isNotEmpty()) {
                filteredData.add(MonthMovementsResponse(item.codMonthMovement, item.date, filteredMovements.toList()))
            }
        }
        adapter.data = filteredData
        adapter.notifyDataSetChanged()
    }



    private fun initWidget() {
        //createFakeData()
        searchView = binding.searchView
        btnAddMov = binding.btnAddMovement
        rvAllMovements = binding.rvAllMovements

        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvAllMovements.layoutManager = mLayoutManager

        adapter = AllMovementsAdapter(requireContext(),data)
        dataFiltered.addAll(data)

        rvAllMovements.adapter = adapter
    }
/*
    private fun createFakeData() {
        data.clear()

        // Primer mes
        val movements1 = listOf(
            MonthMovementsResponse.Movement(1, "2022-01-01", "Movimiento 1.1 hola hola hola hola hola hola hola hola hola hola hola hola", 100.0, null, MonthMovementsResponse.Type.INCOME),
            MonthMovementsResponse.Movement(2, "2022-01-02", "Movimiento 1.2", -50.0, null, MonthMovementsResponse.Type.EXPENSE),
            MonthMovementsResponse.Movement(3, "2022-01-03", "Movimiento 1.3 hola hola hola hola hola hola hola hola hola", 20000.0, null, MonthMovementsResponse.Type.INCOME)
        )
        val month1 = MonthMovementsResponse(1, "Enero 2022", movements1)
        data.add(month1)

        // Segundo mes
        val movements2 = listOf(
            MonthMovementsResponse.Movement(1, "2022-02-01", "Movimiento 2.1", 150.0, null, MonthMovementsResponse.Type.INCOME),
            MonthMovementsResponse.Movement(2, "2022-02-02", "Movimiento 2.2", -75.0, null, MonthMovementsResponse.Type.EXPENSE),
            MonthMovementsResponse.Movement(3, "2022-02-03", "Movimiento 2.3", 300.0, null, MonthMovementsResponse.Type.INCOME)
        )
        val month2 = MonthMovementsResponse(2, "Febrero 2022", movements2)
        data.add(month2)

        // Tercer mes
        val movements3 = listOf(
            MonthMovementsResponse.Movement(1, "2022-03-01", "Movimiento 3.1", 200.0, null, MonthMovementsResponse.Type.INCOME),
            MonthMovementsResponse.Movement(2, "2022-03-02", "Movimiento 3.2", -100.0, null, MonthMovementsResponse.Type.EXPENSE),
            MonthMovementsResponse.Movement(3, "2022-03-03", "Movimiento 3.3", 400.0, null, MonthMovementsResponse.Type.INCOME)
        )
        val month3 = MonthMovementsResponse(3, "Marzo 2022", movements3)
        data.add(month3)
    }
*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}