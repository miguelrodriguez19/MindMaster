package com.miguelrodriguez19.mindmaster.models.firebase

import android.content.Context
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.models.comparators.MovementsGroupComparator
import com.miguelrodriguez19.mindmaster.models.firebase.FirebaseManager.getDB
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse.Movement
import com.miguelrodriguez19.mindmaster.models.structures.MonthMovementsResponse.Type
import com.miguelrodriguez19.mindmaster.models.utils.Preferences.getUserUID
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.getMonthYearOf
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit.showToast
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

object FMovementManager {
    private const val USERS = "users"
    private const val DATE = "date"
    private const val MOVEMENTS = "movements"
    private const val INCOMES = "incomes"
    private const val EXPENSES = "expenses"

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
                    callback(MonthMovementsResponse.Movement(documentId, move))
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
}