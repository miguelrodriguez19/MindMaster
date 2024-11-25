package com.miguelrodriguez19.mindmaster.view.adapters.passwordVault

import android.app.Activity
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellFormAccountBinding
import com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault.Account
import com.miguelrodriguez19.mindmaster.model.structures.enums.AccountType
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.evaluatePasswordSecurity
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.generateSafePassword

class FormAccountAdapter(
    private val activity: Activity,
    private val data: ArrayList<Account?>
) : RecyclerView.Adapter<FormAccountAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cell_form_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun addNewForm() {
        data.add(null)
        notifyItemInserted(data.size - 1)
    }

    private fun deleteItemAt(index: Int) {
        if (data.size > 1) {
            data.removeAt(index)
            notifyItemRemoved(index)
        } else {
            Toolkit.showToast(activity, R.string.at_least_one_account)
        }
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellFormAccountBinding.bind(v)
        private val tilTitleAccount = bind.tilTitleAccount
        private val etTitleAccount = bind.etTitleAccount
        private val tgTypeSignIn = bind.toggleTypeSignIn
        private val tilUsername = bind.tilUsername
        private val etUsername = bind.etUsername
        private val tilEmail = bind.tilEmail
        private val etEmail = bind.etEmail
        private val llPassword = bind.llPassword
        private val tilPassword = bind.tilPassword
        private val etPassword = bind.etPassword
        private val btnGeneratePwd = bind.btnRandomPassword
        private val btnDelete = bind.btnDeleteAccount
        private val tilDescription = bind.tilDescription
        private val etDescription = bind.etDescription

        fun bind(item: Account?) {
            if (item != null) {
                setUpData(item)
            }

            etPassword.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val pair = evaluatePasswordSecurity(s.toString())
                    tilPassword.helperText = activity.getString(pair.first)
                    tilPassword.setHelperTextColor(ColorStateList.valueOf(activity.getColor(pair.second)))
                    tilPassword.boxStrokeColor = activity.getColor(pair.second)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            tgTypeSignIn.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {
                        R.id.btn_typeEmail -> updateView(AccountType.EMAIL)
                        R.id.btn_typeGoogle -> updateView(AccountType.GOOGLE)
                        R.id.btn_typeOther -> updateView(AccountType.OTHER)
                    }
                }
            }

            btnDelete.setOnClickListener {
                deleteItemAt(adapterPosition)
            }

            btnGeneratePwd.setOnClickListener {
                etPassword.setText(generateSafePassword(activity))
            }

            etDescription.apply {
                setOnTouchListener { v, event ->
                    if (v.id == R.id.et_description) {
                        v.parent.requestDisallowInterceptTouchEvent(true)
                        when (event.action and MotionEvent.ACTION_MASK) {
                            MotionEvent.ACTION_UP -> {
                                v.parent.requestDisallowInterceptTouchEvent(false)
                                v.performClick()
                            }
                        }
                    }
                    false
                }
                setOnClickListener { }
            }

        }

        private fun updateView(type: AccountType) {
            checkVisibility(type)
            // We ensure that the state view is actualized
            tgTypeSignIn.requestLayout()
            tgTypeSignIn.invalidate()
        }

        private fun setUpData(item: Account) {
            etTitleAccount.setText(item.name)
            etUsername.setText(item.username)
            etEmail.setText(item.email)
            etPassword.setText(item.password)
            etDescription.setText(item.description)
            when (item.type) {
                AccountType.EMAIL -> {
                    tgTypeSignIn.check(R.id.btn_typeEmail)
                    checkVisibility(AccountType.EMAIL)
                }
                AccountType.GOOGLE -> {
                    tgTypeSignIn.check(R.id.btn_typeGoogle)
                    checkVisibility(AccountType.GOOGLE)
                }
                AccountType.OTHER -> {
                    tgTypeSignIn.check(R.id.btn_typeOther)
                    checkVisibility(AccountType.OTHER)
                }
            }
        }

        private fun checkVisibility(type: AccountType) {
            resetAllFields() // Show all

            when (type) {
                AccountType.EMAIL -> {
                    changeVisibility(listOf(tilUsername))
                }
                AccountType.GOOGLE -> {
                    changeVisibility(listOf(tilUsername, llPassword))
                }
                AccountType.OTHER -> { /* Nothing to do, shows all */
                }
            }
        }

        private fun resetAllFields() {
            llPassword.visibility = View.VISIBLE
            listOf(tilUsername, tilPassword, tilEmail).forEach { til ->
                til.visibility = View.VISIBLE
                til.error = null
                til.editText?.text = null
            }
        }

        private fun changeVisibility(hide: List<View>) {
            hide.forEach { field ->
                field.visibility = View.GONE
            }
        }
    }
}