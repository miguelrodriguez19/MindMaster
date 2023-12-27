package com.miguelrodriguez19.mindmaster.view.bottomSheets.builders

import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet

interface CustomBottomSheetBuilder<T> {
    fun build(): CustomBottomSheet<T>
}
