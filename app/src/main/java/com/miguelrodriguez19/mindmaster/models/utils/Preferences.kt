package com.miguelrodriguez19.mindmaster.models.utils

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.miguelrodriguez19.mindmaster.models.structures.UserResponse
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.toJson
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.toUserResponse

object Preferences {
    private const val USER_SETTINGS = "userSettings"

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

    fun deleteUser() {
        getEncryptedSharedPrefs().edit().remove(USER_SETTINGS).apply()
    }

    fun getUserUID(): String {
        return getUser()!!.uid
    }
}
