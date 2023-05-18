package com.miguelrodriguez19.mindmaster.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.*
import com.miguelrodriguez19.mindmaster.models.*
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse.Movement
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse.Type
import com.miguelrodriguez19.mindmaster.passwords.FormAccountAdapter
import com.miguelrodriguez19.mindmaster.utils.AllDialogs.Companion.colorPickerDialog
import com.miguelrodriguez19.mindmaster.utils.AllDialogs.Companion.showConfirmationDialog
import com.miguelrodriguez19.mindmaster.utils.AllDialogs.Companion.showDatePicker
import com.miguelrodriguez19.mindmaster.utils.AllDialogs.Companion.showDateTimePicker
import com.miguelrodriguez19.mindmaster.utils.FirebaseManager.saveInSchedule
import com.miguelrodriguez19.mindmaster.utils.FirebaseManager.saveMovement
import com.miguelrodriguez19.mindmaster.utils.FirebaseManager.updateInSchedule
import com.miguelrodriguez19.mindmaster.utils.FirebaseManager.updateMovement
import com.miguelrodriguez19.mindmaster.utils.Toolkit.checkFields
import com.miguelrodriguez19.mindmaster.utils.Toolkit.compareDates
import com.miguelrodriguez19.mindmaster.utils.Toolkit.makeChip
import com.miguelrodriguez19.mindmaster.utils.Toolkit.processChipGroup

class AllBottomSheets {
    companion object {
        fun showEventsBS(context: Context, e: Event?, callback: (Event) -> Unit) {
            val botSheet = BottomSheetDialog(context)
            val bottomSheetView =
                LayoutInflater.from(context).inflate(R.layout.bottom_sheet_events, null)
            val bind = BottomSheetEventsBinding.bind(bottomSheetView)
            var color = e?.color_tag ?: String.format("#%06X", 0xFFFFFF and ContextCompat.getColor(context, R.color.primaryColor))

            if (e != null) {
                bind.etTitle.setText(e.title)
                bind.etStartTime.setText(e.start_time)
                bind.etEndTime.setText(e.end_time)
                bind.etLocation.setText(e.location)
                // bind.tilRepetition
                bind.etDescription.setText(e.description)
                bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(e.color_tag)))
                for (cat in e.category) {
                    val chip = makeChip(context, cat)
                    bind.cgCategory.addView(chip)
                }
                for (part in e.participants) {
                    val chip = makeChip(context, part)
                    bind.cgParticipants.addView(chip)
                }
            }

            bind.cgParticipants.setOnCheckedChangeListener { group, checkedId ->
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
                        if (compareDates(datetime, bind.etStartTime.text.toString()) < 0) {
                            bind.etEndTime.text = null
                            bind.tilEndTime.error =
                                context.getString(R.string.err_end_date_incorrect)
                        }
                    } else {
                        bind.etStartTime.setText(datetime)
                    }
                }
            }
            bind.cgCategory.setOnCheckedChangeListener { group, checkedId ->
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
                        bind.tilCategory.error = context.getString(R.string.reached_max_categories)
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
            bind.ibtnClose.setOnClickListener {
                showConfirmationDialog(
                    context,
                    context.getString(R.string.changes_will_lost),
                    context.getString(R.string.confirm_lost_changes_message)
                ) {
                    if (it) {
                        botSheet.dismiss()
                    }
                }
            }
            bind.efabSave.setOnClickListener {
                checkFields(
                    context, arrayOf(bind.tilTitle, bind.tilStartTime, bind.tilEndTime)
                ) { ok ->
                    if (ok) {
                        val event = Event(
                            bind.etTitle.text.toString(),
                            bind.etStartTime.text.toString(),
                            bind.etEndTime.text.toString(),
                            bind.etLocation.text.toString(),
                            bind.etDescription.text.toString(),
                            processChipGroup(bind.cgParticipants),
                            processChipGroup(bind.cgCategory),
                            Repetition.NONE,
                            color,
                            EventType.EVENT
                        )
                        if (e == null) {
                            saveInSchedule(context, event) { added ->
                                callback(added as Event)
                            }
                        } else {
                            updateInSchedule(context, Event(e.uid, event)){
                                callback(it as Event)
                            }
                        }
                        botSheet.dismiss()
                    }
                }
            }

            botSheet.setContentView(bottomSheetView)
            botSheet.show()
        }

        fun showRemindersBS(context: Context, r: Reminder?, callback: (Reminder) -> Unit) {
            val botSheet = BottomSheetDialog(context)
            val bottomSheetView =
                LayoutInflater.from(context).inflate(R.layout.bottom_sheet_reminder, null)
            val bind = BottomSheetReminderBinding.bind(bottomSheetView)
            var color = r?.color_tag ?: String.format("#%06X", 0xFFFFFF and ContextCompat.getColor(context, R.color.primaryColor))

            if (r != null) {
                bind.etTitle.setText(r.title)
                bind.etDate.setText(r.date_time)
                // bind.tilRepetition
                bind.etDescription.setText(r.description)
                bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(r.color_tag)))
                for (cat in r.category ?: ArrayList()) {
                    val chip = makeChip(context, cat)
                    bind.cgCategory.addView(chip)
                }
            }

            bind.etDate.setOnClickListener {
                showDatePicker(context) { date ->
                    bind.etDate.setText(date)
                }
            }
            bind.cgCategory.setOnCheckedChangeListener { group, checkedId ->
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
                        bind.tilCategory.error = context.getString(R.string.reached_max_categories)
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
            bind.ibtnClose.setOnClickListener {
                showConfirmationDialog(
                    context,
                    context.getString(R.string.changes_will_lost),
                    context.getString(R.string.confirm_lost_changes_message)
                ) {
                    if (it) {
                        botSheet.dismiss()
                    }
                }
            }
            bind.efabSave.setOnClickListener {
                checkFields(context, arrayOf(bind.tilTitle, bind.tilDate)) { ok ->
                    if (ok) {
                        val reminder = Reminder(
                            bind.etTitle.text.toString(),
                            bind.etDate.text.toString(),
                            bind.etDescription.text.toString(),
                            processChipGroup(bind.cgCategory),
                            color,
                            EventType.REMINDER
                        )
                        if (r == null) {
                            saveInSchedule(context, reminder) { added ->
                                callback(added as Reminder)
                            }
                        } else {
                            updateInSchedule(context, Reminder(r.uid, reminder)){
                                callback(it as Reminder)
                            }
                        }
                        botSheet.dismiss()
                    }
                }
            }
            botSheet.setContentView(bottomSheetView)
            botSheet.show()
        }

        fun showTasksBS(context: Context, t: Task?, callback: (Task) -> Unit) {
            val botSheet = BottomSheetDialog(context)
            val bottomSheetView =
                LayoutInflater.from(context).inflate(R.layout.bottom_sheet_tasks, null)
            val bind = BottomSheetTasksBinding.bind(bottomSheetView)
            var color = t?.color_tag ?: String.format("#%06X", 0xFFFFFF and ContextCompat.getColor(context, R.color.primaryColor))

            if (t != null) {
                bind.etTitle.setText(t.title)
                bind.etDueDate.setText(t.due_date)
                // bind.tilStatus
                // bind.priority
                bind.etDescription.setText(t.description)
                bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(t.color_tag)))
                for (cat in t.category ?: ArrayList()) {
                    val chip = makeChip(context, cat)
                    bind.cgCategory.addView(chip)
                }
            }

            bind.etDueDate.setOnClickListener {
                showDatePicker(context) { date ->
                    bind.etDueDate.setText(date)
                }
            }
            bind.cgCategory.setOnCheckedChangeListener { group, checkedId ->
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
                        bind.tilCategory.error = context.getString(R.string.reached_max_categories)
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
            bind.ibtnClose.setOnClickListener {
                showConfirmationDialog(
                    context,
                    context.getString(R.string.changes_will_lost),
                    context.getString(R.string.confirm_lost_changes_message)
                ) {
                    if (it) {
                        botSheet.dismiss()
                    }
                }
            }
            bind.efabSave.setOnClickListener {
                checkFields(context, arrayOf(bind.tilTitle, bind.tilDueDate)) { ok ->
                    if (ok) {
                        val task = Task(
                            bind.etTitle.text.toString(),
                            bind.etDueDate.text.toString(),
                            bind.etDescription.text.toString(),
                            Priority.URGENT, Status.PENDING,
                            processChipGroup(bind.cgCategory),
                            color,
                            EventType.TASK
                        )
                        if (t == null) {
                            saveInSchedule(context, task) { added ->
                                callback(added as Task)
                            }
                        } else {
                            updateInSchedule(context, Task(t.uid, task)){
                                callback(it as Task)
                            }
                        }
                        botSheet.dismiss()
                    }
                }
            }

            botSheet.setContentView(bottomSheetView)
            botSheet.show()
        }

        fun showMovementBS(
            context: Context, m: Movement?, typeMov: Type?, callback: (Movement) -> Unit
        ) {
            val botSheet = BottomSheetDialog(context)
            val bottomSheetView =
                LayoutInflater.from(context).inflate(R.layout.bottom_sheet_movements, null)
            val bind = BottomSheetMovementsBinding.bind(bottomSheetView)
            var selectedType = Type.INCOME

            if (m != null) {
                selectedType = when (m.type) {
                    Type.INCOME -> {
                        bind.toggleTypeMovement.check(R.id.btn_typeIncome)
                        Type.INCOME
                    }
                    Type.EXPENSE -> {
                        bind.toggleTypeMovement.check(R.id.btn_typeExpense)
                        Type.EXPENSE
                    }
                }
                bind.etConcept.setText(m.concept)
                bind.etDate.setText(m.date)
                bind.etAmount.setText(m.amount.toString())
                bind.etDescription.setText(m.description)
            }

            selectedType = when (typeMov) {
                Type.INCOME, null -> {
                    bind.toggleTypeMovement.check(R.id.btn_typeIncome)
                    Type.INCOME
                }
                Type.EXPENSE -> {
                    bind.toggleTypeMovement.check(R.id.btn_typeExpense)
                    Type.EXPENSE
                }
            }
            bind.toggleTypeMovement.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when (checkedId) {
                    R.id.btn_typeIncome -> selectedType = Type.INCOME
                    R.id.btn_typeExpense -> selectedType = Type.EXPENSE
                }
            }
            bind.etDate.setOnClickListener {
                showDatePicker(context) { date ->
                    bind.etDate.setText(date)
                }
            }
            bind.ibtnClose.setOnClickListener {
                showConfirmationDialog(
                    context,
                    context.getString(R.string.changes_will_lost),
                    context.getString(R.string.confirm_lost_changes_message)
                ) {
                    if (it) {
                        botSheet.dismiss()
                    }
                }
            }
            bind.efabSave.setOnClickListener {
                checkFields(context, arrayOf(bind.tilConcept, bind.tilDate, bind.tilAmount)) { ok ->
                    if (ok) {
                        val move = Movement(
                            bind.etDate.text.toString(),
                            bind.etConcept.text.toString(),
                            bind.etAmount.text.toString().toFloat(),
                            bind.etDescription.text.toString(),
                            selectedType
                        )
                        if(m == null){
                            saveMovement(context, move) { added ->
                                callback(added)
                            }
                        }else{
                            updateMovement(context, Movement(m.uid, move)){ updated ->
                                callback(updated!!)
                            }
                        }
                        botSheet.dismiss()
                    }
                }
            }

            botSheet.setContentView(bottomSheetView)
            botSheet.show()
        }

        fun showPasswordsBS(
            context: Context, group: GroupPasswordsResponse?
        ) {
            val botSheet = BottomSheetDialog(context)
            val bottomSheetView =
                LayoutInflater.from(context).inflate(R.layout.bottom_sheet_password, null)
            val bind = BottomSheetPasswordBinding.bind(bottomSheetView)

            bind.etTitleGroup.setText(group?.name)
            val adapter = FormAccountAdapter(
                context,
                group?.accountsList?.let { ArrayList(it) } ?: arrayListOf(null))
            bind.rvAccountsBS.adapter = adapter
            bind.rvAccountsBS.layoutManager = StaggeredGridLayoutManager(1, 1)

            bind.btnAddNewAccForm.setOnClickListener {
                adapter.addNewForm()
            }

            bind.ibtnClose.setOnClickListener {
                showConfirmationDialog(
                    context,
                    context.getString(R.string.changes_will_lost),
                    context.getString(R.string.confirm_lost_changes_message)
                ) {
                    if (it) {
                        botSheet.dismiss()
                    }
                }
            }

            bind.efabSave.setOnClickListener {
                checkPwdBSFields(context, bind.rvAccountsBS) { ok ->
                    if (ok) {
                        botSheet.dismiss()
                    }
                }
            }

            botSheet.setContentView(bottomSheetView)
            botSheet.show()
        }

        private fun checkPwdBSFields(
            context: Context, rv: RecyclerView, callback: (Boolean) -> Unit
        ) {
            val adapter = rv.adapter
            if (adapter != null) {
                var flag = true
                for (i in 0 until adapter.itemCount) {
                    val view = rv.getChildAt(i)
                    val bind = CellFormAccountBinding.bind(view)
                    val childTil = arrayOf(bind.tilEmail, bind.tilUsername, bind.tilPassword)
                    if (bind.toggleTypeSignIn.checkedButtonId != R.id.btn_typeOther) {
                        for (til in childTil) {
                            if (til.visibility == View.VISIBLE) {
                                if (til.editText?.text.isNullOrEmpty()) {
                                    flag = false
                                    til.error = context.getString(R.string.field_obligatory)
                                } else {
                                    til.error = null
                                }
                            }
                        }
                    }
                }
                callback(flag)
            }
            callback(false)
        }

    }
}