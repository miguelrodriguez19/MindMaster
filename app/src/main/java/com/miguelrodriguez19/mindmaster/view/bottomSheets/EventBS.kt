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
import com.miguelrodriguez19.mindmaster.databinding.BottomSheetEventsBinding
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Event
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.Repetition
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.compareDateTimes
import com.miguelrodriguez19.mindmaster.model.utils.Preferences
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.checkFields
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.getPeekHeight
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.makeChip
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.processChipGroup
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs.Companion.colorPickerDialog
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs.Companion.showConfirmationDialog
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs.Companion.showDateTimePicker

class EventBS : CustomBottomSheet<Event>() {
    private lateinit var bind: BottomSheetEventsBinding
    private lateinit var repetitionArr: Array<String>
    private lateinit var arrAdapter: ArrayAdapter<String>

    override fun showViewDetailBS(context: Context, obj: Event?, callback: (Event) -> Unit) {
        MaterialDialog(context, BottomSheet(LayoutMode.MATCH_PARENT)).show {
            customView(
                R.layout.bottom_sheet_events,
                scrollable = true,
                horizontalPadding = true
            )
            cornerRadius(res = R.dimen.corner_radius_bottom_sheets)
            setPeekHeight(getPeekHeight(context))

            bind = BottomSheetEventsBinding.bind(getCustomView())
            var color = obj?.colorTag ?: String.format(
                "#%06X", 0xFFFFFF and ContextCompat.getColor(context, R.color.primaryColor)
            )
            repetitionArr = context.resources.getStringArray(R.array.repetition_enum)
            arrAdapter = ArrayAdapter(context, R.layout.dropdown_item, repetitionArr)
            bind.atvRepetition.setAdapter(arrAdapter)

            if (obj != null) {
                fillData(context, obj)
            }

            bind.cgParticipants.setOnCheckedChangeListener { _, checkedId ->
                val chip = bind.cgParticipants.findViewById<Chip>(checkedId)
                bind.cgParticipants.removeView(chip)
            }

            bind.btnAddParticipant.setOnClickListener {
                if ((bind.etParticipants.text ?: "").isNotBlank()) {
                    val chip = makeChip(context, bind.etParticipants.text.toString())
                    bind.cgParticipants.addView(chip)
                    bind.tilParticipants.error = null
                    bind.etParticipants.text = null
                } else {
                    bind.tilParticipants.error = context.getString(R.string.type_anything)
                }
            }
            bind.etStartTime.setOnClickListener {
                showDateTimePicker(context) { datetime ->
                    bind.etStartTime.setText(datetime)
                    if (bind.etEndTime.text!!.isBlank()) {
                        bind.etEndTime.setText(datetime)
                    }
                }
            }
            bind.etEndTime.setOnClickListener {
                showDateTimePicker(context) { datetime ->
                    bind.etEndTime.setText(datetime)
                    bind.tilEndTime.error = null
                    if (bind.etStartTime.text!!.isNotBlank()) {
                        if (compareDateTimes(datetime, bind.etStartTime.text.toString()) < 0) {
                            bind.etEndTime.text = null
                            bind.tilEndTime.error =
                                context.getString(R.string.err_end_date_incorrect)
                        }
                    } else {
                        bind.etStartTime.setText(datetime)
                    }
                }
            }
            bind.cgCategory.setOnCheckedChangeListener { _, checkedId ->
                val chip = bind.cgCategory.findViewById<Chip>(checkedId)
                bind.cgCategory.removeView(chip)
            }

            bind.btnAddCategory.setOnClickListener {
                if ((bind.etCategory.text ?: "").isNotBlank() && bind.cgCategory.size < 3) {
                    val chip = makeChip(context, bind.etCategory.text.toString())
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
                colorPickerDialog(context) {
                    val colorStateList = ColorStateList.valueOf(Color.parseColor(it))
                    bind.tilColorTag.setStartIconTintList(colorStateList)
                    color = it
                }
            }

            bind.btnClose.setOnClickListener {
                showConfirmationDialog(
                    context, context.getString(R.string.changes_will_lost),
                    context.getString(R.string.confirm_lost_changes_message)
                ) {
                    if (it) {
                        dismiss()
                    }
                }
            }

            bind.efabSave.setOnClickListener {
                checkFields(
                    context, arrayOf(bind.tilTitle, bind.tilStartTime, bind.tilEndTime)
                ) { ok ->
                    if (ok) {
                        val repetition =
                            Repetition.values()[arrAdapter.getPosition(bind.atvRepetition.text.toString())]
                        val event = Event(
                            uid = "",
                            title = bind.etTitle.text.toString(),
                            startTime = bind.etStartTime.text.toString(),
                            endTime = bind.etEndTime.text.toString(),
                            location = bind.etLocation.text.toString(),
                            description = bind.etDescription.text.toString(),
                            participants = processChipGroup(bind.cgParticipants),
                            category = processChipGroup(bind.cgCategory),
                            repetition = repetition,
                            colorTag = color,
                            type = ActivityType.EVENT,
                            notificationId = Preferences.getNextNotificationId()
                        )
                        if (obj == null) {
                            FirestoreManagerFacade.saveInSchedule(event) { added ->
                                callback(added as Event)
                                added.createNotification(context)
                            }
                        } else {
                            FirestoreManagerFacade.updateInSchedule(event.copy(uid = obj.uid)) {
                                callback(it as Event)
                            }
                        }
                        dismiss()
                    }
                }
            }

        }
    }

    override fun fillData(context: Context, obj: Event) {
        bind.etTitle.setText(obj.title)
        bind.etStartTime.setText(obj.startTime)
        bind.etStartTime.isEnabled = false
        bind.etEndTime.setText(obj.endTime)
        bind.etLocation.setText(obj.location)

        // Set Repetition dropdown selection
        bind.atvRepetition.apply {
            setText(
                repetitionArr[obj.repetition.ordinal],
                false
            ) // Directly use the enum index
            setAdapter(arrAdapter)
        }
        bind.etDescription.setText(obj.description)
        bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(obj.colorTag)))

        // Category Chips creation
        for (cat in obj.category) {
            val chip = makeChip(context, cat)
            bind.cgCategory.addView(chip)
        }

        // Participants Chips creation
        for (part in obj.participants) {
            val chip = makeChip(context, part)
            bind.cgParticipants.addView(chip)
        }
    }
}