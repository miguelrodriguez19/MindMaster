package com.miguelrodriguez19.mindmaster.models.utils

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.firebase.auth.GetTokenResult
import com.miguelrodriguez19.mindmaster.models.structures.UserResponse
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.toJson
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.toUserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

object Preferences {

    private const val USER_SETTINGS = "userSettings"
    private const val IV = "initializationVector"

    private fun getEncryptedSharedPrefs(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            masterKeyAlias,
            "user_prefs",
            MainApplication.instance,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getUser(): UserResponse? {
        val userSettings = getEncryptedSharedPrefs().getString(USER_SETTINGS, null)
        return userSettings?.toUserResponse()
    }

    fun setUser(user: UserResponse) {
        val json = user.toJson()
        getEncryptedSharedPrefs().edit().putString(USER_SETTINGS, json).apply()
    }

    fun clearUser() {
        getEncryptedSharedPrefs().edit().remove(USER_SETTINGS).apply()
    }

    fun getUserUID(): String {
        return getUser()!!.uid
    }

    suspend fun getToken(): GetTokenResult? = withContext(Dispatchers.IO) {
        return@withContext FirebaseManager.getAuth().currentUser?.getIdToken(false)
            ?.await<GetTokenResult?>()
    }

    fun setSecurePhrase() {

    }

    fun getSecurePhrase(): String {
        return "123456789"
    }

    fun setInitializationVector(iv: ByteArray) {
        val ivStr = Base64.getEncoder().encodeToString(iv)
        getEncryptedSharedPrefs().edit().putString(IV, ivStr).apply()
    }

    fun getInitializationVector(): ByteArray {
        val iv = getEncryptedSharedPrefs().getString(IV, null)
        return Base64.getDecoder().decode(iv)
    }

    fun clearSecurePhrase() {

    }
}
