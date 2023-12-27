package com.miguelrodriguez19.mindmaster.view.adapters.passwordVault

import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellFormAccountBinding
import com.miguelrodriguez19.mindmaster.model.structures.dto.PasswordGroupResponse
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.evaluatePasswordSecurity
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit.isPasswordStrong
import kotlin.random.Random

class FormAccountAdapter(
    private val context: Context,
    private val data: ArrayList<PasswordGroupResponse.Account?>
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
            Toolkit.showToast(context, R.string.at_least_one_account)
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
        private val tilPassword = bind.tilPassword
        private val etPassword = bind.etPassword
        private val btnGeneratePwd = bind.btnRandomPassword
        private val btnDelete = bind.btnDeleteAccount
        private val tilDescription = bind.tilDescription
        private val etDescription = bind.etDescription

        fun bind(item: PasswordGroupResponse.Account?) {
            if (item != null) {
                setUpData(item)
            }

            etPassword.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val pair = evaluatePasswordSecurity(s.toString())
                    tilPassword.helperText = context.getString(pair.first)
                    tilPassword.setHelperTextColor(ColorStateList.valueOf(context.getColor(pair.second)))
                    tilPassword.boxStrokeColor = context.getColor(pair.second)
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            tgTypeSignIn.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when (checkedId) {
                    R.id.btn_typeEmail -> {
                        checkVisibility(PasswordGroupResponse.Type.EMAIL)
                    }
                    R.id.btn_typeGoogle -> {
                        checkVisibility(PasswordGroupResponse.Type.GOOGLE)
                    }
                    R.id.btn_typeOther -> {
                        checkVisibility(PasswordGroupResponse.Type.OTHER)
                    }
                }
            }

            btnDelete.setOnClickListener {
                deleteItemAt(adapterPosition)
            }

            btnGeneratePwd.setOnClickListener {
                etPassword.setText(generateSafePassword())
            }
        }

        private fun generateSafePassword(): String {
            val length = context.getString(R.string.secure_pwd_lenght).toInt()
            val chars = context.getString(R.string.secure_pwd_characters).toList().shuffled()
            var safePwd: String
            do {
                safePwd = (1..length)
                    .map { Random.nextInt(0, chars.size) }
                    .map(chars::get)
                    .joinToString("")
            } while (!isPasswordStrong(safePwd))
            return safePwd
        }

        private fun setUpData(item: PasswordGroupResponse.Account) {
            etTitleAccount.setText(item.name)
            etUsername.setText(item.username)
            etEmail.setText(item.email)
            etPassword.setText(item.password)
            etDescription.setText(item.description)
            when (item.type) {
                PasswordGroupResponse.Type.EMAIL -> {
                    tgTypeSignIn.check(R.id.btn_typeEmail)
                    checkVisibility(PasswordGroupResponse.Type.EMAIL)
                }
                PasswordGroupResponse.Type.GOOGLE -> {
                    tgTypeSignIn.check(R.id.btn_typeGoogle)
                    checkVisibility(PasswordGroupResponse.Type.GOOGLE)
                }
                PasswordGroupResponse.Type.OTHER -> {
                    tgTypeSignIn.check(R.id.btn_typeOther)
                    checkVisibility(PasswordGroupResponse.Type.OTHER)
                }
            }
        }

        private fun checkVisibility(type: PasswordGroupResponse.Type) {
            when (type) {
                PasswordGroupResponse.Type.EMAIL -> {
                    changeVisibility(arrayOf(tilUsername, tilPassword, tilEmail), arrayOf())
                    btnGeneratePwd.visibility = View.VISIBLE
                }
                PasswordGroupResponse.Type.GOOGLE -> {
                    changeVisibility(arrayOf(tilEmail), arrayOf(tilPassword, tilUsername))
                    btnGeneratePwd.visibility = View.GONE
                }
                PasswordGroupResponse.Type.OTHER -> {
                    changeVisibility(arrayOf(tilUsername, tilPassword), arrayOf())
                    btnGeneratePwd.visibility = View.VISIBLE
                }
            }
        }

        private fun changeVisibility(
            show: Array<TextInputLayout>,
            dismiss: Array<TextInputLayout>
        ) {
            for (til in show) {
                til.visibility = View.VISIBLE
                til.error = null
            }
            for (til in dismiss) {
                til.visibility = View.GONE
                til.error = null
                til.editText?.text = null
            }
        }
    }
}