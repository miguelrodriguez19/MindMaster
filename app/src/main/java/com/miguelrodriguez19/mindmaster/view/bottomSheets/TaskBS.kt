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
import com.miguelrodriguez19.mindmaster.databinding.BottomSheetTasksBinding
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Task
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Priority
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Status
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.DEFAULT_DATE_FORMAT
import com.miguelrodriguez19.mindmaster.model.utils.Preferences
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskBS : CustomBottomSheet<Task>() {
    private lateinit var bind: BottomSheetTasksBinding
    private lateinit var statusArr: Array<String>
    private lateinit var statusAdapter: ArrayAdapter<String>
    private lateinit var priorityArr: Array<String>
    private lateinit var priorityAdapter: ArrayAdapter<String>

    override fun showViewDetailBS(
        activity: Activity,
        obj: Task?,
        date: LocalDate,
        callback: (Task) -> Unit
    ) {
        MaterialDialog(activity, BottomSheet(LayoutMode.MATCH_PARENT)).show {
            customView(R.layout.bottom_sheet_tasks, scrollable = true, horizontalPadding = true)
            cornerRadius(res = R.dimen.corner_radius_bottom_sheets)
            setPeekHeight(Toolkit.getPeekHeight(activity))

            bind = BottomSheetTasksBinding.bind(getCustomView())
            var color = obj?.colorTag ?: String.format(
                "#%06X", 0xFFFFFF and ContextCompat.getColor(activity, R.color.primaryColor)
            )

            // Status dropdown
            statusArr = activity.resources.getStringArray(R.array.status_enum)
            statusAdapter = ArrayAdapter(activity, R.layout.dropdown_item, statusArr)
            bind.atvStatus.setAdapter(statusAdapter)

            // Priority dropdown
            priorityArr = activity.resources.getStringArray(R.array.priority_enum)
            priorityAdapter = ArrayAdapter(activity, R.layout.dropdown_item, priorityArr)
            bind.atvPriority.setAdapter(priorityAdapter)

            if (obj != null) {
                fillData(activity, obj)
            }

            bind.etDueDate.let {
                if (obj == null) it.setText(date.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                it.setOnClickListener {
                    AllDialogs.showDatePicker(activity) { date ->
                        bind.etDueDate.setText(date)
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
                Toolkit.checkFields(activity, arrayOf(bind.tilTitle, bind.tilDueDate)) { ok ->
                    if (ok) {
                        val task = Task(
                            uid = "",
                            title = bind.etTitle.text.toString(),
                            dueDate = bind.etDueDate.text.toString(),
                            description = bind.etDescription.text.toString(),
                            priority = Priority.values()[priorityAdapter.getPosition(bind.atvPriority.text.toString())],
                            status = Status.values()[statusAdapter.getPosition(bind.atvStatus.text.toString())],
                            category = Toolkit.processChipGroup(bind.cgCategory),
                            colorTag = color,
                            type = ActivityType.TASK,
                            notificationId = Preferences.getNextNotificationId()
                        )
                        if (obj == null) {
                            FirestoreManagerFacade.saveInSchedule(task) { added ->
                                added.createNotification(activity)
                                callback(added as Task)
                            }
                        } else {
                            FirestoreManagerFacade.updateInSchedule(task.copy(uid = obj.uid)) {
                                callback(it as Task)
                            }
                        }
                        dismiss()
                    }
                }
            }
        }
    }

    override fun fillData(context: Context, obj: Task) {
        bind.etTitle.setText(obj.title)
        bind.etDueDate.setText(obj.dueDate)
        bind.etDueDate.isEnabled = false

        // Set status dropdown selection
        bind.atvStatus.apply {
            setText(
                statusArr[obj.status.ordinal],
                false
            ) // Directly use the enum index
            setAdapter(statusAdapter)
        }

        // Set priority dropdown selection
        bind.atvPriority.apply {
            setText(
                priorityArr[obj.priority.ordinal],
                false
            ) // Directly use the enum index
            setAdapter(priorityAdapter)
        }
        bind.etDescription.setText(obj.description)
        bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(obj.colorTag)))

        // Category Chips creation
        for (cat in obj.category ?: ArrayList()) {
            val chip = Toolkit.makeChip(context, cat)
            bind.cgCategory.addView(chip)
        }
    }
}