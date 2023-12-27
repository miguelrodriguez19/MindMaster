package com.miguelrodriguez19.mindmaster.view.bottomSheets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.size
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.setPeekHeight
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.chip.Chip
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.BottomSheetReminderBinding
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.structures.dto.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Reminder
import com.miguelrodriguez19.mindmaster.model.structures.enums.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.Repetition
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs

class ReminderBS : CustomBottomSheet<Reminder>() {
    private lateinit var bind: BottomSheetReminderBinding
    private lateinit var repetitionArr: Array<String>
    private lateinit var arrAdapter: ArrayAdapter<String>

    override fun showViewDetailBS(context: Context, obj: Reminder?, callback: (Reminder) -> Unit) {
        MaterialDialog(context, BottomSheet(LayoutMode.MATCH_PARENT)).show {
            customView(
                R.layout.bottom_sheet_reminder, scrollable = true, horizontalPadding = true
            )
            cornerRadius(res = R.dimen.corner_radius_bottom_sheets)
            setPeekHeight(Toolkit.getPeekHeight(context))

            bind = BottomSheetReminderBinding.bind(getCustomView())
            var color = obj?.colorTag ?: String.format(
                "#%06X", 0xFFFFFF and ContextCompat.getColor(context, R.color.primaryColor)
            )
            repetitionArr = context.resources.getStringArray(R.array.repetition_enum)
            arrAdapter = ArrayAdapter(context, R.layout.dropdown_item, repetitionArr)
            bind.atvRepetition.setAdapter(arrAdapter)

            if (obj != null) {
                fillData(context, obj)
            }

            bind.etDate.setOnClickListener {
                AllDialogs.showDatePicker(context) { date ->
                    bind.etDate.setText(date)
                }
            }
            bind.cgCategory.setOnCheckedChangeListener { _, checkedId ->
                val chip = bind.cgCategory.findViewById<Chip>(checkedId)
                bind.cgCategory.removeView(chip)
            }
            bind.btnAddCategory.setOnClickListener {
                if ((bind.etCategory.text ?: "").isNotBlank() && bind.cgCategory.size < 3) {
                    val chip = Toolkit.makeChip(context, bind.etCategory.text.toString())
                    bind.cgCategory.addView(chip)
                    bind.tilCategory.error = null
                    bind.etCategory.text = null
                } else {
                    if (bind.cgCategory.size >= 3) {
                        bind.tilCategory.error =
                            context.getString(R.string.reached_max_categories)
                    } else {
                        bind.tilCategory.error = context.getString(R.string.type_anything)
                    }
                }
            }
            bind.etColorTag.setOnClickListener {
                AllDialogs.colorPickerDialog(context) {
                    val colorStateList = ColorStateList.valueOf(Color.parseColor(it))
                    bind.tilColorTag.setStartIconTintList(colorStateList)
                    color = it
                }
            }
            bind.btnClose.setOnClickListener {
                AllDialogs.showConfirmationDialog(
                    context,
                    context.getString(R.string.changes_will_lost),
                    context.getString(R.string.confirm_lost_changes_message)
                ) {
                    if (it) {
                        dismiss()
                    }
                }
            }
            bind.efabSave.setOnClickListener {
                Toolkit.checkFields(context, arrayOf(bind.tilTitle, bind.tilDate)) { ok ->
                    if (ok) {
                        val reminder = Reminder(
                            bind.etTitle.text.toString(),
                            bind.etDate.text.toString(),
                            bind.etDescription.text.toString(),
                            Toolkit.processChipGroup(bind.cgCategory),
                            color,
                            Repetition.values()[arrAdapter.getPosition(bind.atvRepetition.text.toString())],
                            ActivityType.REMINDER
                        )
                        if (obj == null) {
                            FirestoreManagerFacade.saveInSchedule(reminder) { added ->
                                callback(added as Reminder)
                            }
                        } else {
                            FirestoreManagerFacade.updateInSchedule(Reminder(obj.uid, reminder)) {
                                callback(it as Reminder)
                            }
                        }
                        dismiss()
                    }
                }
            }
        }
    }

    override fun fillData(context: Context, obj: Reminder) {
        bind.etTitle.setText(obj.title)
        bind.etDate.setText(obj.dateTime)
        bind.etDate.isEnabled = false

        // Set repetition dropdown selection
        bind.atvRepetition.apply {
            setText(
                repetitionArr[obj.repetition.ordinal],
                false
            ) // Directly use the enum index
            setAdapter(arrAdapter)
        }
        bind.etDescription.setText(obj.description)
        bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(obj.colorTag)))
        for (cat in obj.category ?: ArrayList()) {
            val chip = Toolkit.makeChip(context, cat)
            bind.cgCategory.addView(chip)
        }
    }
}