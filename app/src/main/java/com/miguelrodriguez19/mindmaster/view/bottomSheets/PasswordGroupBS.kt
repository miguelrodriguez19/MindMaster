package com.miguelrodriguez19.mindmaster.view.bottomSheets

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.setPeekHeight
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.BottomSheetPasswordBinding
import com.miguelrodriguez19.mindmaster.databinding.CellFormAccountBinding
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault.Account
import com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault.PasswordGroupResponse
import com.miguelrodriguez19.mindmaster.model.structures.enums.AccountType
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit
import com.miguelrodriguez19.mindmaster.view.adapters.passwordVault.FormAccountAdapter
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs
import java.time.LocalDate

class PasswordGroupBS : CustomBottomSheet<PasswordGroupResponse>() {
    private lateinit var bind: BottomSheetPasswordBinding

    override fun showViewDetailBS(
        activity: Activity,
        obj: PasswordGroupResponse?,
        date: LocalDate,
        callback: (PasswordGroupResponse) -> Unit
    ) {
        MaterialDialog(activity, BottomSheet(LayoutMode.MATCH_PARENT)).show {
            customView(
                R.layout.bottom_sheet_password, scrollable = true, horizontalPadding = true
            )
            cornerRadius(res = R.dimen.corner_radius_bottom_sheets)
            setPeekHeight(Toolkit.getPeekHeight(activity, 0.95f))

            bind = BottomSheetPasswordBinding.bind(getCustomView())

            bind.etTitleGroup.setText(obj?.name)
            val adapter = FormAccountAdapter(
                activity,
                obj?.accountsList?.let { ArrayList(it) } ?: arrayListOf(null))
            bind.rvAccountsBS.adapter = adapter
            bind.rvAccountsBS.layoutManager = StaggeredGridLayoutManager(1, 1)

            bind.btnAddNewAccForm.setOnClickListener {
                adapter.addNewForm()
            }

            bind.btnClose.setOnClickListener {
                AllDialogs.showConfirmationDialog(
                    activity,
                    activity.getString(R.string.changes_will_lost),
                    activity.getString(R.string.confirm_lost_changes_message)
                ) {
                    if (it) {
                        dismiss()
                    }
                }
            }

            bind.efabSave.setOnClickListener {
                if (checkPwdBSFields(activity, bind.rvAccountsBS)) {
                        if (obj == null) {
                            FirestoreManagerFacade.saveGroup(
                                PasswordGroupResponse(
                                    bind.etTitleGroup.text.toString(),
                                    getAccounts(bind.rvAccountsBS)
                                )
                            ) {
                                callback(it)
                            }
                        } else {
                            FirestoreManagerFacade.updateGroup(
                                obj.copy(
                                    name = bind.etTitleGroup.text.toString(),
                                    accountsList = getAccounts(bind.rvAccountsBS)
                                )
                            ) {
                                callback(it)
                            }
                        }
                        dismiss()
                    }
            }
        }
    }

        private fun getAccounts(rv: RecyclerView): List<Account> {
            val adapter = rv.adapter
            val accountsList = ArrayList<Account>()
            if (adapter != null) {
                for (i in 0 until adapter.itemCount) {
                    val view = rv.getChildAt(i)
                    val acc = collectDataFromView(view)
                    accountsList.add(acc)
                }
            }
            return accountsList
        }

        private fun collectDataFromView(view: View): Account {
            val bind = CellFormAccountBinding.bind(view)
            val title = bind.etTitleAccount.text.toString()
            val username = bind.etUsername.text.toString()
            val email = bind.etEmail.text.toString()
            val password = bind.etPassword.text.toString()
            val description = bind.etDescription.text.toString()
            val type = when (bind.toggleTypeSignIn.checkedButtonId) {
                R.id.btn_typeEmail -> AccountType.EMAIL
                R.id.btn_typeGoogle -> AccountType.GOOGLE
                R.id.btn_typeOther -> AccountType.OTHER
                else -> AccountType.OTHER
            }

            return Account(
                title,
                username,
                email,
                password,
                description,
                type
            )
        }

    private fun checkPwdBSFields(context: Context, rv: RecyclerView): Boolean {
        val adapter = rv.adapter
        var flag = false
        if (adapter != null) {
            for (i in 0 until adapter.itemCount) {
                val view = rv.getChildAt(i)
                val bind = CellFormAccountBinding.bind(view)

                flag = when (bind.toggleTypeSignIn.checkedButtonId) {
                    R.id.btn_typeEmail -> {
                        checkObligatoryFields(listOf(bind.tilEmail, bind.tilPassword), context)
                    }

                    R.id.btn_typeGoogle -> {
                        checkObligatoryFields(listOf(bind.tilEmail), context)
                    }

                    else -> {
                        true
                    }
                }
            }
        }
        return flag
    }

        override fun fillData(context: Context, obj: PasswordGroupResponse) {
            // Does nothing, rejects the inheritance
        }
    }