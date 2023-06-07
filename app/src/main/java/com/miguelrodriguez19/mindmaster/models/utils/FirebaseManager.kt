package com.miguelrodriguez19.mindmaster.models.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.models.comparators.AccountsGroupsComparator
import com.miguelrodriguez19.mindmaster.models.comparators.EventComparator
import com.miguelrodriguez19.mindmaster.models.comparators.EventGroupComparator
import com.miguelrodriguez19.mindmaster.models.comparators.MovementsGroupComparator
import com.miguelrodriguez19.mindmaster.models.structures.*
import com.miguelrodriguez19.mindmaster.models.structures.GroupPasswordsResponse.Account
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse.Movement
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse.Type
import com.miguelrodriguez19.mindmaster.models.utils.Preferences.getUserUID
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.getMonthYearOf
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.showToast
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

object FirebaseManager {

    private const val TAG = "FIREBASE_MANAGER"
    private const val SECURE = "secure"
    private const val USERS = "users"
    private const val SCHEDULE = "schedule"
    private const val EVENTS = "events"
    private const val REMINDERS = "reminders"
    private const val TASKS = "tasks"
    private const val DATE = "date"
    private const val MOVEMENTS = "movements"
    private const val INCOMES = "incomes"
    private const val EXPENSES = "expenses"
    private const val GROUPS = "groups"
    private const val ACCOUNTS = "accounts"
    private const val KEY = "key"
    private const val TEMPORAL = "temporal"

    fun getAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    private fun getDB(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings
        return db
    }

    private fun getUserByUID(uid: String, callback: (UserResponse?) -> Unit) {
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
        getDB().collection(USERS).document(uid).get().addOnCompleteListener { task ->
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
        getDB().collection(TEMPORAL).document(uid).get().addOnCompleteListener { task ->
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
        updateUser(upUser) {
            Preferences.setUser(upUser)
        }
    }

    private fun saveImageInStorage(context: Context, imageUrl: String, callback: (String) -> Unit) {
        Glide.with(context).asBitmap().load(imageUrl).into(object : SimpleTarget<Bitmap>() {
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

    // LOG_IN & SIGN_UP
    fun logInEmailPwd(
        email: String,
        password: String,
        callback: (Boolean, UserResponse?, String?) -> Unit
    ) {
        getAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = it.result.user
                if (user != null && user.isEmailVerified) {
                    getUserByUID(user.uid) { u ->
                        if (u != null) {
                            val token = user.getIdToken(false).result.token
                            callback(true, u, token)
                        } else {
                            callback(false, null, null)
                        }
                    }
                } else {
                    callback(false, null, null)
                }
            } else {
                callback(false, null, null)
            }
        }
    }

    fun createUser(
        context: Context,
        name: String,
        lastname: String?,
        birthdate: String,
        email: String,
        password: String,
        result: (Boolean) -> Unit
    ) {
        getAuth().createUserWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val theme = context.getString(R.string.photo_themes).split(",").shuffled()[0]
                    val url = context.getString(R.string.photo_url_request, theme)
                    saveImageInStorage(
                        context, url
                    ) { photoUrl ->
                        task.result.user?.sendEmailVerification()
                        saveTemporalUser(
                            UserResponse(
                                task.result.user?.uid!!,
                                name,
                                lastname,
                                email,
                                birthdate,
                                photoUrl,
                                false
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

    fun saveUser(user: UserResponse) {
        getDB().collection(USERS).document(user.uid).set(user).addOnSuccessListener { }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun saveTemporalUser(user: UserResponse) {
        getDB().collection(TEMPORAL).document(user.uid).set(user).addOnSuccessListener { }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun updateUser(user: UserResponse, onUpdated: (UserResponse) -> Unit) {
        val docRef = getDB().collection(USERS).document(user.uid)
        val userData = user.toMap()
        docRef.update(userData).addOnSuccessListener {
                onUpdated(user)
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun deleteUser(user: UserResponse) {
        getDB().collection(USERS).document(user.uid).delete().addOnFailureListener {
            it.printStackTrace()
        }
    }

    private fun deleteTemporalUser(uid: String) {
        getDB().collection(TEMPORAL).document(uid).delete().addOnFailureListener {
            it.printStackTrace()
        }
    }

    // SCHEDULE
    private fun getRefAndDate(absEvent: AbstractEvent, callback: (String, String) -> Unit) {

        val date = AbstractEvent.getDateOf(absEvent)
        val ref = when (absEvent.type) {
            EventType.EVENT -> EVENTS
            EventType.REMINDER -> REMINDERS
            EventType.TASK -> TASKS
        }
        callback(ref, date)
    }

    fun saveInSchedule(
        context: Context, absEvent: AbstractEvent, onAdded: (AbstractEvent) -> Unit
    ) {
        val userUID = getUserUID()
        if (userUID != null) {
            getRefAndDate(absEvent) { ref, date ->
                val schedule =
                    getDB().collection(USERS).document(userUID).collection(SCHEDULE).document(date)
                schedule.set(mapOf(Pair(DATE, date)))

                val collection = schedule.collection(ref)
                collection.add(absEvent).addOnCompleteListener { obj ->
                    if (obj.isSuccessful) {
                        val id = obj.result.id
                        when (absEvent.type) {
                            EventType.EVENT -> {
                                onAdded(Event(id, (absEvent as Event)))
                            }
                            EventType.REMINDER -> {
                                onAdded(Reminder(id, (absEvent as Reminder)))
                            }
                            EventType.TASK -> {
                                onAdded(Task(id, (absEvent as Task)))
                            }
                        }
                    } else {
                        obj.exception?.printStackTrace()
                        showToast(context, R.string.try_later)
                    }
                }
            }
        }
    }

    fun updateInSchedule(
        context: Context, absEvent: AbstractEvent, onUpdated: (AbstractEvent?) -> Unit
    ) {
        val userUID = getUserUID()
        if (userUID != null) {
            getRefAndDate(absEvent) { ref, date ->

                val collectionRef =
                    getDB().collection(USERS).document(userUID).collection(SCHEDULE).document(date)
                collectionRef.update(mapOf(Pair(DATE, date)))

                val docRef = collectionRef.collection(ref).document(absEvent.uid)
                docRef.set(absEvent).addOnCompleteListener { obj ->
                    if (obj.isSuccessful) {
                        onUpdated(absEvent)
                    } else {
                        obj.exception?.printStackTrace()
                        showToast(context, R.string.try_later)
                    }
                }
            }
        }
    }

    fun deleteInSchedule(
        context: Context, absEvent: AbstractEvent, onDeleted: (AbstractEvent) -> Unit
    ) {
        val userUID = getUserUID()
        if (userUID != null) {
            getRefAndDate(absEvent) { ref, date ->
                val collectionRef =
                    getDB().collection(USERS).document(userUID).collection(SCHEDULE).document(date)
                        .collection(ref)

                collectionRef.document(absEvent.uid).delete().addOnSuccessListener {
                        onDeleted(absEvent)
                    }.addOnFailureListener { exception ->
                        exception.printStackTrace()
                        showToast(context, R.string.try_later)
                    }
            }
        }
    }


    suspend fun loadScheduleByDate(
        context: Context, date: String
    ): List<AbstractEvent> = withContext(Dispatchers.IO) {
        val userUID = getUserUID()
        val dayList = ArrayList<AbstractEvent>()
        if (userUID != null) {
            val documentSnapshot: DocumentSnapshot
            try {
                documentSnapshot =
                    getDB().collection(USERS).document(userUID).collection(SCHEDULE).document(date)
                        .get().await()
            } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
                System.err.println("${e.message}\n${e.printStackTrace()}")
                return@withContext dayList
            }

            val eventsDeferred =
                async { documentSnapshot.reference.collection(EVENTS).get().await() }
            val reminderDeferred =
                async { documentSnapshot.reference.collection(REMINDERS).get().await() }
            val tasksDeferred = async { documentSnapshot.reference.collection(TASKS).get().await() }

            val eventsQuerySnapshot = eventsDeferred.await()
            val reminderQuerySnapshot = reminderDeferred.await()
            val tasksQuerySnapshot = tasksDeferred.await()

            for (eventDoc in eventsQuerySnapshot.documents) {
                val event = eventDoc.toObject(Event::class.java)
                if (event != null) {
                    dayList.add(Event(eventDoc.id, event))
                }
            }

            for (reminderDoc in reminderQuerySnapshot.documents) {
                val reminder = reminderDoc.toObject(Reminder::class.java)
                if (reminder != null) {
                    dayList.add(Reminder(reminderDoc.id, reminder))
                }
            }

            for (taskDoc in tasksQuerySnapshot.documents) {
                val task = taskDoc.toObject(Task::class.java)
                if (task != null) {
                    dayList.add(Task(taskDoc.id, task))
                }
            }
        }

        return@withContext dayList.sortedWith(EventComparator())
    }

    fun loadAllSchedule(context: Context, callback: (List<EventsResponse>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val userUID = getUserUID()
            if (userUID != null) {
                val scheduleRef =
                    getDB().collection(USERS).document(userUID).collection(SCHEDULE).get().await()

                val deferredList = mutableListOf<Deferred<EventsResponse?>>()

                for (doc in scheduleRef) {
                    val deferred = async {
                        val dayList = loadScheduleByDate(context, doc.id)
                        if (dayList.isNotEmpty()) {
                            EventsResponse(doc.id, dayList)
                        } else {
                            null
                        }
                    }
                    deferredList.add(deferred)
                }

                val allEvents = deferredList.awaitAll().filterNotNull()

                withContext(Dispatchers.Main) {
                    callback(allEvents.sortedWith(EventGroupComparator()))
                }
            }
        }
    }


    // MOVEMENTS
    fun saveMovement(context: Context, move: Movement, callback: (Movement) -> Unit) {
        val userUID = getUserUID()
        if (userUID != null) {
            val collectionRef = getDB().collection(USERS).document(userUID).collection(MOVEMENTS)
                .document(getMonthYearOf(move.date))

            collectionRef.set(mapOf(Pair(DATE, getMonthYearOf(move.date))))
            val collectionType = when (move.type) {
                Type.INCOME -> {
                    collectionRef.collection(INCOMES)
                }
                Type.EXPENSE -> collectionRef.collection(EXPENSES)
            }
            collectionType.add(move).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentId = task.result.id
                    callback(Movement(documentId, move))
                } else {
                    task.exception?.printStackTrace()
                    showToast(context, R.string.try_later)
                }
            }
        }
    }

    fun updateMovement(
        context: Context, movement: Movement, onUpdated: (Movement?) -> Unit
    ) {
        val userUID = getUserUID()
        if (userUID != null) {
            val date = movement.date
            val collectionRef = getDB().collection(USERS).document(userUID).collection(MOVEMENTS)
                .document(getMonthYearOf(date))

            collectionRef.update(mapOf(Pair(DATE, getMonthYearOf(date))))
            val collectionType = when (movement.type) {
                Type.INCOME -> {
                    collectionRef.collection(INCOMES)
                }
                Type.EXPENSE -> collectionRef.collection(EXPENSES)
            }
            collectionType.document(movement.uid).set(movement).addOnCompleteListener { obj ->
                if (obj.isSuccessful) {
                    onUpdated(movement)
                } else {
                    obj.exception?.printStackTrace()
                    showToast(context, R.string.try_later)
                }
            }
        }
    }

    suspend fun loadActualMonthMovements(
        context: Context, currentMonth: String
    ): MonthMovementsResponse = withContext(Dispatchers.IO) {
        val incomesList = ArrayList<Movement>()
        val expensesList = ArrayList<Movement>()
        val userUID = getUserUID()
        if (userUID != null) {
            val documentSnapshot = getDB().collection(USERS).document(userUID).collection(MOVEMENTS)
                .document(currentMonth).get().await()

            val incomesDeferred =
                async { documentSnapshot.reference.collection(INCOMES).get().await() }
            val expensesDeferred =
                async { documentSnapshot.reference.collection(EXPENSES).get().await() }

            val incomesQuerySnapshot = incomesDeferred.await()
            val expensesQuerySnapshot = expensesDeferred.await()
            for (income in incomesQuerySnapshot.documents) {
                val m = income.toObject(Movement::class.java)
                if (m != null) {
                    incomesList.add(Movement(income.id, m))
                }
            }

            for (expense in expensesQuerySnapshot.documents) {
                val m = expense.toObject(Movement::class.java)
                if (m != null) {
                    expensesList.add(Movement(expense.id, m))
                }
            }
        }

        return@withContext MonthMovementsResponse(currentMonth, incomesList, expensesList)
    }

    fun loadAllMovements(context: Context, callback: (List<MonthMovementsResponse>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val userUID = getUserUID()
            if (userUID != null) {
                val movementsRef =
                    getDB().collection(USERS).document(userUID).collection(MOVEMENTS).get().await()

                val deferredList = mutableListOf<Deferred<MonthMovementsResponse?>>()

                for (doc in movementsRef) {

                    val deferred = async {
                        val response = loadActualMonthMovements(context, doc.id)
                        if (response.expensesList.isNotEmpty() || response.incomeList.isNotEmpty()) {
                            response
                        } else {
                            null
                        }
                    }
                    deferredList.add(deferred)
                }

                val allMovementsResponses = deferredList.awaitAll().filterNotNull()

                withContext(Dispatchers.Main) {
                    callback(allMovementsResponses.sortedWith(MovementsGroupComparator()))
                }
            }
        }
    }

    fun deleteMovement(context: Context, movement: Movement, onDeleted: (Movement) -> Unit) {
        val userUID = getUserUID()
        if (userUID != null) {
            val collectionRef = getDB().collection(USERS).document(userUID).collection(MOVEMENTS)
                .document(getMonthYearOf(movement.date))

            val collectionType = when (movement.type) {
                Type.INCOME -> collectionRef.collection(INCOMES)
                Type.EXPENSE -> collectionRef.collection(EXPENSES)
            }
            collectionType.document(movement.uid).delete().addOnSuccessListener {
                    onDeleted(movement)
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                    showToast(context, R.string.try_later)
                }
        }
    }

    // PASSWORDS
    fun saveGroup(
        context: Context, group: GroupPasswordsResponse, onSuccess: (GroupPasswordsResponse) -> Unit
    ) {
        val accountList = ArrayList<Account>()
        val userUID = getUserUID()
        if (userUID != null) {
            val groupRef = getDB().collection(USERS).document(userUID).collection(GROUPS).document()

            groupRef.set(mapOf(Pair("name", group.name)))
            val deferreds = group.accountsList.map { account ->
                CoroutineScope(Dispatchers.IO).async {
                    val accJson = Gson().toJson(account.copy(uid = groupRef.id))
                    val encryptedAccount = AESEncripter.encrypt(accJson)
                    val accountUid = groupRef.collection(ACCOUNTS).document().id
                    groupRef.collection(ACCOUNTS).document(accountUid)
                        .set(mapOf(Pair("account", encryptedAccount))).addOnCompleteListener {
                            if (it.isSuccessful) {
                                accountList.add(account.copy(uid = accountUid))
                            }
                        }.await()
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                deferreds.forEach { it.await() }
                if (accountList.size == group.accountsList.size) {
                    onSuccess(group.copy(accountsList = accountList))
                }
            }
        }
    }


    fun updateGroup(
        context: Context, group: GroupPasswordsResponse, onUpdated: (GroupPasswordsResponse) -> Unit
    ) {
        val userUID = getUserUID()
        if (userUID != null) {
            val groupRef =
                getDB().collection(USERS).document(userUID).collection(GROUPS).document(group.uid)

            groupRef.update(mapOf(Pair("name", group.name)))

            val accountsRef = groupRef.collection(ACCOUNTS)

            val updatedAccountsList = ArrayList<Account>()
            deleteOldAccounts(group)

            val deferreds = group.accountsList.map { account ->
                CoroutineScope(Dispatchers.IO).async {
                    val accountRef = accountsRef.document()
                    val accountJson = Gson().toJson(account)
                    val encryptedAccount = AESEncripter.encrypt(accountJson)
                    accountRef.set(mapOf(Pair("account", encryptedAccount))).addOnCompleteListener {
                            if (it.isSuccessful) {
                                updatedAccountsList.add(account.copy(uid = accountRef.id))
                            }
                        }.await()
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                deferreds.forEach { it.await() }
                if (updatedAccountsList.size == group.accountsList.size) {
                    onUpdated(group.copy(accountsList = updatedAccountsList))
                }
            }
        }
    }

    private fun deleteOldAccounts(group: GroupPasswordsResponse) {
        val userUID = getUserUID()
        if (userUID != null) {
            val collectionRef =
                getDB().collection(USERS).document(userUID).collection(GROUPS).document(group.uid)
                    .collection(ACCOUNTS)

            collectionRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    for (account in it.result.documents) {
                        val uid = account.id
                        collectionRef.document(uid).delete().addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }


    private suspend fun loadGroup(
        context: Context, groupUID: String
    ): GroupPasswordsResponse = withContext(Dispatchers.IO) {
        lateinit var groupName: String
        val accountList = ArrayList<Account>()
        val userUID = getUserUID()
        if (userUID != null) {
            val groupRef =
                getDB().collection(USERS).document(userUID).collection(GROUPS).document(groupUID)


            groupName = groupRef.get().await().data?.getOrDefault("name", null) as String

            val docsSnap = groupRef.collection(ACCOUNTS).get().await().documents
            for (accountDoc in docsSnap) {
                try {
                    val encryptedAcc = accountDoc.get("account") as String
                    val accJson = AESEncripter.decrypt(encryptedAcc)
                    val account = Gson().fromJson(accJson, Account::class.java)
                    accountList.add(account)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }
        }
        return@withContext GroupPasswordsResponse(groupUID, groupName, accountList)
    }

    fun loadAllGroups(
        context: Context, onSuccess: (List<GroupPasswordsResponse>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val userUID = getUserUID()
            if (userUID != null) {
                val movementsRef =
                    getDB().collection(USERS).document(userUID).collection(GROUPS).get().await()

                val deferredList = mutableListOf<Deferred<GroupPasswordsResponse?>>()

                for (doc in movementsRef) {

                    val deferred = async {
                        val response = loadGroup(context, doc.id)
                        if (response.accountsList.isNotEmpty()) {
                            response
                        } else {
                            null
                        }
                    }
                    deferredList.add(deferred)
                }

                val allGroupsResponse = deferredList.awaitAll().filterNotNull()

                withContext(Dispatchers.Main) {
                    onSuccess(allGroupsResponse.sortedWith(AccountsGroupsComparator()))
                }
            }
        }
    }

    fun deleteGroup(
        context: Context, group: GroupPasswordsResponse, onDeleted: (GroupPasswordsResponse) -> Unit
    ) {
        val userUID = getUserUID()
        if (userUID != null) {
            getDB().collection(USERS).document(userUID).collection(GROUPS).document(group.uid)
                .delete().addOnSuccessListener {
                    onDeleted(group)
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                    showToast(context, R.string.try_later)
                }
        }
    }

    suspend fun getWords(): List<String> = withContext(Dispatchers.IO) {
        return@withContext getDB().collection(SECURE).document("secureWords").get().await()
            .get("words") as List<String>
    }

    fun saveCredentials(passPhraseHash: String, iv: String) {
        val user = Preferences.getUser()
        if (user != null) {
            val fields = mapOf(Pair("hash", passPhraseHash), Pair("iv", iv))
            getDB().collection(SECURE).document(user.uid).set(fields).addOnSuccessListener { }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    suspend fun getSecurePhraseHash(userUID: String): String? = withContext(Dispatchers.IO) {
        val docRef = getDB().collection(SECURE).document(userUID).get().await()

        return@withContext docRef.get("hash") as String?
    }

    suspend fun getInitialisationVector(userUID: String): String? = withContext(Dispatchers.IO) {
        val docRef = getDB().collection(SECURE).document(userUID).get().await()

        return@withContext docRef.get("iv") as String?
    }

}