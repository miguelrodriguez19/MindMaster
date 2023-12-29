package com.miguelrodriguez19.mindmaster.view.fragments.expenses

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentPieChartBinding
import com.miguelrodriguez19.mindmaster.model.formatters.ChartsValueFormatter
import com.miguelrodriguez19.mindmaster.model.structures.dto.expenses.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.model.utils.Preferences
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.getAmount
import com.miguelrodriguez19.mindmaster.model.viewModels.expenses.ExpensesViewModel

class PieChartFragment : Fragment() {
    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!
    private val expensesViewModel: ExpensesViewModel by activityViewModels()
    private val values: ArrayList<PieEntry> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expensesViewModel.actualMonth.observe(
            viewLifecycleOwner,
            this@PieChartFragment::setPieChart
        )
        binding.pbLoadingPieChart.visibility = View.VISIBLE
        binding.pieChart.visibility = View.INVISIBLE
    }

    private fun setPieChart(moveResp: MonthMovementsResponse) {
        values.clear()
        binding.pieChart.visibility = View.INVISIBLE
        val contrastColor = if (Preferences.getTheme().toInt() == 1){
            Color.WHITE
        }else{
            Color.BLACK
        }
        val incomesValue = getAmount(moveResp.incomeList)
        val expensesValue = getAmount(moveResp.expensesList)
        values.add(PieEntry(incomesValue, requireContext().getString(R.string.incomes)))
        values.add(PieEntry(expensesValue, requireContext().getString(R.string.expenses)))

        val pieDataSet = PieDataSet(values, "")
        pieDataSet.setColors(ResourcesCompat.getColor(resources, R.color.green_jade_500, null))
        pieDataSet.addColor(ResourcesCompat.getColor(resources, R.color.red_bittersweet_200, null))
        pieDataSet.valueTextColor = contrastColor
        pieDataSet.valueTextSize = 15f

        val pieData = PieData(pieDataSet)
        pieData.setValueFormatter(ChartsValueFormatter())

        val balance = ChartsValueFormatter.getFormattedValue(incomesValue - expensesValue)

        with(binding.pieChart) {
            data = pieData
            description.text = moveResp.date
            description.textColor = contrastColor
            description.textSize = 16f
            centerText = "${getString(R.string.balance)}:\n${balance}"
            legend.textSize = 10f
            legend.textColor = contrastColor
            setCenterTextColor(contrastColor)
            setDrawEntryLabels(false)
            setBackgroundColor(ResourcesCompat.getColor(resources, R.color.transparent, null))
            setHoleColor(ResourcesCompat.getColor(resources, R.color.transparent, null))

            visibility = View.VISIBLE
            binding.pbLoadingPieChart.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}