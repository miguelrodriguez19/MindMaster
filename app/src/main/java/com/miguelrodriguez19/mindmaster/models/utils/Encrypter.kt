package com.miguelrodriguez19.mindmaster.models.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.*
import java.util.stream.Collectors
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.streams.toList

object Encrypter {
    private const val ENCRYPTION_ALGORITHM = "AES/ECB/PKCS5Padding"
    private const val SHA = "SHA-256"
    private const val wordCount = 20

    private fun encrypt(text: String, pwd: String): String {
        try {
            val key = generateHash(pwd)
            val secretKey = SecretKeySpec(key, ENCRYPTION_ALGORITHM)

            val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedMessage = cipher.doFinal(text.toByteArray())
            return Base64.getEncoder().encodeToString(encryptedMessage)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun decrypt(nota: String, pass: String): String {
        try {
            val key = generateHash(pass)
            val secretKey = SecretKeySpec(key, ENCRYPTION_ALGORITHM)

            val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)

            val encryptedMessage = Base64.getDecoder().decode(nota)
            val decryptedMessage = cipher.doFinal(encryptedMessage)
            return String(decryptedMessage)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun generateHash(pass: String): ByteArray {
        val md = MessageDigest.getInstance(SHA)
        return md.digest(pass.toByteArray())
    }

    fun generateSecurityPhrase(): String {
        lateinit var words: List<String>
        val securePhrase = HashSet<String>()

        CoroutineScope(Dispatchers.IO).launch {
            // TODO(get from firesotre the words list)
        }.onJoin

        while(securePhrase.size <= wordCount){
            securePhrase.add(words.random())
        }

        return securePhrase.stream().collect(Collectors.joining("\\s"))
    }
}