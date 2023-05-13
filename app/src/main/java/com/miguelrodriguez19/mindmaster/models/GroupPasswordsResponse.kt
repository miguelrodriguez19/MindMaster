package com.miguelrodriguez19.mindmaster.models

import java.io.Serializable

data class GroupPasswordsResponse(
    val codGroup: String,
    val name: String,
    val accountsList: List<Account>,
) : Serializable {
    data class Account(
        val codAccount: String,
        val name:String,
        val username: String?,
        val email: String?,
        val password: String?,
        val description:String?,
        val note: String?,
        val type: Type
    ) : Serializable

    enum class Type {
        EMAIL, GOOGLE, OTHER
    }
}