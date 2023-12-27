package com.miguelrodriguez19.mindmaster.model.comparators

import com.miguelrodriguez19.mindmaster.model.structures.dto.GroupPasswordsResponse.Account

class AccountComparator : Comparator<Account> {
    override fun compare(o1: Account?, o2: Account?): Int {
        return if (o1 == null) {
            -1
        } else if (o2 == null) {
            1
        } else {
            o1.name.compareTo(o2.name)
        }
    }

}