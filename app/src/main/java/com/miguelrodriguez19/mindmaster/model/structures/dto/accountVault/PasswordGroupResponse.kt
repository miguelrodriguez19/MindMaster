package com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault

import java.io.Serializable

data class PasswordGroupResponse(
    val uid: String,
    val name: String,
    val accountsList: List<Account>,
) : Serializable {

    constructor() : this("", "", emptyList())

    constructor(codGroup: String, group: PasswordGroupResponse) : this(
        codGroup,
        group.name,
        group.accountsList
    )

    constructor(name: String, accountsList: List<Account>) : this("", name, accountsList)

}