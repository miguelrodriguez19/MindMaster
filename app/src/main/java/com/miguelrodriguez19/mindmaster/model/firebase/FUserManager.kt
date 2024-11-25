package com.miguelrodriguez19.mindmaster.model.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.structures.dto.UserResponse
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs
import com.miguelrodriguez19.mindmaster.model.utils.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FUserManager {

    private const val SECURE = "secure"
    private const val USERS = "users"
    private const val TEMPORAL = "temporal"

    fun getUserByUID(uid: String, callback: (UserResponse?) -> Unit) {
        searchUserInUsers(uid) {
            if (it == null) {
                searchUserInTemporal(uid) { tempUser ->
                    callback(tempUser)
                }
            } else {
                callback(it)
            }
        }
    }

    private fun searchUserInUsers(uid: String, callback: (UserResponse?) -> Unit) {
        FirestoreManagerFacade.getDB()
            .collection(USERS).document(uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val userResponse = documentSnapshot.toObject(UserResponse::class.java)
                        callback(userResponse)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
    }

    private fun searchUserInTemporal(uid: String, callback: (UserResponse?) -> Unit) {
        FirestoreManagerFacade.getDB()
            .collection(TEMPORAL).document(uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val userResponse = documentSnapshot.toObject(UserResponse::class.java)
                        callback(userResponse)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
    }

    fun updateHasLoggedInBefore(user: UserResponse) {
        // Add user in Real Users collection
        saveUser(user)
        // Remove user from Temporal
        deleteTemporalUser(user.uid)
        // Update hasLoggedInBefore -> True
        val upUser = user.copy(hasLoggedInBefore = true)
        FirestoreManagerFacade.updateUser(upUser) {
            Preferences.setUser(upUser)
        }
    }

    fun getCurrentUser(): FirebaseUser? = runBlocking {
        FirestoreManagerFacade.getAuth().currentUser
    }

    // LOG_IN & SIGN_UP
    fun logInEmailPwd(
        email: String,
        password: String,
        callback: (Boolean, UserResponse?) -> Unit
    ) {
        FirestoreManagerFacade.getAuth().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = it.result.user
                    if (user != null && user.isEmailVerified) {
                        FirestoreManagerFacade.getUserByUID(user.uid) { u ->
                            if (u != null) {
                                val token = user.getIdToken(false).result.token
                                callback(true, u)
                            } else {
                                callback(false, null)
                            }
                        }
                    } else {
                        callback(false, null)
                    }
                } else {
                    callback(false, null)
                }
            }
    }

    fun createUser(
        context: Context, name: String, lastname: String?, birthdate: String,
        email: String, password: String, result: (Boolean) -> Unit
    ) {
        FirestoreManagerFacade.getAuth().createUserWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val theme = context.getString(R.string.photo_themes).split(",").shuffled()[0]
                    val url = context.getString(R.string.photo_url_request, theme)
                    FirestoreManagerFacade.saveImageInStorage(url) { photoUrl ->
                        task.result.user?.sendEmailVerification()
                        saveTemporalUser(
                            UserResponse(
                                task.result.user?.uid!!, name, lastname,
                                email, birthdate, photoUrl, false
                            )
                        )
                        result(true)
                    }
                } else {
                    result(false)
                    val msg = if (task.exception is FirebaseAuthUserCollisionException) {
                        context.getString(R.string.collision_auth)
                    } else {
                        context.getString(R.string.something_went_wrong)
                    }
                    AllDialogs.showAlertDialog(context, context.getString(R.string.error), msg)
                }
            }
    }

    private fun saveUser(user: UserResponse) {
        FirestoreManagerFacade.getDB().collection(USERS).document(user.uid).set(user)
            .addOnSuccessListener { }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun saveTemporalUser(user: UserResponse) {
        FirestoreManagerFacade.getDB().collection(TEMPORAL).document(user.uid).set(user)
            .addOnSuccessListener { }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun updateUser(user: UserResponse, onUpdated: (UserResponse) -> Unit) {
        val docRef = FirestoreManagerFacade.getDB().collection(USERS).document(user.uid)
        val userData = user.toMap()
        docRef.update(userData).addOnSuccessListener {
            onUpdated(user)
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }
    }

    fun deleteUser(user: UserResponse) {
        FirestoreManagerFacade.getAuth().currentUser?.delete()
        deleteCredentials()
        FirestoreManagerFacade.getDB().collection(USERS).document(user.uid).delete().addOnFailureListener {
            it.printStackTrace()
        }
    }

    private fun deleteTemporalUser(uid: String) {
        FirestoreManagerFacade.getDB().collection(TEMPORAL).document(uid).delete().addOnFailureListener {
            it.printStackTrace()
        }
    }

    fun saveCredentials(passPhraseHash: String, iv: String) {
        val user = Preferences.getUser()
        if (user != null) {
            val fields = mapOf(Pair("hash", passPhraseHash), Pair("iv", iv))
            FirestoreManagerFacade.getDB().collection(SECURE).document(user.uid).set(fields)
                .addOnSuccessListener { }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    private fun deleteCredentials() {
        val user = Preferences.getUser()
        if (user != null) {
            FirestoreManagerFacade.getDB().collection(SECURE).document(user.uid).delete()
                .addOnSuccessListener { }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    suspend fun getSecurePhraseHash(userUID: String): String? = withContext(Dispatchers.IO) {
        val docRef = FirestoreManagerFacade.getDB().collection(SECURE).document(userUID).get().await()

        return@withContext docRef.get("hash") as String?
    }

    suspend fun getInitialisationVector(userUID: String): String? = withContext(Dispatchers.IO) {
        val docRef = FirestoreManagerFacade.getDB().collection(SECURE).document(userUID).get().await()

        return@withContext docRef.get("iv") as String?
    }

    fun sendResetPassword(context: Context, email: String, result: (Boolean) -> Unit) {
        FirestoreManagerFacade.getAuth().sendPasswordResetEmail(email).addOnCompleteListener { task ->
            result(task.isSuccessful)
        }
    }
}