package com.miguelrodriguez19.mindmaster.view.bottomSheets.builders

import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Event
import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet
import com.miguelrodriguez19.mindmaster.view.bottomSheets.EventBS

class EventBSBuilder : CustomBottomSheetBuilder<Event> {
    override fun build(): CustomBottomSheet<Event> = EventBS()
}
