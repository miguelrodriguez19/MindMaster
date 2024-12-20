package com.miguelrodriguez19.mindmaster.view.bottomSheets

import android.app.Activity
import android.content.Context
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.BottomSheetMovementsBinding
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.structures.dto.expenses.Movement
import com.miguelrodriguez19.mindmaster.model.structures.enums.MovementType
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.DEFAULT_DATE_FORMAT
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MovementBS : CustomBottomSheet<Movement>() {
    private lateinit var bind: BottomSheetMovementsBinding
    private lateinit var selectedType: MovementType

    override fun showViewDetailBS(
        activity: Activity,
        obj: Movement?,
        date: LocalDate,
        callback: (Movement) -> Unit
    ) {
        MaterialDialog(activity, BottomSheet(LayoutMode.MATCH_PARENT)).show {
            customView(
                R.layout.bottom_sheet_movements, scrollable = true, horizontalPadding = true
            )
            bind = BottomSheetMovementsBinding.bind(getCustomView())
            cornerRadius(res = R.dimen.corner_radius_bottom_sheets)

            if (obj != null && obj.amIEmpty()) {
                fillData(activity, obj)
            }

            selectedType = when (obj?.type) {
                MovementType.EXPENSE -> {
                    bind.toggleTypeMovement.check(R.id.btn_typeExpense)
                    MovementType.EXPENSE
                }

                else -> { // Type.Income or null
                    bind.toggleTypeMovement.check(R.id.btn_typeIncome)
                    MovementType.INCOME // By default it is of type income
                }
            }

            bind.toggleTypeMovement.addOnButtonCheckedListener { _, checkedId, _ ->
                when (checkedId) {
                    R.id.btn_typeIncome -> selectedType = MovementType.INCOME
                    R.id.btn_typeExpense -> selectedType = MovementType.EXPENSE
                }
            }

            bind.etDate.let {
                if (obj == null) it.setText(date.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                it.setOnClickListener {
                    AllDialogs.showDatePicker(activity) { date ->
                        bind.etDate.setText(date)
                    }
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
                Toolkit.checkFields(
                    activity, arrayOf(bind.tilConcept, bind.tilDate, bind.tilAmount)
                ) { ok ->
                    if (ok) {
                        val move = Movement(
                            bind.etDate.text.toString(),
                            bind.etConcept.text.toString(),
                            bind.etAmount.text.toString().toFloat(),
                            bind.etDescription.text.toString(),
                            selectedType
                        )
                        if (obj == null) {
                            FirestoreManagerFacade.saveMovement(move) { added ->
                                callback(added)
                            }
                        } else {
                            FirestoreManagerFacade.updateMovement(
                                Movement(
                                    obj.uid,
                                    move
                                )
                            ) { updated ->
                                callback(updated!!)
                            }
                        }
                        dismiss()
                    }
                }
            }
        }
    }

    override fun fillData(context: Context, obj: Movement) {
        bind.etConcept.setText(obj.concept)
        bind.etDate.setText(obj.date)
        bind.etDate.isEnabled = false
        bind.etAmount.setText(obj.amount.toString())
        bind.etDescription.setText(obj.description)
    }
}