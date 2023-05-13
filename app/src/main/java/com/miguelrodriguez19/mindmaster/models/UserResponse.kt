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
}