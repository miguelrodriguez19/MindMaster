package com.miguelrodriguez19.mindmaster.models

import java.io.Serializable

data class GroupPasswordsResponse(
    val codGroup: Int,
    val name: String,
    val accountsList: List<Account>,
) : Serializable {
    data class Account(
        val codAccount: Int,
        val username: String?,
        val email: String?,
        val password: String?,
        val note: String?,
        val type: String
    ) : Serializable
}