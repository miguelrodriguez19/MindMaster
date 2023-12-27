package com.miguelrodriguez19.mindmaster.model.comparators

import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.enums.Priority
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Task

class EventComparator : Comparator<AbstractActivity> {
    override fun compare(a: AbstractActivity, b: AbstractActivity): Int {
        // If both objects are tasks, they compare by priority
        if (a is Task && b is Task) {
            if (a.priority == Priority.URGENT && b.priority != Priority.URGENT) {
                return -1
            } else if (b.priority == Priority.URGENT && a.priority != Priority.URGENT) {
                return 1
            } else if ((a.priority == Priority.URGENT || a.priority == Priority.HIGH) && a.priority == b.priority) {
                // If both objects have the same priority, they compare by title
                return a.title.compareTo(b.title)
            }
        }
        // If only one object is a Task it goes first
        if (a is Task && (a.priority == Priority.URGENT || a.priority == Priority.HIGH)) {
            return -1
        }
        if (b is Task && (b.priority == Priority.URGENT || b.priority == Priority.HIGH)) {
            return 1
        }
        // If none of the objects are tasks, they are compared by title
        return a.title.compareTo(b.title)
    }
}

