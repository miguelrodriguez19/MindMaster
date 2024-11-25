package com.miguelrodriguez19.mindmaster.model.structures.dto

data class UserResponse(
    val uid:String,
    val firstName:String,
    val lastName:String?,
    val email:String,
    val birthdate: String?,
    val photoUrl:String,
    val hasLoggedInBefore:Boolean
):java.io.Serializable{

    constructor() : this("", "", null, "", null, "", false)

    constructor(user: UserResponse, firstName: String, lastName: String, birthdate: String) : this(user.uid, firstName, lastName, user.email, birthdate, user.photoUrl, user.hasLoggedInBefore)

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["uid"] = uid
        map["firstName"] = firstName
        map["lastName"] = lastName ?: ""
        map["email"] = email
        map["birthdate"] = birthdate ?: ""
        map["photoUrl"] = photoUrl
        map["hasLoggedInBefore"] = hasLoggedInBefore
        return map
    }
}