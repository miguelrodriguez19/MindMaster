package com.miguelrodriguez19.mindmaster.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object Toolkit {
    val PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,12}\$")

    fun checkFields(context: Context, tilArr: Array<TextInputLayout>, callback: (Boolean) -> Unit) {
        var flag = true
        for (til in tilArr) {
            if (til.editText!!.text.isNullOrBlank()) {
                til.error = context.getString(R.string.field_obligatory)
                flag = false
            } else {
                til.error = null
            }
        }
        callback(flag)
    }

    fun compareDates(date1Str: String, date2Str: String): Int {
        val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val date1 = format.parse(date1Str)
        val date2 = format.parse(date2Str)
        return date1.compareTo(date2)
    }

    fun showToast(context: Context, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun makeChip(context:Context, text: String): View {
        val chip = Chip(context, null, R.style.ChipStyle)
        chip.text = text
        chip.isClickable = true
        chip.isCheckable = true
        chip.isCloseIconVisible = true
        return chip
    }
}