package com.miguelrodriguez19.mindmaster.expenses

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentExpensesBinding
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.utils.AllBottomSheets.Companion.showMovementBS
import kotlin.collections.ArrayList

class ExpensesFragment : Fragment() {
    private val TAG = "ExpensesFragment"
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvLastMovements: RecyclerView
    private lateinit var movementAdapter: MovementAdapter
    private lateinit var pbLoading: View
    private lateinit var btnSeeAllMovements: Button
    private lateinit var btnAddExpense: ExtendedFloatingActionButton
    private lateinit var btnAddIncome: ExtendedFloatingActionButton
    private lateinit var pageAdapter: ViewPagerAdapter
    var data = arrayListOf(
        MonthMovementsResponse.Movement(1, "2022-01-01", "Movimiento 1.1",
            100.0, "", MonthMovementsResponse.Type.INCOME),
        MonthMovementsResponse.Movement(2, "2022-01-02", "Movimiento 1.2", -50.0, "", MonthMovementsResponse.Type.EXPENSE),
        MonthMovementsResponse.Movement(3, "2022-01-03", "Movimiento 1.3", 200.0, null, MonthMovementsResponse.Type.INCOME)
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
        initTabs()
        // Related to recycler view
        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvLastMovements.layoutManager = mLayoutManager

        movementAdapter = MovementAdapter(requireContext(), data) { movement ->
            Log.i(TAG, "onViewCreated - event: ${movement.concept}")
        }
        rvLastMovements.adapter = movementAdapter

        // On CLick Listeners
        btnSeeAllMovements.setOnClickListener {
            val action = ExpensesFragmentDirections.actionExpensesFragmentToAllMovementsFragment()
            findNavController().navigate(action)
        }
        btnAddExpense.setOnClickListener {
            showMovementBS(requireContext(), null, MonthMovementsResponse.Type.EXPENSE)
        }
        btnAddIncome.setOnClickListener {
            showMovementBS(requireContext(), null, MonthMovementsResponse.Type.INCOME)
        }

        pbLoading.visibility = View.GONE

    }

    private fun initTabs() {
        pageAdapter = ViewPagerAdapter(childFragmentManager)
        pageAdapter.addFragment(PieChartFragment(), resources.getString(R.string.monthly))
        pageAdapter.addFragment(BarsChartFragment(), resources.getString(R.string.annual))
        binding.viewPagerGraphs.adapter = pageAdapter
        binding.tlTabsGraph.setupWithViewPager(binding.viewPagerGraphs)
    }

    private fun initWidgets() {
        rvLastMovements = binding.rvLatestMovements
        pbLoading = binding.pbLoading
        btnSeeAllMovements = binding.btnSeeAllMovements
        btnAddExpense = binding.efabAddExpense
        btnAddIncome = binding.efabAddIncome

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    private val mFrgmentList = ArrayList<Fragment>()
    private val mFrgmentTitleList = ArrayList<String>()
    override fun getCount() = mFrgmentList.size
    override fun getItem(position: Int) = mFrgmentList[position]
    override fun getPageTitle(position: Int) = mFrgmentTitleList[position]
    fun addFragment(fragment: Fragment, title: String) {
        mFrgmentList.add(fragment)
        mFrgmentTitleList.add(title)
    }
}