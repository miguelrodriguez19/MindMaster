package com.miguelrodriguez19.mindmaster.view.bottomSheets.builders

import com.miguelrodriguez19.mindmaster.model.structures.dto.MonthMovementsResponse.Movement
import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet
import com.miguelrodriguez19.mindmaster.view.bottomSheets.MovementBS

class MovementBSBuilder : CustomBottomSheetBuilder<Movement> {
    override fun build(): CustomBottomSheet<Movement> = MovementBS()
}
