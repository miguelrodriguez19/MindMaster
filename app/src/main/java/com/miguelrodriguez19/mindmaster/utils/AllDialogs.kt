package com.miguelrodriguez19.mindmaster.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.miguelrodriguez19.mindmaster.R
import java.util.regex.Pattern

class AllDialogs {
    companion object {
        val PASSWORD_PATTERN = Pattern.compile(
            "^" + "(?=.*[0-9])" + "(?=.*[a-z])" + "(?=.*[A-Z])" + "(?=.*[._!#$%])" + "(?=\\S+$)" + ".{4,}" + "$"
        )

        val colors = intArrayOf(
            R.color.red_standard,
            R.color.orange_standard,
            R.color.yellow_standard,
            R.color.green_standard,
            R.color.turquoise_standard,
            R.color.blue_standard,
            R.color.navy_standard,
            R.color.magenta_standard,
            R.color.gray_standard
        )

        val subColors = arrayOf( // size = 9
            intArrayOf(R.color.red_extralight,R.color.red_light,R.color.red_standard,R.color.red_extradark),
            intArrayOf(R.color.orange_extralight,R.color.orange_light,R.color.orange_standard,R.color.orange_extradark),
            intArrayOf(R.color.yellow_extralight,R.color.yellow_light,R.color.yellow_standard,R.color.yellow_extradark),
            intArrayOf(R.color.green_extralight,R.color.green_light,R.color.green_standard,R.color.green_extradark),
            intArrayOf(R.color.turquoise_extralight,R.color.turquoise_light,R.color.turquoise_standard,R.color.turquoise_extradark),
            intArrayOf(R.color.blue_extralight,R.color.blue_light,R.color.blue_standard,R.color.blue_extradark),
            intArrayOf(R.color.navy_extralight,R.color.navy_light,R.color.navy_standard,R.color.navy_extradark),
            intArrayOf(R.color.magenta_extralight,R.color.magenta_light,R.color.magenta_standard,R.color.magenta_extradark),
            intArrayOf(R.color.gray_extralight,R.color.gray_light,R.color.gray_standard,R.color.gray_extradark)
        )

        fun showToast(context: Context, message: String?) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun colorPickerDialog(context: Context, callback: (Int) -> Unit) {
            ColorPalette
            MaterialDialog(context).show {
                title(R.string.select_a_color)
                colorChooser(
                    ColorPalette.Primary,
                    subColors = ColorPalette.PrimarySub,
                    initialSelection = colors[6],
                    allowCustomArgb = true,
                    showAlphaSelector = false
                ) { dialog, color ->
                    callback(color)
                }
                negativeButton(R.string.cancel)
                positiveButton(R.string.select)
            }
        }

        fun showConfirmationDialog(
            context: Context, title: String, message: String?, callback: (Boolean) -> Unit
        ) {
            val alertDialog = AlertDialog.Builder(context).setTitle(title).setMessage(message ?: "")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    callback(true)
                }.setNegativeButton(android.R.string.cancel) { _, _ ->
                    callback(false)
                }.create()
            alertDialog.show()
        }

        fun showAlertDialog(
            context: Context, title: String, message: String?
        ) {
            val alertDialog = AlertDialog.Builder(context).setTitle(title).setMessage(message ?: "")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                }.create()
            alertDialog.show()
        }


    }
}