package com.miguelrodriguez19.mindmaster.view.bottomSheets

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.view.bottomSheets.builders.CustomBottomSheetBuilder
import com.miguelrodriguez19.mindmaster.view.bottomSheets.builders.EventBSBuilder
import com.miguelrodriguez19.mindmaster.view.bottomSheets.builders.MovementBSBuilder
import com.miguelrodriguez19.mindmaster.view.bottomSheets.builders.PasswordGroupBSBuilder
import com.miguelrodriguez19.mindmaster.view.bottomSheets.builders.ReminderBSBuilder
import com.miguelrodriguez19.mindmaster.view.bottomSheets.builders.TaskBSBuilder
import java.time.LocalDate

abstract class CustomBottomSheet<T> {

    abstract fun showViewDetailBS(
        activity: Activity, obj: T?, date: LocalDate = LocalDate.now(), callback: (T) -> Unit
    )

    abstract fun fillData(context: Context, obj: T)

    fun checkObligatoryFields(obligatoryFields: List<TextInputLayout>, context: Context) : Boolean {
        var flag = true
        for (til in obligatoryFields) {
                if (til.editText?.text.isNullOrEmpty()) {
                    flag = false
                    til.error = context.getString(R.string.field_obligatory)
                } else {
                    til.error = null
                }
        }
        return flag
    }

    companion object {
        private val bsFactory: Map<String, CustomBottomSheetBuilder<*>> = hashMapOf(
            EventBS::class.java.name to EventBSBuilder(),
            ReminderBS::class.java.name to ReminderBSBuilder(),
            TaskBS::class.java.name to TaskBSBuilder(),
            MovementBS::class.java.name to MovementBSBuilder(),
            PasswordGroupBS::class.java.name to PasswordGroupBSBuilder()
        )

        fun <T> get(type: String?): CustomBottomSheet<T>? {
            Log.i("CUSTOM_BOTTOM_SHEET", "get: $type")
            @Suppress("UNCHECKED_CAST")
            return bsFactory[type]?.build() as? CustomBottomSheet<T>
        }
    }

}
