package com.miguelrodriguez19.mindmaster.models.formatters

import com.github.mikephil.charting.formatter.ValueFormatter
import com.miguelrodriguez19.mindmaster.models.utils.Preferences
import java.text.DecimalFormat

class ChartsValueFormatter : ValueFormatter() {

    companion object{
        fun getFormattedValue(value: Float): String {
            return if (value % 1 == 0f) {
                "${value.toInt()}${Preferences.getCurrency()}"
            } else {
                val decimalFormat = DecimalFormat("0.00")
                "${decimalFormat.format(value)}${Preferences.getCurrency()}"
            }
        }
    }

    override fun getFormattedValue(value: Float): String {
        return if (value % 1 == 0f) {
            "${value.toInt()}${Preferences.getCurrency()}"
        } else {
            val decimalFormat = DecimalFormat("0.00")
            "${decimalFormat.format(value)}${Preferences.getCurrency()}"
        }
    }
}