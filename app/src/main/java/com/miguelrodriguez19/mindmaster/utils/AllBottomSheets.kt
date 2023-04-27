package com.miguelrodriguez19.mindmaster.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.size
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.BottomSheetEventsBinding
import com.miguelrodriguez19.mindmaster.models.Event
import com.miguelrodriguez19.mindmaster.utils.AllDialogs.Companion.colorPickerDialog

class AllBottomSheets {
    companion object {
        fun showEventsBS(context: Context, e: Event?) {
            val botSheet = BottomSheetDialog(context)
            val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_events, null)
            botSheet.setContentView(bottomSheetView)
            val bind = BottomSheetEventsBinding.bind(bottomSheetView)
            if (e != null) {
                bind.etTitle.setText(e.title)
                bind.etStartTime.setText(e.start_time)
                bind.etEndTime.setText(e.end_time)
                bind.etLocation.setText(e.location)
                // bind.tilRepetition
                bind.etDescription.setText(e.description)
                bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(e.color_tag)))
                for (cat in e.category ?: ArrayList()) {
                    val chip = Chip(context)
                    chip.text = cat
                    chip.isClickable = true
                    chip.isCheckable = true
                    bind.cgCategory.addView(chip)
                }
                for (part in e.participants ?: ArrayList()) {
                    val chip = Chip(context)
                    chip.text = part
                    chip.isClickable = true
                    chip.isCheckable = true
                    bind.chipGroup.addView(chip)
                }
            }

            bind.btnAddCategory.setOnClickListener {
                if ((bind.etCategory.text ?: "").isNotBlank() && bind.cgCategory.size < 3){
                    val chip = Chip(context)
                    chip.text = bind.etCategory.text
                    chip.isClickable = true
                    chip.isCheckable = true
                    bind.cgCategory.addView(chip)
                    bind.tilCategory.error = null
                    bind.etCategory.text = null
                }
                else {
                    if (bind.cgCategory.size >= 3){
                        bind.tilCategory.error = context.getString(R.string.reached_max_categories)
                    }else {
                        bind.tilCategory.error = context.getString(R.string.fill_this_field)
                    }
                }
            }

            bind.etColorTag.setOnClickListener {
                colorPickerDialog(context) {
                    bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(it))
                }
            }

            bind.ibtnClose.setOnClickListener {
                botSheet.dismiss()
            }

            bind.efabSave.setOnClickListener {
                if (checkfields(context, arrayOf(bind.tilTitle, bind.tilStartTime, bind.tilEndTime))) {
                    botSheet.dismiss()
                }
            }

            botSheet.show()
        }

        private fun checkfields(context:Context, tilArr: Array<TextInputLayout>): Boolean {
            var flag = true
            for (til in tilArr) {
                if (til.editText!!.text.isBlank()) {
                    til.error = context.getString(R.string.fill_this_field)
                    flag = false
                } else {
                    til.error = null
                }
            }
            return flag;
        }
    }
}