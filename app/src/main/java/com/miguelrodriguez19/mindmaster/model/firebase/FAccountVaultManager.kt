package com.miguelrodriguez19.mindmaster.model.firebase

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.model.comparators.AccountsGroupsComparator
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade.getDB
import com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault.PasswordGroupResponse
import com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault.Account
import com.miguelrodriguez19.mindmaster.model.utils.AESEncripter
import com.miguelrodriguez19.mindmaster.model.utils.Preferences.getUserUID
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.showToast
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

object FAccountVaultManager {
    private const val USERS = "users"
    private const val GROUPS = "groups"
    private const val ACCOUNTS = "accounts"

    fun saveGroup(
        context: Context, group: PasswordGroupResponse, onSuccess: (PasswordGroupResponse) -> Unit
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
        context: Context, group: PasswordGroupResponse, onUpdated: (PasswordGroupResponse) -> Unit
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

    private fun deleteOldAccounts(group: PasswordGroupResponse) {
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
    ): PasswordGroupResponse = withContext(Dispatchers.IO) {
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
        return@withContext PasswordGroupResponse(groupUID, groupName, accountList)
    }

    fun loadAllGroups(
        context: Context, onSuccess: (List<PasswordGroupResponse>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val userUID = getUserUID()
            if (userUID != null) {
                val movementsRef =
                    getDB().collection(USERS).document(userUID).collection(GROUPS).get().await()

                val deferredList = mutableListOf<Deferred<PasswordGroupResponse?>>()

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
        context: Context, group: PasswordGroupResponse, onDeleted: (PasswordGroupResponse) -> Unit
    ) {
        val userUID = getUserUID()
        if (!userUID.isNullOrBlank() && group.uid.isNotBlank()) {
            getDB().collection(USERS).document(userUID).collection(GROUPS).document(group.uid)
                .delete().addOnSuccessListener {
                    onDeleted(group)
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                    showToast(context, R.string.try_later)
                }
        }
    }
}