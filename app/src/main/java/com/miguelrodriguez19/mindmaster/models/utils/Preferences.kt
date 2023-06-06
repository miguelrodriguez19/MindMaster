package com.miguelrodriguez19.mindmaster.models.utils

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.miguelrodriguez19.mindmaster.models.structures.UserResponse
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.toJson
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.toUserResponse
import java.util.*

object Preferences {

    private const val USER_SETTINGS = "userSettings"
    private const val IV = "initializationVector"
    private const val TOKEN = "userToken"
    private const val SECURE_PHRASE = "securePhrase"

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

    fun clearAll() {
        clearUser()
        clearSecurePhrase()
        clearInitializationVector()
        clearToken()
    }

    fun getUser(): UserResponse? {
        val userSettings = getEncryptedSharedPrefs().getString(USER_SETTINGS, null)
        return userSettings?.toUserResponse()
    }

    fun getUserUID(): String = getUser()!!.uid

    fun setUser(user: UserResponse) {
        val json = user.toJson()
        getEncryptedSharedPrefs().edit().putString(USER_SETTINGS, json).apply()
    }

    private fun clearUser() {
        getEncryptedSharedPrefs().edit().remove(USER_SETTINGS).apply()
    }

    fun setToken(token: String?) {
        if (token != null) {
            getEncryptedSharedPrefs().edit().putString(TOKEN, token).apply()
        }
    }

    fun getToken(): String? = getEncryptedSharedPrefs().getString(TOKEN, null)

    private fun clearToken() {
        getEncryptedSharedPrefs().edit().remove(TOKEN).apply()
    }

    fun setSecurePhrase(securePhrase: String) {
        getEncryptedSharedPrefs().edit().putString(SECURE_PHRASE, securePhrase).apply()
    }

    fun getSecurePhrase(): String? = getEncryptedSharedPrefs().getString(SECURE_PHRASE, null)

    private fun clearSecurePhrase() {
        getEncryptedSharedPrefs().edit().remove(SECURE_PHRASE).apply()
    }
    fun setInitializationVector(iv: ByteArray) {
        val ivStr = Toolkit.parseByteArrayToString(iv)
        getEncryptedSharedPrefs().edit().putString(IV, ivStr).apply()
    }

    fun setInitializationVector(iv: String) {
        getEncryptedSharedPrefs().edit().putString(IV, iv).apply()
    }

    fun getInitializationVector(): ByteArray? {
        val iv: String? = getEncryptedSharedPrefs().getString(IV, null)
        return Base64.getDecoder().decode(iv) ?: null
    }

    private fun clearInitializationVector() {
        getEncryptedSharedPrefs().edit().remove(IV).apply()
    }

}
