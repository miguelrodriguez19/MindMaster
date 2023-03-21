package com.miguelrodriguez19.mindmaster.expenses

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.calendar.CalendarEventsAdapter
import com.miguelrodriguez19.mindmaster.databinding.FragmentExpensesBinding
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse
import kotlin.collections.ArrayList

class ExpensesFragment : Fragment() {
    private val TAG = "ExpensesFragment"
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvLastMovements: RecyclerView
    private lateinit var adapter: LastMovementsAdapter
    private lateinit var pbLoading: View
    private lateinit var btnSeeAllMovements: Button
    private lateinit var btnAddExpense: ExtendedFloatingActionButton
    private lateinit var btnAddIncome: ExtendedFloatingActionButton
    var data = arrayListOf(
        MonthMovementsResponse.Movement(1, "2022-01-01", "Movimiento 1.1", 100f, "income"),
        MonthMovementsResponse.Movement(2, "2022-01-02", "Movimiento 1.2", -50f, "expense"),
        MonthMovementsResponse.Movement(3, "2022-01-03", "Movimiento 1.3", 200f, "income")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidgets()
        // Related to recycler view
        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvLastMovements.layoutManager = mLayoutManager
        adapter = LastMovementsAdapter(data) { movement ->
            Log.i(TAG, "onViewCreated - event: ${movement.title}")
        }
        rvLastMovements.adapter = adapter

        // On CLick Listeners
        btnSeeAllMovements.setOnClickListener {
            val action = ExpensesFragmentDirections.actionExpensesFragmentToAllMovementsFragment()
            findNavController().navigate(action)
        }
        btnAddExpense.setOnClickListener {

        }
        btnAddIncome.setOnClickListener {

        }

        pbLoading.visibility = View.GONE

    }

    private fun initWidgets() {
        rvLastMovements = binding.rvLatestMovements
        pbLoading = binding.pbLoading
        btnSeeAllMovements = binding.btnSeeAllMovements
        btnAddExpense = binding.efabAddExpense
        btnAddIncome = binding.efabAddIncome

        val adapter = TabsFragmentAdapter(
            parentFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        adapter.addItem(PieChartFragment(), resources.getString(R.string.monthly))
        adapter.addItem(BarsChartFragment(), resources.getString(R.string.annual))

        val viewPager = binding.viewPagerGraphs
        viewPager.adapter = adapter

        val tabsGraphLayout = binding.tlTabsGraph
        tabsGraphLayout.setupWithViewPager(viewPager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TabsFragmentAdapter(fm: FragmentManager, behavior: Int) :
    FragmentPagerAdapter(fm, behavior) {

    private val listFragment: MutableList<Fragment> = ArrayList()
    private val tittleList: MutableList<String> = ArrayList()

    fun addItem(fragment: Fragment, title: String) {
        listFragment.add(fragment)
        tittleList.add(title)
    }

    override fun getCount(): Int {
        return listFragment.size
    }

    override fun getItem(position: Int): Fragment {
        return listFragment[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return tittleList[position]
    }
}
