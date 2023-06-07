package com.miguelrodriguez19.mindmaster.models.structures

import java.io.Serializable

data class GroupPasswordsResponse(
    val uid: String,
    val name: String,
    val accountsList: List<Account>,
) : Serializable {

    constructor() : this("", "", emptyList())

    constructor(codGroup: String, group: GroupPasswordsResponse) : this(
        codGroup,
        group.name,
        group.accountsList
    )

    constructor(name: String, accountsList: List<Account>) : this("", name, accountsList)

    data class Account(
        val uid: String,
        val name: String,
        val username: String?,
        val email: String?,
        val password: String?,
        val description: String?,
        val type: Type
    ) : Serializable {
        constructor() : this("", "", "", "", "", "", Type.OTHER)
        constructor(
            name: String, username: String?, email: String?,
            password: String?, description: String?, type: Type
        ) : this("", name, username, email, password, description, type)

        constructor(codAccount: String, account: Account) : this(
            codAccount, account.name, account.username, account.email,
            account.password, account.description, account.type
        )

    }

    enum class Type {
        EMAIL, GOOGLE, OTHER
    }
}