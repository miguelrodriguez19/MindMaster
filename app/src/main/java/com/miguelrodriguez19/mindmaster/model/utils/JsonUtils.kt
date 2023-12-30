package com.miguelrodriguez19.mindmaster.model.utils

import com.google.gson.Gson
import com.miguelrodriguez19.mindmaster.model.structures.dto.UserResponse
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Event
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Reminder
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Task

object jsonUtils {
    fun String.toUserResponse(): UserResponse = Gson().fromJson(this, UserResponse::class.java)

    fun UserResponse.toJson(): String = Gson().toJson(this)
}