package com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault

import com.miguelrodriguez19.mindmaster.model.structures.enums.AccountType
import java.io.Serializable

data class Account(
    val uid: String,
    val name: String,
    val username: String?,
    val email: String?,
    val password: String?,
    val description: String?,
    val type: AccountType
) : Serializable {

    constructor() : this("", "", "", "", "", "", AccountType.OTHER)

    constructor(
        name: String, username: String?, email: String?,
        password: String?, description: String?, type: AccountType
    ) : this("", name, username, email, password, description, type)

    constructor(codAccount: String, account: Account) : this(
        codAccount, account.name, account.username, account.email,
        account.password, account.description, account.type
    )

    /**
     * Compares this Account with another Account for deep equality.
     *
     * Deep equality is achieved when all corresponding fields of both accounts
     * are equal. This includes uid, name, username, email, password, description,
     * and type.
     *
     * @param other The Account to be compared with this instance.
     * @return true if all fields are equal, false otherwise.
     */
    fun areDeepEquals(other: Account): Boolean {
        return this.uid == other.uid && this.name == other.name
                && this.username == other.username && this.email == other.email
                && this.password == other.password && this.description == other.description
                && this.type == other.type
    }


}