package com.miguelrodriguez19.mindmaster.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.*
import com.miguelrodriguez19.mindmaster.models.*
import com.miguelrodriguez19.mindmaster.passwords.FormAccountAdapter
import com.miguelrodriguez19.mindmaster.utils.AllDialogs.Companion.colorPickerDialog
import com.miguelrodriguez19.mindmaster.utils.AllDialogs.Companion.showConfirmationDialog
import com.miguelrodriguez19.mindmaster.utils.AllDialogs.Companion.showDatePicker
import com.miguelrodriguez19.mindmaster.utils.AllDialogs.Companion.showDateTimePicker
import com.miguelrodriguez19.mindmaster.utils.Toolkit.checkFields
import com.miguelrodriguez19.mindmaster.utils.Toolkit.compareDates
import com.miguelrodriguez19.mindmaster.utils.Toolkit.makeChip

class AllBottomSheets {
    companion object {
        fun showEventsBS(context: Context, e: Event?) {
            val botSheet = BottomSheetDialog(context)
            val bottomSheetView =
                LayoutInflater.from(context).inflate(R.layout.bottom_sheet_events, null)
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
                    val chip = makeChip(context, cat)
                    bind.cgCategory.addView(chip)
                }
                for (part in e.participants ?: ArrayList()) {
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
                colorPickerDialog(context) { color ->
                    bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(color))
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
                        botSheet.dismiss()
                    }
                }
            }
            botSheet.setContentView(bottomSheetView)
            botSheet.show()
        }

        fun showRemindersBS(context: Context, e: Reminder?) {
            val botSheet = BottomSheetDialog(context)
            val bottomSheetView =
                LayoutInflater.from(context).inflate(R.layout.bottom_sheet_reminder, null)
            val bind = BottomSheetReminderBinding.bind(bottomSheetView)
            if (e != null) {
                bind.etTitle.setText(e.title)
                bind.etDate.setText(e.date_time)
                // bind.tilRepetition
                bind.etDescription.setText(e.description)
                bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(e.color_tag)))
                for (cat in e.category ?: ArrayList()) {
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
                    bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(it))
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
                        botSheet.dismiss()
                    }
                }
            }
            botSheet.setContentView(bottomSheetView)
            botSheet.show()
        }

        fun showTasksBS(context: Context, e: Task?) {
            val botSheet = BottomSheetDialog(context)
            val bottomSheetView =
                LayoutInflater.from(context).inflate(R.layout.bottom_sheet_tasks, null)
            val bind = BottomSheetTasksBinding.bind(bottomSheetView)

            if (e != null) {
                bind.etTitle.setText(e.title)
                bind.etDueDate.setText(e.due_date)
                // bind.tilStatus
                // bind.priority
                bind.etDescription.setText(e.description)
                bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(e.color_tag)))
                for (cat in e.category ?: ArrayList()) {
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
                    bind.tilColorTag.setStartIconTintList(ColorStateList.valueOf(it))
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
                        botSheet.dismiss()
                    }
                }

            }

            botSheet.setContentView(bottomSheetView)
            botSheet.show()
        }

        fun showMovementBS(
            context: Context, m: MonthMovementsResponse.Movement?,
            typeMov: MonthMovementsResponse.Type?
        ) {
            val botSheet = BottomSheetDialog(context)
            val bottomSheetView =
                LayoutInflater.from(context).inflate(R.layout.bottom_sheet_movements, null)
            val bind = BottomSheetMovementsBinding.bind(bottomSheetView)

            if (m != null) {
                when (m.type) {
                    MonthMovementsResponse.Type.INCOME -> bind.toggleTypeMovement.check(R.id.btn_typeIncome)
                    MonthMovementsResponse.Type.EXPENSE -> bind.toggleTypeMovement.check(R.id.btn_typeExpense)
                }
                bind.etConcept.setText(m.concept)
                bind.etDate.setText(m.date)
                bind.etAmount.setText(m.amount.toString())
                bind.etDescription.setText(m.description)
            }

            when (typeMov) {
                MonthMovementsResponse.Type.INCOME, null -> bind.toggleTypeMovement.check(R.id.btn_typeIncome)
                MonthMovementsResponse.Type.EXPENSE -> bind.toggleTypeMovement.check(R.id.btn_typeExpense)
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
            val adapter = FormAccountAdapter(context, group?.accountsList?.let { ArrayList(it) } ?: arrayListOf(null))
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
            context: Context,
            rv: RecyclerView,
            callback: (Boolean) -> Unit
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