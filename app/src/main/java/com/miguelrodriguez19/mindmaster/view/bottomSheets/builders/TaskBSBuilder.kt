package com.miguelrodriguez19.mindmaster.view.bottomSheets.builders

import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Task
import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet
import com.miguelrodriguez19.mindmaster.view.bottomSheets.TaskBS

class TaskBSBuilder : CustomBottomSheetBuilder<Task> {
    override fun build(): CustomBottomSheet<Task> = TaskBS()
}
