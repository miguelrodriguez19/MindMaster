package com.miguelrodriguez19.mindmaster.model.firebase

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.comparators.EventComparator
import com.miguelrodriguez19.mindmaster.model.comparators.EventGroupComparator
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade.getDB
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Event
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.EventsResponse
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Reminder
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Task
import com.miguelrodriguez19.mindmaster.model.structures.enums.schedule.ActivityType
import com.miguelrodriguez19.mindmaster.model.utils.Preferences.getUserUID
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FScheduleManager {

    private const val USERS = "users"
    private const val SCHEDULE = "schedule"
    private const val EVENTS = "events"
    private const val REMINDERS = "reminders"
    private const val TASKS = "tasks"
    private const val DATE = "date"


    private fun getRefAndDate(absActivity: AbstractActivity, callback: (String, String) -> Unit) {

        val date = AbstractActivity.getFormattedDateOf(absActivity)
        val ref = when (absActivity.type) {
            ActivityType.EVENT -> EVENTS
            ActivityType.REMINDER -> REMINDERS
            ActivityType.TASK -> TASKS
        }
        callback(ref, date)
    }

    fun saveInSchedule(
        context: Context, absActivity: AbstractActivity, onAdded: (AbstractActivity) -> Unit
    ) {
        val userUID = getUserUID()
        if (userUID != null) {
            getRefAndDate(absActivity) { ref, date ->
                val schedule = getDB().collection(USERS).document(userUID).collection(SCHEDULE).document(date)
                schedule.set(mapOf(Pair(DATE, date)))

                val collection = schedule.collection(ref)
                collection.add(absActivity).addOnCompleteListener { obj ->
                    if (obj.isSuccessful) {
                        val id = obj.result.id
                        val type = when (absActivity.type) {
                            ActivityType.EVENT -> onAdded((absActivity as Event).copy(uid = id))
                            ActivityType.REMINDER -> onAdded((absActivity as Reminder).copy(uid = id))
                            ActivityType.TASK -> onAdded((absActivity as Task).copy(uid = id))
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
        context: Context, absEvent: AbstractActivity, onUpdated: (AbstractActivity?) -> Unit
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
        context: Context, absEvent: AbstractActivity, onDeleted: (AbstractActivity) -> Unit
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
    ): List<AbstractActivity> = withContext(Dispatchers.IO) {
        val userUID = getUserUID()
        val dayList = ArrayList<AbstractActivity>()
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
                    dayList.add(event.copy(uid = eventDoc.id))
                }
            }

            for (reminderDoc in reminderQuerySnapshot.documents) {
                val reminder = reminderDoc.toObject(Reminder::class.java)
                if (reminder != null) {
                    dayList.add(reminder.copy(uid = reminderDoc.id))
                }
            }

            for (taskDoc in tasksQuerySnapshot.documents) {
                val task = taskDoc.toObject(Task::class.java)
                if (task != null) {
                    dayList.add(task.copy(uid = taskDoc.id))
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