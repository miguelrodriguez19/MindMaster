package com.miguelrodriguez19.mindmaster.models.firebase

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.miguelrodriguez19.mindmaster.models.structures.*
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse.Movement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*

object FirebaseManager {

    private lateinit var appContext: Context

    private const val SECURE = "secure"

    fun init(context: Context) {
        this.appContext = context.applicationContext
    }

    fun getAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    fun getDB(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings
        return db
    }

    fun getUserByUID(uid: String, callback: (UserResponse?) -> Unit) =
        FUserManager.getUserByUID(uid) { callback(it) }

    fun updateHasLoggedInBefore(user: UserResponse) = FUserManager.updateHasLoggedInBefore(user)

    fun saveImageInStorage(imageUrl: String, callback: (String) -> Unit) {
        Glide.with(appContext).asBitmap().load(imageUrl).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val storageRef = FirebaseStorage.getInstance().reference
                val imagesRef = storageRef.child("images")
                val imageFileName = UUID.randomUUID().toString() + ".jpg"
                val imageRef = imagesRef.child(imageFileName)

                val baos = ByteArrayOutputStream()
                resource.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val imageData = baos.toByteArray()

                val uploadTask = imageRef.putBytes(imageData)
                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        callback(uri.toString())
                    }
                }
            }
        })
    }

    fun getCurrentUser(): FirebaseUser? = FUserManager.getCurrentUser()

    // LOG_IN & SIGN_UP
    fun logInEmailPwd(
        email: String, password: String, callback: (Boolean, UserResponse?) -> Unit
    ) = FUserManager.logInEmailPwd(email, password) { bool, user -> callback(bool, user) }

    fun createUser(
        name: String, lastname: String?, birthdate: String, email: String, password: String,
        result: (Boolean) -> Unit
    ) = FUserManager.createUser(
        appContext, name, lastname, birthdate, email, password
    ) { result(it) }

    fun updateUser(user: UserResponse, onUpdated: (UserResponse) -> Unit) =
        FUserManager.updateUser(user) { onUpdated(it) }

    fun deleteUser(user: UserResponse) = FUserManager.deleteUser(user)

    // SCHEDULE
    fun saveInSchedule(
        absEvent: AbstractEvent, onAdded: (AbstractEvent) -> Unit
    ) = FScheduleManager.saveInSchedule(appContext, absEvent) { onAdded(it) }

    fun updateInSchedule(
        absEvent: AbstractEvent, onUpdated: (AbstractEvent?) -> Unit
    ) = FScheduleManager.updateInSchedule(appContext, absEvent) { onUpdated(it) }

    fun deleteInSchedule(
        absEvent: AbstractEvent, onDeleted: (AbstractEvent) -> Unit
    ) = FScheduleManager.deleteInSchedule(appContext, absEvent) { onDeleted(it) }

    suspend fun loadScheduleByDate(
        date: String
    ): List<AbstractEvent> = FScheduleManager.loadScheduleByDate(appContext, date)

    fun loadAllSchedule(callback: (List<EventsResponse>) -> Unit) =
        FScheduleManager.loadAllSchedule(appContext) { callback(it) }

    // MOVEMENTS
    fun saveMovement(move: Movement, callback: (Movement) -> Unit) =
        FMovementManager.saveMovement(appContext, move) { callback(it) }

    fun updateMovement(
        movement: Movement, onUpdated: (Movement?) -> Unit
    ) = FMovementManager.updateMovement(appContext, movement) { onUpdated(it) }

    suspend fun loadActualMonthMovements(
        currentMonth: String
    ): MonthMovementsResponse = FMovementManager.loadActualMonthMovements(appContext, currentMonth)

    fun loadAllMovements(callback: (List<MonthMovementsResponse>) -> Unit) =
        FMovementManager.loadAllMovements(appContext) { callback(it) }

    fun deleteMovement(movement: Movement, onDeleted: (Movement) -> Unit) =
        FMovementManager.deleteMovement(appContext, movement) { onDeleted(it) }

    // PASSWORDS
    fun saveGroup(
        group: GroupPasswordsResponse, onSuccess: (GroupPasswordsResponse) -> Unit
    ) = FAccountVaultManager.saveGroup(appContext, group) { onSuccess(it) }


    fun updateGroup(
        group: GroupPasswordsResponse, onUpdated: (GroupPasswordsResponse) -> Unit
    ) = FAccountVaultManager.updateGroup(appContext, group) { onUpdated(it) }

    fun loadAllGroups(
        onSuccess: (List<GroupPasswordsResponse>) -> Unit
    ) = FAccountVaultManager.loadAllGroups(appContext) { onSuccess(it) }

    fun deleteGroup(
        group: GroupPasswordsResponse, onDeleted: (GroupPasswordsResponse) -> Unit
    ) = FAccountVaultManager.deleteGroup(appContext, group) { onDeleted(it) }

    suspend fun getWords(): List<String> = withContext(Dispatchers.IO) {
        return@withContext getDB().collection(SECURE).document("secureWords").get().await()
            .get("words") as List<String>
    }

    fun saveCredentials(passPhraseHash: String, iv: String) =
        FUserManager.saveCredentials(passPhraseHash, iv)

    suspend fun getSecurePhraseHash(userUID: String): String? =
        FUserManager.getSecurePhraseHash(userUID)

    suspend fun getInitialisationVector(userUID: String): String? =
        FUserManager.getInitialisationVector(userUID)

    fun sendResetPassword(context: Context, email: String, result: (Boolean) -> Unit) =
        FUserManager.sendResetPassword(appContext, email) { result(it) }

}