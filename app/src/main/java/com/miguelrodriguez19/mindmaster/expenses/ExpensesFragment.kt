package com.miguelrodriguez19.mindmaster.expenses

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentExpensesBinding
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse.Movement
import com.miguelrodriguez19.mindmaster.models.comparators.MovementComparator
import com.miguelrodriguez19.mindmaster.utils.AllBottomSheets.Companion.showMovementBS
import com.miguelrodriguez19.mindmaster.utils.FirebaseManager.loadActualMonthMovements
import com.miguelrodriguez19.mindmaster.utils.Toolkit
import com.miguelrodriguez19.mindmaster.utils.Toolkit.getCurrentDate
import com.miguelrodriguez19.mindmaster.utils.Toolkit.getMonthYearOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpensesFragment : Fragment() {
    private val TAG = "ExpensesFragment"
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvLastMovements: RecyclerView
    private lateinit var movementAdapter: MovementAdapter
    private lateinit var btnSeeAllMovements: Button
    private lateinit var btnAddExpense: ExtendedFloatingActionButton
    private lateinit var pbLoading: View
    private lateinit var btnAddIncome: ExtendedFloatingActionButton
    private lateinit var pageAdapter: ViewPagerAdapter
    private val data = ArrayList<Movement>()
    private val latestMoves = ArrayList<Movement>()
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
        CoroutineScope(Dispatchers.Main).launch {
            setUpData(getMonthYearOf(getCurrentDate()))
        }

        // On CLick Listeners
        btnSeeAllMovements.setOnClickListener {
            val action = ExpensesFragmentDirections.actionExpensesFragmentToAllMovementsFragment()
            findNavController().navigate(action)
        }
        btnAddExpense.setOnClickListener {
            showMovementBS(requireContext(), null, MonthMovementsResponse.Type.EXPENSE) {
                addToLastestMoves(it)
            }
        }
        btnAddIncome.setOnClickListener {
            showMovementBS(requireContext(), null, MonthMovementsResponse.Type.INCOME) {
                addToLastestMoves(it)
            }
        }

        pbLoading.visibility = View.GONE
    }

    private fun addToLastestMoves(move: Movement) {
        if (move.date.matches(("..-" + getMonthYearOf(getCurrentDate())).toRegex())) {
            if (latestMoves.size>=3){
                latestMoves.removeAt(0)
            }else{
                latestMoves.add(move)
                movementAdapter.setData(latestMoves)
            }
        }
    }

    private suspend fun setUpData(currentMonth: String) {
        pbLoading.visibility = View.VISIBLE
        this@ExpensesFragment.data.clear()
        this@ExpensesFragment.latestMoves.clear()
        val eventMove = loadActualMonthMovements(
            requireContext(), currentMonth
        )
        val moves = ArrayList(eventMove.expensesList + eventMove.incomeList)
        moves.sortedWith(MovementComparator())
        this@ExpensesFragment.data.addAll(moves)
        val max = requireContext().getString(R.string.max_movements_to_show).toInt()
        this@ExpensesFragment.latestMoves.addAll(moves.reversed().subList(0, moves.size.coerceAtMost(max)))
        movementAdapter.setData(latestMoves)
        //val pieChart =
        //    requireActivity().supportFragmentManager.findFragmentById(R.id.pieChartFragment) as PieChartFragment
        //pieChart.setPieChart(eventMove)
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

        // Related to recycler view
        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        rvLastMovements.layoutManager = mLayoutManager

        movementAdapter = MovementAdapter(requireContext(), data) { movement ->
            Log.i(TAG, "onViewCreated - event: ${movement.concept}")
        }
        rvLastMovements.adapter = movementAdapter
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