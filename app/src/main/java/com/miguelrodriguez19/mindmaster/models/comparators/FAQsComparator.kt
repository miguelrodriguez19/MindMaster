package com.miguelrodriguez19.mindmaster.models.comparators

import com.miguelrodriguez19.mindmaster.models.structures.FAQ
import com.miguelrodriguez19.mindmaster.models.structures.GroupPasswordsResponse

class FAQsComparator: Comparator<FAQ> {
    override fun compare(o1: FAQ?, o2: FAQ?): Int {
        return if (o1 == null) {
            -1
        } else if (o2 == null) {
            1
        } else {
            o1.question.compareTo(o2.question)
        }
    }

}