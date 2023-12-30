package com.miguelrodriguez19.mindmaster.model.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.structures.dto.expenses.Movement
import com.miguelrodriguez19.mindmaster.model.structures.dto.UserResponse
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Reminder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


object Toolkit {
    val PASSWORD_PATTERN: Pattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20}\$")

    fun checkFields(context: Context, tilArr: Array<TextInputLayout>, callback: (Boolean) -> Unit) {
        var flag = true
        for (til in tilArr) {
            if (til.editText!!.text.isNullOrBlank()) {
                til.isErrorEnabled = true
                til.error =
                    context.getString(com.miguelrodriguez19.mindmaster.R.string.field_obligatory)
                flag = false
            } else {
                til.error = null
                til.isErrorEnabled = false
            }
        }
        callback(flag)
    }


    fun showToast(context: Context, message: Int) =
        Toast.makeText(context, context.getString(message), Toast.LENGTH_SHORT).show()

    fun showUndoSnackBar(context: Context, parent: View, isUndone: (Boolean) -> Unit) {
        val snackbar = Snackbar.make(
            context, parent,
            context.getString(com.miguelrodriguez19.mindmaster.R.string.event_deleted),
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction(com.miguelrodriguez19.mindmaster.R.string.undo) {
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
        val chip = Chip(context, null, com.miguelrodriguez19.mindmaster.R.style.ChipStyle)
        chip.text = text
        chip.isClickable = true
        chip.isCheckable = true
        chip.isCloseIconVisible = true
        return chip
    }

    fun processChipGroup(cg: ChipGroup): List<String> = cg.children
        .filterIsInstance<Chip>()
        .map { chip -> chip.text.toString() }
        .toList()

    fun getAmount(list: List<Movement>): Float {
        var amount = 0.0F
        for (move in list) {
            amount += move.amount
        }
        return amount
    }

    fun getPeekHeight(context: Context): Int =
        context.resources.displayMetrics.heightPixels * context.getString(com.miguelrodriguez19.mindmaster.R.string.peek_height_percent_botSheet)
            .toInt() / 100

    fun getPeekHeight(context: Context, percent: Float): Int {
        if (percent < 0 || percent > 1) {
            throw IllegalArgumentException("The percentage must be in the range of 0 to 1.")
        }
        return (context.resources.displayMetrics.heightPixels * percent).toInt()
    }

    fun isPasswordStrong(password: String): Boolean = password.length >= 8
            && password.matches(".*[a-z].*".toRegex()) && password.matches(".*[A-Z].*".toRegex())
            && password.matches(".*\\d.*".toRegex()) && password.matches(".*[!@#$%&*()_+=|<>?{}\\[\\]~-].*".toRegex())

    @StringRes
    fun evaluatePasswordSecurity(password: String): Pair<Int, Int> {
        // Initialize with weak password
        var result = Pair(R.string.password_weak, R.color.red_error_500)

        // check for medium security: 8 or more characters with letters and numbers
        if (password.length >= 8 &&
            password.matches(".*[a-zA-Z].*".toRegex()) &&
            password.matches(".*\\d.*".toRegex())
        ) {
            result = Pair(R.string.password_medium, R.color.orange)
        }

        // check for strong security: 8 or more characters with lower & upper letters, numbers, and special characters
        if (password.length >= 8 &&
            password.matches(".*[a-z].*".toRegex()) &&
            password.matches(".*[A-Z].*".toRegex()) &&
            password.matches(".*\\d.*".toRegex()) &&
            password.matches(".*[!@#$%&*()_+=|<>?{}\\[\\]~-].*".toRegex())
        ) {
            result = Pair(R.string.password_strong, R.color.green_mantis_200)
        }
        return result
    }

    fun parseStringToByteArray(ivString: String): ByteArray {
        return Base64.getDecoder().decode(ivString)
    }

    fun parseByteArrayToString(iv:ByteArray):String{
        return Base64.getEncoder().encodeToString(iv)
    }
}