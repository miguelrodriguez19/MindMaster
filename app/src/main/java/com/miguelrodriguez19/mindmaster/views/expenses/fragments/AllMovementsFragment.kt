package com.miguelrodriguez19.mindmaster.views.expenses.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.miguelrodriguez19.mindmaster.databinding.FragmentAllMovementsBinding
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.models.utils.AllBottomSheets.Companion.showMovementBS
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager
import com.miguelrodriguez19.mindmaster.views.expenses.adapters.AllMovementsAdapter

class AllMovementsFragment : Fragment() {

    private var _binding: FragmentAllMovementsBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private lateinit var btnAddMov: ExtendedFloatingActionButton
    private lateinit var rvAllMovements: RecyclerView
    private lateinit var adapter: AllMovementsAdapter
    private var data: ArrayList<MonthMovementsResponse> = ArrayList()
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
        setUpData()

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
            showMovementBS(requireContext(), null, null){
                adapter.addItem(it)
            }
        }
    }

    private fun search(text: String) {
        val filteredData = ArrayList<MonthMovementsResponse>()
        for (item in data) {
            val filteredIncome = ArrayList<MonthMovementsResponse.Movement>()
            val filteredExpense = ArrayList<MonthMovementsResponse.Movement>()
            val expensesList = item.expensesList
            val incomeList = item.incomeList
            for (movement in expensesList) {
                if (movement.concept.contains(text, true) || movement.date.contains(text, true)) {
                    filteredExpense.add(movement)
                }
            }
            for (movement in incomeList) {
                if (movement.concept.contains(text, true) || movement.date.contains(text, true)) {
                    filteredIncome.add(movement)
                }
            }
            if (filteredExpense.isNotEmpty() || filteredIncome.isNotEmpty()) {
                filteredData.add(MonthMovementsResponse(item.date, filteredIncome, filteredExpense))
            }
        }
        adapter.data = filteredData
        adapter.notifyDataSetChanged()
    }

    private fun setUpData() {
        binding.progressBarAllMovements.visibility = View.VISIBLE
        this@AllMovementsFragment.data.clear()
        FirebaseManager.loadAllMovements() { monthResponsesList ->
            this@AllMovementsFragment.data.addAll(monthResponsesList)
            dataFiltered = data
            adapter.setData(monthResponsesList)
            binding.progressBarAllMovements.visibility = View.GONE
        }
    }

    private fun initWidget() {
        searchView = binding.searchView
        btnAddMov = binding.btnAddMovement
        rvAllMovements = binding.rvAllMovements

        rvAllMovements.layoutManager = StaggeredGridLayoutManager(1, 1)
        adapter = AllMovementsAdapter(requireContext(), data)
        rvAllMovements.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}