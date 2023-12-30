package com.miguelrodriguez19.mindmaster.model.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.structures.dto.UserResponse
import com.miguelrodriguez19.mindmaster.model.utils.jsonUtils.toJson
import com.miguelrodriguez19.mindmaster.model.utils.jsonUtils.toUserResponse
import java.util.*

object Preferences {

    private lateinit var appContext: Context

    private val THEME:String by lazy{ appContext.resources.getString(R.string.theme_preferences_key)}
    private val USER_SETTINGS:String by lazy{ appContext.resources.getString(R.string.user_settings_preferences_key)}
    private val CURRENCY:String by lazy{ appContext.resources.getString(R.string.currency_preferences_key)}
    private val IV:String by lazy{ appContext.resources.getString(R.string.init_vector_preferences_key)}
    private val SECURE_PHRASE:String by lazy{ appContext.resources.getString(R.string.secure_phrase_preferences_key)}
    private val LAST_NOTIFICATION_ID:String by lazy{ appContext.resources.getString(R.string.last_notification_id_key)}
    private val GLOBAL_NOTIFICATIONS:String by lazy{ appContext.resources.getString(R.string.global_notifications_key)}

    fun init(context: Context) {
        this.appContext = context.applicationContext
    }

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

    fun saveTheme(position: Int) {
        getEncryptedSharedPrefs().edit().putString(THEME, position.toString()).apply()
    }

    fun getTheme() = getEncryptedSharedPrefs().getString(THEME, "-1") ?: "-1"

    fun clearUserPreferences() {
        clearUser()
        clearSecurePhrase()
        clearInitializationVector()
    }

    fun getUser(): UserResponse? {
        val userSettings = getEncryptedSharedPrefs().getString(USER_SETTINGS, null)
        return userSettings?.toUserResponse()
    }

    fun getUserUID(): String? = getUser()?.uid

    fun setUser(user: UserResponse) {
        val json = user.toJson()
        getEncryptedSharedPrefs().edit().putString(USER_SETTINGS, json).apply()
    }

    private fun clearUser() {
        getEncryptedSharedPrefs().edit().remove(USER_SETTINGS).apply()
    }

    fun setCurrency(currency: String) {
        getEncryptedSharedPrefs().edit().putString(CURRENCY, currency).apply()
    }

    fun getCurrency(): String = getEncryptedSharedPrefs().getString(CURRENCY, null) ?: "€"

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

    fun getNextNotificationId(): Int {
        val lastId = getEncryptedSharedPrefs().getInt(LAST_NOTIFICATION_ID, 1000) + 1
        getEncryptedSharedPrefs().edit().putInt(LAST_NOTIFICATION_ID, lastId).apply()
        return lastId
    }

    fun setGlobalNotificationsEnabled(enabled: Boolean) {
        getEncryptedSharedPrefs().edit().putBoolean(GLOBAL_NOTIFICATIONS, enabled).apply()
    }

    fun areGlobalNotificationsEnabled(): Boolean {
        return getEncryptedSharedPrefs().getBoolean(GLOBAL_NOTIFICATIONS, true)
    }
}
