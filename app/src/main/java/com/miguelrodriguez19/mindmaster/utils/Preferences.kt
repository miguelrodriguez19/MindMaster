package com.miguelrodriguez19.mindmaster.utils

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object Preferences {
   private const val TOKEN_SETTINGS = "tokenSettings"
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

   fun getToken(): String? {
       val userSettings = getEncryptedSharedPrefs().getString(TOKEN_SETTINGS, null)
       return userSettings
   }

   fun setToken(token: String) {
       getEncryptedSharedPrefs().edit().putString(TOKEN_SETTINGS, token).apply()
   }

    fun deleteToken() {
        getEncryptedSharedPrefs().edit().remove(TOKEN_SETTINGS).apply()
    }
}
