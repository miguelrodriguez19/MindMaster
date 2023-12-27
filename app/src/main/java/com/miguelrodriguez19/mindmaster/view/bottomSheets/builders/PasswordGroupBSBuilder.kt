package com.miguelrodriguez19.mindmaster.view.bottomSheets.builders

import com.miguelrodriguez19.mindmaster.model.structures.dto.PasswordGroupResponse
import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet
import com.miguelrodriguez19.mindmaster.view.bottomSheets.PasswordGroupBS

class PasswordGroupBSBuilder : CustomBottomSheetBuilder<PasswordGroupResponse> {
    override fun build(): CustomBottomSheet<PasswordGroupResponse> = PasswordGroupBS()
}
