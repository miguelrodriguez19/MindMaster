package com.miguelrodriguez19.mindmaster.models

import com.google.gson.Gson

data class UserResponse(
    val uid:String,
    val firstName:String,
    val lastName:String?,
    val email:String,
    val birthdate: String?,
    val photoUrl:String
){
    constructor() : this("", "", null, "", null, "")

    constructor(user: UserResponse,firstName: String, lastName: String, birthdate: String) : this(user.uid, firstName, lastName, user.email, birthdate, user.photoUrl)
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["uid"] = uid
        map["firstName"] = firstName
        map["lastName"] = lastName ?: ""
        map["email"] = email
        map["birthdate"] = birthdate ?: ""
        map["photoUrl"] = photoUrl
        return map
    }
}