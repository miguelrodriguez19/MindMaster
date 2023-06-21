package com.miguelrodriguez19.mindmaster.models.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.setPeekHeight
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.chip.Chip
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.*
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager.saveGroup
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager.saveInSchedule
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager.saveMovement
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager.updateGroup
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager.updateInSchedule
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager.updateMovement
import com.miguelrodriguez19.mindmaster.models.structures.*
import com.miguelrodriguez19.mindmaster.models.structures.GroupPasswordsResponse.Account
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse.Movement
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse.Type
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs.Companion.colorPickerDialog
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs.Companion.showConfirmationDialog
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs.Companion.showDatePicker
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs.Companion.showDateTimePicker
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.checkFields
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.compareDates
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.getPeekHeight
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.makeChip
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.processChipGroup
import com.miguelrodriguez19.mindmaster.views.passwords.adapters.FormAccountAdapter

class AllBottomSheets {

    companion object {

        fun showEventsBS(context: Context, e: Event?, callback: (Event) -> Unit) {
            MaterialDialog(context, BottomSheet(LayoutMode.MATCH_PARENT)).show {
                customView(
                    R.layout.bottom_sheet_events,
                    scrollable = true,
                    horizontalPadding = true
                )
                cornerRadius(res = R.dimen.corner_radius_bottom_sheets)
                setPeekHeight(getPeekHeight(context))

                val bind = BottomSheetEventsBinding.bind(getCustomView())
                var color = e?.color_tag ?: String.format(
                    "#%06X", 0xFFFFFF and ContextCompat.getColor(context, R.color.primaryColor)
                )
                val repetitionArr = context.resources.getStringArray(R.array.repetition_enum)
                val arrAdapter = ArrayAdapter(context, R.layout.dropdown_item, repetitionArr)
                bind.atvRepetition.setAdapter(arrAdapter)

                if (e != null) {
                    bind.etTitle.setText(e.title)
                    bind.etStartTime.setText(e.start_time)
                    bind.etStartTime.isEnabled = false
                    bind.etEndTime.setText(e.end_time)
                    bind.etLocation.setText(e.location)
                    bind.atvRepetition.apply {
                        setText(
                            repetitionArr[e.repetition.ordinal],
                            false
                        ) // Directly use the enum index
                        setAdapter(arrAdapter)
                    }
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
                                Repetition.values()[arrAdapter.getPosition(bind.atvRepetition.text.toString())],
                                color,
                                EventType.EVENT
                            )
                            if (e == null) {
                                saveInSchedule(event) { added ->
                                    callback(added as Event)
                                }
                            } else {
                                updateInSchedule(Event(e.uid, event)) {
                                    callback(it as Event)
                                }
                            }
                            dismiss()
                        }
                    }
                }

            }
        }

        fun showRemindersBS(context: Context, r: Reminder?, callback: (Reminder) -> Unit) {
            MaterialDialog(context, BottomSheet(LayoutMode.MATCH_PARENT)).show {
                customView(
                    R.layout.bottom_sheet_reminder, scrollable = true, horizontalPadding = true
                )
                cornerRadius(res = R.dimen.corner_radius_bottom_sheets)
                setPeekHeight(getPeekHeight(context))

                val bind = BottomSheetReminderBinding.bind(getCustomView())
                var color = r?.color_tag ?: String.format(
                    "#%06X", 0xFFFFFF and ContextCompat.getColor(context, R.color.primaryColor)
                )
                val repetitionArr = context.resources.getStringArray(R.array.repetition_enum)
                val arrAdapter = ArrayAdapter(context, R.layout.dropdown_item, repetitionArr)
                bind.atvRepetition.setAdapter(arrAdapter)

                if (r != null) {
                    bind.etTitle.setText(r.title)
                    bind.etDate.setText(r.date_time)
                    bind.etDate.isEnabled = false
                    bind.atvRepetition.apply {
                        setText(
                            repetitionArr[r.repetition.ordinal],
                            false
                        ) // Directly use the enum index
                        setAdapter(arrAdapter)
                    }
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
                    checkFields(context, arrayOf(bind.tilTitle, bind.tilDate)) { ok ->
                        if (ok) {
                            val reminder = Reminder(
                                bind.etTitle.text.toString(),
                                bind.etDate.text.toString(),
                                bind.etDescription.text.toString(),
                                processChipGroup(bind.cgCategory),
                                color,
                                Repetition.values()[arrAdapter.getPosition(bind.atvRepetition.text.toString())],
                                EventType.REMINDER
                            )
                            if (r == null) {
                                saveInSchedule(reminder) { added ->
                                    callback(added as Reminder)
                                }
                            } else {
                                updateInSchedule(Reminder(r.uid, reminder)) {
                                    callback(it as Reminder)
                                }
                            }
                            dismiss()
                        }
                    }
                }
            }
        }

        fun showTasksBS(context: Context, t: Task?, callback: (Task) -> Unit) {
            MaterialDialog(context, BottomSheet(LayoutMode.MATCH_PARENT)).show {
                customView(R.layout.bottom_sheet_tasks, scrollable = true, horizontalPadding = true)
                cornerRadius(res = R.dimen.corner_radius_bottom_sheets)
                setPeekHeight(getPeekHeight(context))

                val bind = BottomSheetTasksBinding.bind(getCustomView())
                var color = t?.color_tag ?: String.format(
                    "#%06X", 0xFFFFFF and ContextCompat.getColor(context, R.color.primaryColor)
                )
                val statusArr = context.resources.getStringArray(R.array.status_enum)
                val statusAdapter = ArrayAdapter(context, R.layout.dropdown_item, statusArr)
                bind.atvStatus.setAdapter(statusAdapter)
                val priorityArr = context.resources.getStringArray(R.array.priority_enum)
                val priorityAdapter = ArrayAdapter(context, R.layout.dropdown_item, priorityArr)
                bind.atvPriority.setAdapter(priorityAdapter)

                if (t != null) {
                    bind.etTitle.setText(t.title)
                    bind.etDueDate.setText(t.due_date)
                    bind.etDueDate.isEnabled = false
                    bind.atvStatus.apply {
                        setText(
                            statusArr[t.status.ordinal],
                            false
                        ) // Directly use the enum index
                        setAdapter(statusAdapter)
                    }
                    bind.atvPriority.apply {
                        setText(
                            priorityArr[t.priority.ordinal],
                            false
                        ) // Directly use the enum index
                        setAdapter(priorityAdapter)
                    }
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
                    checkFields(context, arrayOf(bind.tilTitle, bind.tilDueDate)) { ok ->
                        if (ok) {
                            val task = Task(
                                bind.etTitle.text.toString(),
                                bind.etDueDate.text.toString(),
                                bind.etDescription.text.toString(),
                                Priority.values()[priorityAdapter.getPosition(bind.atvPriority.text.toString())],
                                Status.values()[statusAdapter.getPosition(bind.atvStatus.text.toString())],
                                processChipGroup(bind.cgCategory),
                                color,
                                EventType.TASK
                            )
                            if (t == null) {
                                saveInSchedule(task) { added ->
                                    callback(added as Task)
                                }
                            } else {
                                updateInSchedule(Task(t.uid, task)) {
                                    callback(it as Task)
                                }
                            }
                            dismiss()
                        }
                    }
                }
            }
        }

        fun showMovementBS(
            context: Context, m: Movement?, typeMov: Type?, onSuccess: (Movement) -> Unit
        ) {
            MaterialDialog(context, BottomSheet(LayoutMode.MATCH_PARENT)).show {
                customView(
                    R.layout.bottom_sheet_movements, scrollable = true, horizontalPadding = true
                )
                cornerRadius(res = R.dimen.corner_radius_bottom_sheets)

                val bind = BottomSheetMovementsBinding.bind(getCustomView())

                var selectedType: Type

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
                    bind.etDate.isEnabled = false
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
                bind.toggleTypeMovement.addOnButtonCheckedListener { _, checkedId, _ ->
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
                bind.btnClose.setOnClickListener {
                    showConfirmationDialog(
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
                    checkFields(
                        context, arrayOf(bind.tilConcept, bind.tilDate, bind.tilAmount)
                    ) { ok ->
                        if (ok) {
                            val move = Movement(
                                bind.etDate.text.toString(),
                                bind.etConcept.text.toString(),
                                bind.etAmount.text.toString().toFloat(),
                                bind.etDescription.text.toString(),
                                selectedType
                            )
                            if (m == null) {
                                saveMovement(move) { added ->
                                    onSuccess(added)
                                }
                            } else {
                                updateMovement(Movement(m.uid, move)) { updated ->
                                    onSuccess(updated!!)
                                }
                            }
                            dismiss()
                        }
                    }
                }
            }
        }

        fun showPasswordsBS(
            context: Context, group: GroupPasswordsResponse?,
            onSuccess: (GroupPasswordsResponse) -> Unit
        ) {
            MaterialDialog(context, BottomSheet(LayoutMode.MATCH_PARENT)).show {
                customView(
                    R.layout.bottom_sheet_password, scrollable = true, horizontalPadding = true
                )
                cornerRadius(res = R.dimen.corner_radius_bottom_sheets)
                setPeekHeight(getPeekHeight(context, 0.95f))

                val bind = BottomSheetPasswordBinding.bind(getCustomView())

                bind.etTitleGroup.setText(group?.name)
                val adapter = FormAccountAdapter(context,
                    group?.accountsList?.let { ArrayList(it) } ?: arrayListOf(null))
                bind.rvAccountsBS.adapter = adapter
                bind.rvAccountsBS.layoutManager = StaggeredGridLayoutManager(1, 1)

                bind.btnAddNewAccForm.setOnClickListener {
                    adapter.addNewForm()
                }

                bind.btnClose.setOnClickListener {
                    showConfirmationDialog(
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
                    checkPwdBSFields(context, bind.rvAccountsBS) { ok ->
                        if (ok) {
                            if (group == null) {
                                saveGroup(
                                    GroupPasswordsResponse(
                                        bind.etTitleGroup.text.toString(),
                                        getAccounts(bind.rvAccountsBS)
                                    )
                                ) {
                                    onSuccess(it)
                                }
                            } else {
                                updateGroup(
                                    group.copy(
                                        name = bind.etTitleGroup.text.toString(),
                                        accountsList = getAccounts(bind.rvAccountsBS)
                                    )
                                ) {
                                    onSuccess(it)
                                }
                            }
                            dismiss()
                        }
                    }
                }
            }
        }

        private fun getAccounts(rv: RecyclerView): List<Account> {
            val adapter = rv.adapter
            val accountsList = ArrayList<Account>()
            if (adapter != null) {
                for (i in 0 until adapter.itemCount) {
                    val view = rv.getChildAt(i)
                    val bind = CellFormAccountBinding.bind(view)
                    val title = bind.etTitleAccount.text.toString()
                    val username = bind.etUsername.text.toString()
                    val email = bind.etEmail.text.toString()
                    val password = bind.etPassword.text.toString()
                    val description = bind.etDescription.text.toString()
                    val type = when (bind.toggleTypeSignIn.checkedButtonId) {
                        R.id.btn_typeEmail -> GroupPasswordsResponse.Type.EMAIL
                        R.id.btn_typeGoogle -> GroupPasswordsResponse.Type.GOOGLE
                        R.id.btn_typeOther -> GroupPasswordsResponse.Type.OTHER
                        else -> GroupPasswordsResponse.Type.OTHER
                    }
                    accountsList.add(Account(title, username, email, password, description, type))
                }
            }
            return accountsList
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