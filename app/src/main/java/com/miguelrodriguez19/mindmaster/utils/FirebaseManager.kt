package com.miguelrodriguez19.mindmaster.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.models.GroupPasswordsResponse

object FirebaseManager {
    private val auth = FirebaseAuth.getInstance()

    fun createUserFirebase(
        context: Context,
        email: String,
        password: String,
        result: (Boolean) -> Unit
    ) {
        getAuth().createUserWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // add user to fireStore
                    // saveUser(context, email)
                    result(true)
                } else {
                    result(false)
                    val msg = if (it.exception is FirebaseAuthUserCollisionException) {
                        context.getString(R.string.collision_auth)
                    } else {
                        context.getString(R.string.something_went_wrong)
                    }
                    AllDialogs.showAlertDialog(context, context.getString(R.string.error), msg)
                }
            }

    }

    fun logInEmailPwd(email: String, password: String, callback: (Boolean, String) -> Unit) {
        getAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                it.result.user?.let { user -> callback(true, user.uid) }
            }
        }
    }

    fun getAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    fun deletePasswordsGroup(context: Context, item: GroupPasswordsResponse) {

    }
}