package com.miguelrodriguez19.mindmaster.view.bottomSheets

import android.app.Activity
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
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Reminder
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.DEFAULT_DATE_FORMAT
import com.miguelrodriguez19.mindmaster.model.utils.Preferences
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReminderBS : CustomBottomSheet<Reminder>() {
    private lateinit var bind: BottomSheetReminderBinding
    private lateinit var repetitionArr: Array<String>
    private lateinit var arrAdapter: ArrayAdapter<String>

    override fun showViewDetailBS(
        activity: Activity,
        obj: Reminder?,
        date: LocalDate,
        callback: (Reminder) -> Unit
    ) {
        MaterialDialog(activity, BottomSheet(LayoutMode.MATCH_PARENT)).show {
            customView(
                R.layout.bottom_sheet_reminder, scrollable = true, horizontalPadding = true
            )
            cornerRadius(res = R.dimen.corner_radius_bottom_sheets)
            setPeekHeight(Toolkit.getPeekHeight(activity))

            bind = BottomSheetReminderBinding.bind(getCustomView())
            var color = obj?.colorTag ?: String.format(
                "#%06X", 0xFFFFFF and ContextCompat.getColor(activity, R.color.primaryColor)
            )
            repetitionArr = activity.resources.getStringArray(R.array.repetition_enum)
            arrAdapter = ArrayAdapter(activity, R.layout.dropdown_item, repetitionArr)
            bind.atvRepetition.setAdapter(arrAdapter)

            if (obj != null) {
                fillData(activity, obj)
            }

            bind.etDate.let {
                if (obj == null) it.setText(date.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                it.setOnClickListener {
                    AllDialogs.showDatePicker(activity) { date ->
                        bind.etDate.setText(date)
                    }
                }
            }
            bind.cgCategory.setOnCheckedChangeListener { _, checkedId ->
                val chip = bind.cgCategory.findViewById<Chip>(checkedId)
                bind.cgCategory.removeView(chip)
            }
            bind.btnAddCategory.setOnClickListener {
                if ((bind.etCategory.text ?: "").isNotBlank() && bind.cgCategory.size < 3) {
                    val chip = Toolkit.makeChip(activity, bind.etCategory.text.toString())
                    bind.cgCategory.addView(chip)
                    bind.tilCategory.error = null
                    bind.etCategory.text = null
                } else {
                    if (bind.cgCategory.size >= 3) {
                        bind.tilCategory.error =
                            activity.getString(R.string.reached_max_categories)
                    } else {
                        bind.tilCategory.error = activity.getString(R.string.type_anything)
                    }
                }
            }
            bind.etColorTag.setOnClickListener {
                AllDialogs.colorPickerDialog(activity) {
                    val colorStateList = ColorStateList.valueOf(Color.parseColor(it))
                    bind.tilColorTag.setStartIconTintList(colorStateList)
                    color = it
                }
            }
            bind.btnClose.setOnClickListener {
                AllDialogs.showConfirmationDialog(
                    activity,
                    activity.getString(R.string.changes_will_lost),
                    activity.getString(R.string.confirm_lost_changes_message)
                ) {
                    if (it) {
                        dismiss()
                    }
                }
            }
            bind.efabSave.setOnClickListener {
                Toolkit.checkFields(activity, arrayOf(bind.tilTitle, bind.tilDate)) { ok ->
                    if (ok) {
                        val reminder = Reminder(
                            uid = "",
                            title = bind.etTitle.text.toString(),
                            dateTime = bind.etDate.text.toString(),
                            description = bind.etDescription.text.toString(),
                            category = Toolkit.processChipGroup(bind.cgCategory),
                            colorTag = color,
                            repetition = Repetition.values()[arrAdapter.getPosition(bind.atvRepetition.text.toString())],
                            type = ActivityType.REMINDER,
                            notificationId = Preferences.getNextNotificationId()
                        )
                        if (obj == null) {
                            FirestoreManagerFacade.saveInSchedule(reminder) { added ->
                                added.createNotification(activity)
                                callback(added as Reminder)
                            }
                        } else {
                            FirestoreManagerFacade.updateInSchedule(reminder.copy(uid = obj.uid)) {
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