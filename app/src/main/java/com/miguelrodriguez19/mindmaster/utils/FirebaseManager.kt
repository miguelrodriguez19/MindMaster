package com.miguelrodriguez19.mindmaster.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.miguelrodriguez19.mindmaster.MainActivity
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.models.*
import com.miguelrodriguez19.mindmaster.models.comparators.DateComparator
import com.miguelrodriguez19.mindmaster.models.comparators.EventComparator
import com.miguelrodriguez19.mindmaster.utils.Preferences.getUserUID
import com.miguelrodriguez19.mindmaster.utils.Toolkit.getDateFromDatetime
import com.miguelrodriguez19.mindmaster.utils.Toolkit.showToast
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

object FirebaseManager {
    private val TAG = "FIREBASE_MANAGER"
    private val USERS = getString(R.string.users)
    private val SCHEDULE = getString(R.string.schedule)
    private val EVENTS = getString(R.string.events)
    private val REMINDER = getString(R.string.reminder)
    private val TASK = getString(R.string.tasks)
    private val DATE = getString(R.string.date)
    private val MOVEMENTS = getString(R.string.movements)

    private fun getString(@StringRes resId: Int): String {
        return MyApplication.instance.getString(resId)
    }
    fun getAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    fun getDB(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun saveImageInStorage(context: Context, imageUrl: String, callback: (String) -> Unit) {
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
                        addNewUserToFirestore(
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

    fun getUserByUID(uid: String, callback: (UserResponse?) -> Unit) {
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

    fun addNewUserToFirestore(user: UserResponse) {
        getDB().collection(USERS).document(user.uid).set(user).addOnSuccessListener { }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun saveInShedule(
        context: Context, absEvent: AbstractEvents, addedObj: (AbstractEvents?) -> Unit
    ) {
        when (absEvent.type) {
            EventType.EVENT -> saveEvent(context, absEvent as Event) {
                addedObj(it)
            }
            EventType.REMINDER -> saveReminder(context, absEvent as Reminder) {
                addedObj(it)
            }
            EventType.TASK -> saveTask(context, absEvent as Task) {
                addedObj(it)
            }
        }
    }

    private fun saveTask(context: Context, t: Task, addedObj: (Task) -> Unit) {
        val usersUID = getUserUID()

        val collectionRef =
            getDB().collection(USERS).document(usersUID).collection(SCHEDULE).document(t.due_date)
        collectionRef.set(mapOf(Pair(DATE, getDateFromDatetime(t.due_date))))

        val collectionTasks = collectionRef.collection(TASK)
        collectionTasks.add(t).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documentId = task.result.id
                addedObj(Task(documentId, t))
            } else {
                task.exception?.printStackTrace()
                showToast(context, R.string.try_later)
            }
        }
    }

    private fun saveReminder(context: Context, r: Reminder, addedObj: (Reminder) -> Unit) {
        val usersUID = getUserUID()
        val collectionRef =
            getDB().collection(USERS).document(usersUID).collection(SCHEDULE).document(r.date_time)
        collectionRef.set(mapOf(Pair(DATE, getDateFromDatetime(r.date_time))))

        val collectionReminder = collectionRef.collection(REMINDER)
        collectionReminder.add(r).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documentId = task.result.id
                addedObj(Reminder(documentId, r))
            } else {
                task.exception?.printStackTrace()
                showToast(context, R.string.try_later)
            }
        }
    }

    private fun saveEvent(context: Context, e: Event, addedObj: (Event) -> Unit) {
        val usersUID = getUserUID()
        val collectionRef = getDB().collection(USERS).document(usersUID).collection(SCHEDULE)
            .document(getDateFromDatetime(e.start_time))

        collectionRef.set(mapOf(Pair(DATE, getDateFromDatetime(e.start_time))))
        val collectionEvents = collectionRef.collection(EVENTS)
        collectionEvents.add(e).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documentId = task.result.id
                addedObj(Event(documentId, e))
            } else {
                task.exception?.printStackTrace()
                showToast(context, R.string.try_later)
            }
        }
    }

    suspend fun loadScheduleByDate(
        context: Context,
        date: String
    ): List<AbstractEvents> = withContext(Dispatchers.IO) {
        val dayList = ArrayList<AbstractEvents>()
        val documentSnapshot = getDB().collection(USERS)
            .document(getUserUID())
            .collection(SCHEDULE)
            .document(date)
            .get()
            .await()

        val eventsDeferred =
            async { documentSnapshot.reference.collection(EVENTS).get().await() }
        val reminderDeferred =
            async { documentSnapshot.reference.collection(REMINDER).get().await() }
        val tasksDeferred = async { documentSnapshot.reference.collection(TASK).get().await() }

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

            val deferredList = mutableListOf<Deferred<EventsResponse>>()

            for (doc in scheduleRef) {
                Log.i(TAG, "Document ID: ${doc.id}")
                val deferred = async {
                    val dayList = loadScheduleByDate(context, doc.id)
                    EventsResponse(doc.id, dayList)
                }
                deferredList.add(deferred)
            }

            val allEvents = deferredList.awaitAll()

            withContext(Dispatchers.Main) {
                callback(allEvents.sortedWith(DateComparator()))
            }
        }
    }

    fun saveMovement(context: Context, m:MonthMovementsResponse){
        val query = getDB().collection(USERS)
            .document(getUserUID())
            .collection(MOVEMENTS)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            val movementsList = mutableListOf<MonthMovementsResponse.Movement>()
            for (doc in snapshot?.documents.orEmpty()) {
                val movement = doc.toObject(MonthMovementsResponse.Movement::class.java)
                movement?.let { movementsList.add(it) }
            }

            val monthMovementsResponse = MonthMovementsResponse("codMonthMovement", "", movementsList)
            // Hacer algo con la lista de los últimos 5 movimientos
        }

    }

    fun deletePasswordsGroup(context: Context, item: GroupPasswordsResponse) {

    }
}