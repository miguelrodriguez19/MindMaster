package com.miguelrodriguez19.mindmaster.expenses

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentBarsChartBinding

class BarsChartFragment : Fragment() {
    private var _binding: FragmentBarsChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var barChart: BarChart
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarsChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barChart = binding.barChart

        val listIncomes: ArrayList<BarEntry> = ArrayList()
        for (i in 1..12) {
            listIncomes.add(BarEntry(i.toFloat(), (Math.random() * 100).toFloat()))
        }

        val listExpenses: ArrayList<BarEntry> = ArrayList()
        for (i in 1..12)
            listExpenses.add(BarEntry(i.toFloat(), (Math.random()*100).toFloat()))

        val barDataSetIncomes = BarDataSet(listIncomes, getString(R.string.incomes))
        barDataSetIncomes.setColors(ResourcesCompat.getColor(resources, R.color.green_jade_500, null))
        barDataSetIncomes.valueTextColor = Color.BLACK

        val barDataSetExpenses = BarDataSet(listExpenses, getString(R.string.expenses))
        barDataSetExpenses.setColors(ResourcesCompat.getColor(resources, R.color.red_bittersweet_200, null))
        barDataSetExpenses.valueTextColor = Color.BLACK


        val barData = BarData(barDataSetIncomes, barDataSetExpenses)
        barChart.setFitBars(false)
        barChart.data = barData

        barChart.description.text = getString(R.string.annual)
        barChart.animateY(200)

        barData.barWidth = 0.4f // Ancho de las barras

        val groupSpace = 0.1f // Separación entre los grupos de barras
        val barSpace = 0.02f // Separación entre las barras dentro de un grupo
        val groupCount = 12 // Cantidad de grupos (valores en el eje X)

        barData.groupBars(0f, groupSpace, barSpace)

        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.axisMinimum = 0f // Límite inferior del eje X
        barChart.xAxis.axisMaximum = 0 + barChart.barData.getGroupWidth(groupSpace, barSpace) * groupCount // Límite superior del eje X

        barChart.setFitBars(true) // Ajusta el tamaño de las barras al tamaño del gráfico
        barChart.data = barData
        barChart.invalidate() // Actualiza el gráfico

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}