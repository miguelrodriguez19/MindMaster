package com.miguelrodriguez19.mindmaster.model.structures.dto

import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity

data class EventsResponse(
    val date: String, val allEventsList: List<AbstractActivity>
) : java.io.Serializable