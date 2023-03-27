package com.miguelrodriguez19.mindmaster.expenses

import android.graphics.Color
import android.graphics.Color.GREEN
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.FragmentPieChartBinding

class PieChartFragment : Fragment() {
    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var pieChart: PieChart

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


        val list: ArrayList<PieEntry> = ArrayList()

        list.add(PieEntry(100f, getString(R.string.incomes)))
        list.add(PieEntry(60f, getString(R.string.expenses)))
        val pieDataSet = PieDataSet(list, "")

        pieDataSet.setColors(ResourcesCompat.getColor(resources, R.color.green_jade_500, null))
        pieDataSet.addColor(ResourcesCompat.getColor(resources, R.color.red_bittersweet_200, null))
        pieDataSet.valueTextColor = Color.WHITE
        pieDataSet.valueTextSize = 15f

        val pieData = PieData(pieDataSet)

        pieChart.data = pieData

        pieChart.description.text = ""

        pieChart.centerText = ""
        pieChart.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.white, null))
        pieChart.setHoleColor(ResourcesCompat.getColor(resources, R.color.white, null))
        pieChart.animateY(200)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}