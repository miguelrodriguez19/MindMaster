package com.miguelrodriguez19.mindmaster.models.firebase

import android.content.Context
import com.google.android.gms.stats.CodePackage.REMINDERS
import com.google.firebase.firestore.DocumentSnapshot
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.models.comparators.EventComparator
import com.miguelrodriguez19.mindmaster.models.comparators.EventGroupComparator
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager.getDB
import com.miguelrodriguez19.mindmaster.models.structures.*
import com.miguelrodriguez19.mindmaster.models.utils.Preferences.getUserUID
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.showToast
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

object FScheduleManager {

    private const val USERS = "users"
    private const val SCHEDULE = "schedule"
    private const val EVENTS = "events"
    private const val REMINDERS = "reminders"
    private const val TASKS = "tasks"
    private const val DATE = "date"


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

}