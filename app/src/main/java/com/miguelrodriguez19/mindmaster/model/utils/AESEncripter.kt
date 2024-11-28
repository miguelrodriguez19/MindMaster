package com.miguelrodriguez19.mindmaster.model.utils

import android.content.Context
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import kotlinx.coroutines.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AESEncripter {

    private lateinit var appContext: Context

    private val AES_KEY_SIZE: Int by lazy {
        appContext.resources.getString(R.string.AES_KEY_SIZE).toInt()
    }

    private val GCM_IV_LENGTH: Int by lazy {
        appContext.resources.getString(R.string.GCM_IV_LENGTH).toInt()
    }

    private val GCM_TAG_LENGTH: Int by lazy {
        appContext.resources.getString(R.string.GCM_TAG_LENGTH).toInt()
    }

    private val SHA: String by lazy {
        appContext.resources.getString(R.string.hash_algorithm)
    }

    fun init(context: Context) {
        this.appContext = context.applicationContext
    }

    fun generateHash(text: String): String {
        val md = MessageDigest.getInstance(SHA)
        val hash = md.digest(text.toByteArray())
        return Toolkit.parseByteArrayToString(hash)
    }

    fun generateSecurePhrase(generatedPhrase: (String) -> Unit ) {
        var words: List<String> = emptyList()
        CoroutineScope(Dispatchers.IO).async {
            words = FirestoreManagerFacade.getWords()
        }.invokeOnCompletion {
            val securePhrase = HashSet<String>()
            val wordCount = appContext.getString(R.string.wordCount4PasswordPhrase).toInt()

            while (securePhrase.size <= wordCount) {
                securePhrase.add(words.random())
            }

            CoroutineScope(Dispatchers.Main).launch {
                generatedPhrase(securePhrase.joinToString(" "))
            }
        }
    }

    fun generateInitializationVector(): String {
        val iniVector = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iniVector)
        return Toolkit.parseByteArrayToString(iniVector)
    }

    private fun generateKey(securePhrase: String, iv: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(securePhrase.toCharArray(), iv, 65536, AES_KEY_SIZE)
        val secretKeyTemp = factory.generateSecret(spec)

        return SecretKeySpec(secretKeyTemp.encoded, "AES")
    }

    fun encrypt(plaintext: String): String {
        val iv = Preferences.getInitializationVector()
        val key = Preferences.getSecurePhrase()
            ?.let { phrase -> iv?.let { vector -> generateKey(phrase, vector) } }
        if (key != null) {
            val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = SecretKeySpec(key.encoded, "AES")
            val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec)
            val encryptedMessage = cipher.doFinal(plaintext.toByteArray())

            return Toolkit.parseByteArrayToString(encryptedMessage)
        }
        return ""
    }

    fun decrypt(cipherText: String): String {
        val iv = Preferences.getInitializationVector()
        val key = Preferences.getSecurePhrase()
            ?.let { phrase -> iv?.let { vector -> generateKey(phrase, vector) } }
        if (key != null) {
            val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = SecretKeySpec(key.encoded, "AES")
            val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec)
            val encryptedMessage = Base64.getDecoder().decode(cipherText)
            val decryptedMessage = cipher.doFinal(encryptedMessage)

            return String(decryptedMessage)
        }
        return ""
    }

}