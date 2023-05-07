package com.miguelrodriguez19.mindmaster.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.miguelrodriguez19.mindmaster.R
import java.text.SimpleDateFormat
import java.util.*

class AllDialogs {
    companion object {
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
            intArrayOf(
                R.color.red_extralight,
                R.color.red_light,
                R.color.red_standard,
                R.color.red_extradark
            ),
            intArrayOf(
                R.color.orange_extralight,
                R.color.orange_light,
                R.color.orange_standard,
                R.color.orange_extradark
            ),
            intArrayOf(
                R.color.yellow_extralight,
                R.color.yellow_light,
                R.color.yellow_standard,
                R.color.yellow_extradark
            ),
            intArrayOf(
                R.color.green_extralight,
                R.color.green_light,
                R.color.green_standard,
                R.color.green_extradark
            ),
            intArrayOf(
                R.color.turquoise_extralight,
                R.color.turquoise_light,
                R.color.turquoise_standard,
                R.color.turquoise_extradark
            ),
            intArrayOf(
                R.color.blue_extralight,
                R.color.blue_light,
                R.color.blue_standard,
                R.color.blue_extradark
            ),
            intArrayOf(
                R.color.navy_extralight,
                R.color.navy_light,
                R.color.navy_standard,
                R.color.navy_extradark
            ),
            intArrayOf(
                R.color.magenta_extralight,
                R.color.magenta_light,
                R.color.magenta_standard,
                R.color.magenta_extradark
            ),
            intArrayOf(
                R.color.gray_extralight,
                R.color.gray_light,
                R.color.gray_standard,
                R.color.gray_extradark
            )
        )

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
        fun showDateTimePicker(context: Context, listener: (String) -> Unit) {
            val currentDate = Calendar.getInstance()
            val dateListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedDate.set(Calendar.MINUTE, minute)

                    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    val selectedDateTime = dateFormat.format(selectedDate.time)
                    listener(selectedDateTime)
                }

                val timePickerDialog = TimePickerDialog(context, timeListener,
                    currentDate.get(Calendar.HOUR_OF_DAY),
                    currentDate.get(Calendar.MINUTE), false)
                timePickerDialog.show()
            }

            val datePickerDialog = DatePickerDialog(context, dateListener,
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        fun showDatePicker(context:Context, callback: (String) -> Unit) {
            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, monthOfYear, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, monthOfYear)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val formattedDate = String.format("%02d-%02d-%04d",
                        dayOfMonth,
                        monthOfYear + 1,
                        year
                    )

                    callback(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.show()
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