package com.miguelrodriguez19.mindmaster.models.comparators

import com.miguelrodriguez19.mindmaster.models.AbstractEvent
import com.miguelrodriguez19.mindmaster.models.Priority
import com.miguelrodriguez19.mindmaster.models.Task

class EventComparator : Comparator<AbstractEvent> {
    override fun compare(a: AbstractEvent, b: AbstractEvent): Int {
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

