package com.miguelrodriguez19.mindmaster.views.expenses.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentExpensesBinding
import com.miguelrodriguez19.mindmaster.models.comparators.MovementComparator
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager.loadActualMonthMovements
import com.miguelrodriguez19.mindmaster.models.structures.dto.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.models.structures.dto.MonthMovementsResponse.Movement
import com.miguelrodriguez19.mindmaster.models.utils.AllBottomSheets.Companion.showMovementBS
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.getCurrentDate
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.getMonthYearOf
import com.miguelrodriguez19.mindmaster.models.viewModels.expenses.ExpensesViewModel
import com.miguelrodriguez19.mindmaster.views.expenses.adapters.MovementAdapter
import com.miguelrodriguez19.mindmaster.views.expenses.adapters.NonSwipeableViewPager
import com.miguelrodriguez19.mindmaster.views.expenses.adapters.ViewPagerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpensesFragment : Fragment() {
    private val TAG = "ExpensesFragment"
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private val expensesViewModel: ExpensesViewModel by activityViewModels()

    private val movementAdapter: MovementAdapter by lazy {
        MovementAdapter(requireContext(), arrayListOf()) { movement ->
            Log.i(TAG, "onViewCreated - event: ${movement.concept}")
        }
    }
    private val data = mutableListOf<Movement>()
    private val latestMoves = mutableListOf<Movement>()
    private lateinit var lastMonthPicked: MonthMovementsResponse

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
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.btnSeeAllMovements.setOnClickListener {
            val action = ExpensesFragmentDirections.actionExpensesFragmentToAllMovementsFragment()
            findNavController().navigate(action)
        }

        binding.efabAddExpense.setOnClickListener {
            showMovementBS(requireContext(), null, MonthMovementsResponse.Type.EXPENSE) {
                addToLatestMoves(it)
                updateLiveData(it)
            }
        }
        binding.efabAddIncome.setOnClickListener {
            showMovementBS(requireContext(), null, MonthMovementsResponse.Type.INCOME) {
                addToLatestMoves(it)
                updateLiveData(it)
            }
        }
    }

    private fun updateLiveData(movement: Movement) {
        val date = movement.date.split("-")
        val regex = ("..-${date[1]}-${date[2]}").toRegex()
        if (getCurrentDate().matches(regex)) {
            lastMonthPicked = when (movement.type) {
                MonthMovementsResponse.Type.INCOME -> {
                    val list = ArrayList(lastMonthPicked.incomeList)
                    list.add(movement)
                    lastMonthPicked.copy(incomeList = list)
                }
                MonthMovementsResponse.Type.EXPENSE -> {
                    val list = ArrayList(lastMonthPicked.expensesList)
                    list.add(movement)
                    lastMonthPicked.copy(expensesList = list)
                }
            }
            expensesViewModel.setActualMonth(lastMonthPicked)
        }
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseManager.loadAllMovements() {
                expensesViewModel.setAllMonths(it)
            }
        }
    }

    private fun addToLatestMoves(move: Movement) {
        if (move.date.matches(("..-" + getMonthYearOf(getCurrentDate())).toRegex())) {
            if (latestMoves.size >= 3) {
                latestMoves.removeAt(0)
            }
            latestMoves.add(move)
            movementAdapter.setData(latestMoves)
        }
    }

    private suspend fun setUpData(currentMonth: String) {
        binding.pbLoading.visibility = View.VISIBLE
        data.clear()
        latestMoves.clear()

        withContext(Dispatchers.IO) {
            FirebaseManager.loadAllMovements() {
                expensesViewModel.setAllMonths(it)
            }
        }

        val eventMove = loadActualMonthMovements(currentMonth)
        val moves = eventMove.expensesList + eventMove.incomeList
        moves.sortedWith(MovementComparator())

        data.addAll(moves)
        val max = requireContext().getString(R.string.max_movements_to_show).toInt()
        latestMoves.addAll(moves.asReversed().take(max))
        lastMonthPicked = eventMove
        movementAdapter.setData(latestMoves)
        expensesViewModel.setActualMonth(eventMove)

        binding.pbLoading.visibility = View.GONE
    }

    private fun initTabs() {
        val pageAdapter = ViewPagerAdapter(childFragmentManager)
        pageAdapter.addFragment(PieChartFragment(), resources.getString(R.string.time_monthly))
        pageAdapter.addFragment(BarsChartFragment(), resources.getString(R.string.time_annual))
        (binding.viewPagerGraphs as NonSwipeableViewPager).adapter = pageAdapter
        binding.tlTabsGraph.setupWithViewPager(binding.viewPagerGraphs)
    }

    private fun initWidgets() {
        val mLayoutManager = StaggeredGridLayoutManager(1, 1)
        binding.rvLatestMovements.layoutManager = mLayoutManager
        binding.rvLatestMovements.adapter = movementAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


