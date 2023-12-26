package com.miguelrodriguez19.mindmaster.views.expenses.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentBarsChartBinding
import com.miguelrodriguez19.mindmaster.models.structures.dto.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.models.utils.Preferences
import com.miguelrodriguez19.mindmaster.models.viewModels.expenses.ExpensesViewModel

class BarsChartFragment : Fragment() {
    private var _binding: FragmentBarsChartBinding? = null
    private val binding get() = _binding!!
    private val expensesViewModel: ExpensesViewModel by activityViewModels()
    private val listIncomes: ArrayList<BarEntry> = ArrayList()
    private val listExpenses: ArrayList<BarEntry> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarsChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expensesViewModel.allMonths.observe(
            viewLifecycleOwner,
            this@BarsChartFragment::setBarsChart
        )

        binding.pbLoadingBarsChart.visibility = View.VISIBLE
        binding.barChart.visibility = View.INVISIBLE
    }

    private fun setBarsChart(movements: List<MonthMovementsResponse>) {
        binding.barChart.visibility = View.INVISIBLE
        fillIncomeExpenseList(movements)
        val contrastColor = if (Preferences.getTheme().toInt() == 1) {
            Color.WHITE
        } else {
            Color.BLACK
        }

        val barDataSetIncomes = BarDataSet(listIncomes, getString(R.string.incomes)).apply {
            setColors(ResourcesCompat.getColor(resources, R.color.green_jade_500, null))
            valueTextColor = contrastColor
            valueTextSize = 10f
            setDrawValues(false)
        }

        val barDataSetExpenses = BarDataSet(listExpenses, getString(R.string.expenses)).apply {
            setColors(ResourcesCompat.getColor(resources, R.color.red_bittersweet_200, null))
            valueTextColor = contrastColor
            valueTextSize = 10f
            setDrawValues(false)
        }

        val barWidth = 0.4f
        val groupSpace = 0.2f
        val barSpace = 0f

        val barData = BarData(barDataSetIncomes, barDataSetExpenses).apply {
            this.barWidth = barWidth
        }

        with(binding.barChart) {
            data = barData
            invalidate() // Update graph

            xAxis.axisMinimum = 0.7f
            xAxis.axisMaximum = 13.2f

            barData.groupBars(0.9f, groupSpace, barSpace)

            legend.textColor = contrastColor
            description.text = getString(R.string.time_annual)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = contrastColor
            axisLeft.textColor = contrastColor
            axisLeft.axisMinimum = 0f // set the start in 0 and disable -y values

            axisRight.textColor = contrastColor
            axisRight.isEnabled = false // disable -x values

            setFitBars(true)

            visibility = View.VISIBLE
        }
        binding.pbLoadingBarsChart.visibility = View.GONE
    }

    private fun fillIncomeExpenseList(movements: List<MonthMovementsResponse>) {
        listIncomes.clear()
        listExpenses.clear()

        val regexPattern = "(\\d{2})-(\\d{4})".toRegex()
        val incomeByMonth = mutableMapOf<Int, Float>()
        val expensesByMonth = mutableMapOf<Int, Float>()

        for (movement in movements) {
            val matchResult = regexPattern.find(movement.date)

            if (matchResult != null) {
                val month = matchResult.groupValues[1].toInt()

                movement.incomeList.forEach { income ->
                    val currentIncomeForMonth = incomeByMonth.getOrDefault(month, 0f)
                    incomeByMonth[month] = currentIncomeForMonth + income.amount
                }
                movement.expensesList.forEach { expense ->
                    val currentExpenseForMonth = expensesByMonth.getOrDefault(month, 0f)
                    expensesByMonth[month] = currentExpenseForMonth + expense.amount
                }
            }
        }

        for (month in 1..12) {
            val incomeForMonth = incomeByMonth[month] ?: 0f
            listIncomes.add(
                BarEntry(
                    month.toFloat() - 0.4f,
                    incomeForMonth
                )
            ) // Desplaza las barras de ingresos a la izquierda

            val expensesForMonth = expensesByMonth[month] ?: 0f
            listExpenses.add(
                BarEntry(
                    month.toFloat() - 0.2f,
                    expensesForMonth
                )
            ) // Desplaza las barras de gastos a la derecha
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
