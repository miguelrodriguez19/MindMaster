package com.miguelrodriguez19.mindmaster.model.comparators

import com.miguelrodriguez19.mindmaster.model.structures.dto.GroupPasswordsResponse

class AccountsGroupsComparator : Comparator<GroupPasswordsResponse> {
    override fun compare(o1: GroupPasswordsResponse?, o2: GroupPasswordsResponse?): Int {
        return if (o1 == null) {
            -1
        } else if (o2 == null) {
            1
        } else {
            o1.name.compareTo(o2.name)
        }
    }
}