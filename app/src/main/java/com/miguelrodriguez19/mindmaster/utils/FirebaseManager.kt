package com.miguelrodriguez19.mindmaster.utils

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
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.models.*
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse.Movement
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse.Type
import com.miguelrodriguez19.mindmaster.models.comparators.EventComparator
import com.miguelrodriguez19.mindmaster.models.comparators.EventGroupComparator
import com.miguelrodriguez19.mindmaster.models.comparators.MovementsGroupComparator
import com.miguelrodriguez19.mindmaster.utils.Preferences.getUserUID
import com.miguelrodriguez19.mindmaster.utils.Toolkit.getMonthYearOf
import com.miguelrodriguez19.mindmaster.utils.Toolkit.showToast
import com.miguelrodriguez19.mindmaster.utils.Toolkit.toJson
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

object FirebaseManager {

    private const val TAG = "FIREBASE_MANAGER"
    private const val USERS = "users"
    private const val SCHEDULE = "schedule"
    private const val EVENTS = "events"
    private const val REMINDERS = "reminders"
    private const val TASKS = "tasks"
    private const val DATE = "date"
    private const val MOVEMENTS = "movements"
    private const val INCOMES = "incomes"
    private const val EXPENSES = "expenses"

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
        activity: FragmentActivity, email: String, password: String, callback: (Boolean) -> Unit
    ) {
        getAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                /*it.result.user?.getIdToken(false)?.addOnCompleteListener { t ->
                    if (t.isSuccessful) {
                        Log.i("TOKEN", t.result.token!!)
                        Log.i("TOKEN", t.result.expirationTimestamp.toString())
                        Log.i("TOKEN", t.result.signInProvider!!)
                    } else {
                        println(t.exception)
                    }
                }*/
                it.result.user?.let { firebaseUser ->
                    getUserByUID(firebaseUser.uid) { user ->
                        Preferences.setUser(user!!)
                        (activity as MainActivity).userSetUp(user)
                        callback(true)
                    }
                }
            }
        }
    }

    fun createUserFirebase(
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
                    saveImageInStorage(
                        context, "https://source.unsplash.com/200x200/?random"
                    ) { photoUrl ->
                        saveUser(
                            UserResponse(
                                task.result.user?.uid!!, name, lastname, email, birthdate, photoUrl
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

    fun updateUser(user: UserResponse, onUpdated: (UserResponse) -> Unit) {
        val docRef = getDB().collection(USERS).document(user.uid)
        val userData = user.toMap()
        docRef.update(userData)
            .addOnSuccessListener {
                onUpdated(user)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun deleteUser(user: UserResponse) {
        getDB().collection(USERS).document(user.uid).delete().addOnFailureListener {
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
        val usersUID = getUserUID()
        getRefAndDate(absEvent) { ref, date ->
            val schedule =
                getDB().collection(USERS).document(usersUID).collection(SCHEDULE)
                    .document(date)
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

    fun updateInSchedule(
        context: Context, absEvent: AbstractEvent, onUpdated: (AbstractEvent?) -> Unit
    ) {
        val usersUID = getUserUID()
        getRefAndDate(absEvent) { ref, date ->

            val collectionRef =
                getDB().collection(USERS).document(usersUID).collection(SCHEDULE)
                    .document(date)
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

    fun deleteInSchedule(
        context: Context, absEvent: AbstractEvent, onDeleted: (AbstractEvent) -> Unit
    ) {
        val usersUID = getUserUID()
        getRefAndDate(absEvent) { ref, date ->
            val collectionRef =
                getDB().collection(USERS).document(usersUID).collection(SCHEDULE)
                    .document(date).collection(ref)

            collectionRef.document(absEvent.uid)
                .delete()
                .addOnSuccessListener {
                    onDeleted(absEvent)
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    showToast(context, R.string.try_later)
                }
        }
    }


    suspend fun loadScheduleByDate(
        context: Context, date: String
    ): List<AbstractEvent> = withContext(Dispatchers.IO) {
        val dayList = ArrayList<AbstractEvent>()
        val documentSnapshot: DocumentSnapshot
        try {
            documentSnapshot = getDB().collection(USERS)
                .document(getUserUID())
                .collection(SCHEDULE)
                .document(date)
                .get()
                .await()
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            System.err.println("${e.message}\n${e.printStackTrace()}")
            return@withContext emptyList()
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

        return@withContext dayList.sortedWith(EventComparator())
    }

    fun loadAllSchedule(context: Context, callback: (List<EventsResponse>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val scheduleRef = getDB().collection(USERS)
                .document(getUserUID())
                .collection(SCHEDULE)
                .get()
                .await()

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


    // MOVEMENTS
    fun saveMovement(context: Context, move: Movement, callback: (Movement) -> Unit) {
        val usersUID = getUserUID()
        val collectionRef = getDB().collection(USERS).document(usersUID).collection(MOVEMENTS)
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

    fun updateMovement(
        context: Context, movement: Movement, onUpdated: (Movement?) -> Unit
    ) {
        val usersUID = getUserUID()
        val date = movement.date
        val collectionRef = getDB().collection(USERS).document(usersUID).collection(MOVEMENTS)
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

    suspend fun loadActualMonthMovements(
        context: Context,
        currentMonth: String
    ): MonthMovementsResponse = withContext(Dispatchers.IO) {
        val incomesList = ArrayList<Movement>()
        val expensesList = ArrayList<Movement>()

        val documentSnapshot = getDB().collection(USERS)
            .document(getUserUID())
            .collection(MOVEMENTS).document(currentMonth).get().await()

        val incomesDeferred = async { documentSnapshot.reference.collection(INCOMES).get().await() }
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

        return@withContext MonthMovementsResponse(currentMonth, incomesList, expensesList)
    }

    fun loadAllMovements(context: Context, callback: (List<MonthMovementsResponse>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val movementsRef = getDB().collection(USERS)
                .document(getUserUID())
                .collection(MOVEMENTS)
                .get()
                .await()

            val deferredList = mutableListOf<Deferred<MonthMovementsResponse?>>()

            for (doc in movementsRef) {

                val deferred = async {
                    val response =  loadActualMonthMovements(context, doc.id)
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

    fun deleteMovement(context: Context, movement: Movement, onDeleted: (Movement) -> Unit) {
        val usersUID = getUserUID()
        val collectionRef = getDB().collection(USERS).document(usersUID).collection(MOVEMENTS)
            .document(getMonthYearOf(movement.date))

        val collectionType = when (movement.type) {
            Type.INCOME -> {
                collectionRef.collection(INCOMES)
            }
            Type.EXPENSE -> collectionRef.collection(EXPENSES)
        }
        collectionType.document(movement.uid).delete()
            .addOnSuccessListener {
                onDeleted(movement)
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
                showToast(context, R.string.try_later)
            }
    }

    // PASSWORDS
    fun deletePasswordsGroup(context: Context, item: GroupPasswordsResponse) {

    }
}