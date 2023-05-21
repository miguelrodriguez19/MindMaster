package com.miguelrodriguez19.mindmaster.views.expenses.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentPieChartBinding
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.getAmount

class PieChartFragment : Fragment() {
    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var pieChart: PieChart
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
        pieChart = binding.pieChart
    }

    fun setPieChart(moveResp: MonthMovementsResponse) {
        values.clear()
        val incomesValue = getAmount(moveResp.incomeList)
        val expensesValue = getAmount(moveResp.expensesList)
        values.add(PieEntry(incomesValue, requireContext().getString(R.string.incomes)))
        values.add(PieEntry(expensesValue, requireContext().getString(R.string.expenses)))

        val pieDataSet = PieDataSet(values, "")
        pieDataSet.setColors(ResourcesCompat.getColor(resources, R.color.green_jade_500, null))
        pieDataSet.addColor(ResourcesCompat.getColor(resources, R.color.red_bittersweet_200, null))
        pieDataSet.valueTextColor = Color.WHITE
        pieDataSet.valueTextSize = 15f

        val pieData = PieData(pieDataSet)

        pieChart.data = pieData

        pieChart.description.text = moveResp.date

        pieChart.centerText = "${incomesValue - expensesValue}€"
        pieChart.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.white, null))
        pieChart.setHoleColor(ResourcesCompat.getColor(resources, R.color.white, null))
        pieChart.animateY(200)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}