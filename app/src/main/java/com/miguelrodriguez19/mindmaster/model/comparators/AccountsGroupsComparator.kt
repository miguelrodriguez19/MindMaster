package com.miguelrodriguez19.mindmaster.model.comparators

import com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault.PasswordGroupResponse

class AccountsGroupsComparator : Comparator<PasswordGroupResponse> {
    override fun compare(o1: PasswordGroupResponse?, o2: PasswordGroupResponse?): Int {
        return if (o1 == null) {
            -1
        } else if (o2 == null) {
            1
        } else {
            o1.name.compareTo(o2.name)
        }
    }
}