package com.miguelrodriguez19.mindmaster.models.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse.Movement
import com.miguelrodriguez19.mindmaster.models.structures.UserResponse
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

    fun showToast(context: Context, message: Int) {
        Toast.makeText(context, context.getString(message), Toast.LENGTH_SHORT).show()
    }

    fun showUndoSnackBar(context: Context, parent: View, isUndone: (Boolean) -> Unit) {
        val snackbar = Snackbar.make(
            context, parent,
            context.getString(R.string.event_deleted),
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction(R.string.undo) {
            isUndone(true)
        }
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event != DISMISS_EVENT_ACTION) {
                    isUndone(false)
                }
            }
        })
        snackbar.show()
    }


    fun makeChip(context: Context, text: String): View {
        val chip = Chip(context, null, R.style.ChipStyle)
        chip.text = text
        chip.isClickable = true
        chip.isCheckable = true
        chip.isCloseIconVisible = true
        return chip
    }

    fun processChipGroup(cg: ChipGroup): List<String> {
        return cg.children
            .asSequence()
            .filterIsInstance<Chip>()
            .map { chip -> chip.text.toString() }
            .toList()
    }

    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return String.format("%02d-%02d-%04d", day, month, year)
    }

    fun getDateFromDatetime(dateTime: String): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = dateFormat.parse(dateTime)
        return dateFormat.format(date)
    }

    fun String.toUserResponse(): UserResponse {
        return Gson().fromJson(this, UserResponse::class.java)
    }

    fun UserResponse.toJson(): String {
        return Gson().toJson(this)
    }

    fun getMonthYearOf(currentDate: String): String {
        val date = currentDate.split("-")
        return "${date[1]}-${date[2]}"
    }


    fun getAmount(list: List<Movement>): Float {
        var amount = 0.0F
        for (move in list) {
            amount += move.amount
        }
        return amount
    }


}