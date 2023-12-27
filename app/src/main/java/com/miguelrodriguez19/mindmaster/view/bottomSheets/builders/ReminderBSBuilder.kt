package com.miguelrodriguez19.mindmaster.view.bottomSheets.builders

import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Reminder
import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet
import com.miguelrodriguez19.mindmaster.view.bottomSheets.ReminderBS

class ReminderBSBuilder : CustomBottomSheetBuilder<Reminder> {
    override fun build(): CustomBottomSheet<Reminder> = ReminderBS()
}
