package com.miguelrodriguez19.mindmaster.views.passwords.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellFormAccountBinding
import com.miguelrodriguez19.mindmaster.models.structures.GroupPasswordsResponse
import com.miguelrodriguez19.mindmaster.models.utils.Toolkit

class FormAccountAdapter(
    private val context: Context,
    private val accountsGroup: ArrayList<GroupPasswordsResponse.Account?>
) : RecyclerView.Adapter<FormAccountAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cell_form_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(accountsGroup[position])
    }

    override fun getItemCount(): Int = accountsGroup.size

    fun addNewForm() {
        accountsGroup.add(null)
        notifyDataSetChanged()
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
        private val tilDescription = bind.tilDescription
        private val etDescription = bind.etDescription

        fun bind(item: GroupPasswordsResponse.Account?) {
            if (item != null) {
                setUpData(item)
            }

            tgTypeSignIn.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when (checkedId) {
                    R.id.btn_typeEmail -> {
                        checkVisibility(GroupPasswordsResponse.Type.EMAIL)
                    }
                    R.id.btn_typeGoogle -> {
                        checkVisibility(GroupPasswordsResponse.Type.GOOGLE)
                    }
                    R.id.btn_typeOther -> {
                        checkVisibility(GroupPasswordsResponse.Type.OTHER)
                    }
                }
            }
            btnGeneratePwd.setOnClickListener {
                etPassword.setText(generateSecurePwd())
            }
        }

        private fun generateSecurePwd(): String {
            val length = 12
            val characters =
                "ABCDEFGHIJKLMNÑOPQRSTUVWXYZabcdefghijklmnñopqrstuvwxyz0123456789@#$%/;:.,+_"
            var sb = ""
            while (!sb.matches(Toolkit.PASSWORD_PATTERN.toRegex())) {
                sb = ""
                val charMix = characters.toList().shuffled().joinToString("")
                for (i in 0 until length) {
                    val randomIndex = (characters.indices).random()
                    sb += charMix[randomIndex]
                }
            }

            return sb
        }

        private fun setUpData(item: GroupPasswordsResponse.Account) {
            etTitleAccount.setText(item.name)
            etUsername.setText(item.username)
            etEmail.setText(item.email)
            etPassword.setText(item.password)
            etDescription.setText(item.description)
            when (item.type) {
                GroupPasswordsResponse.Type.EMAIL -> {
                    tgTypeSignIn.check(R.id.btn_typeEmail)
                    checkVisibility(GroupPasswordsResponse.Type.EMAIL)
                }
                GroupPasswordsResponse.Type.GOOGLE -> {
                    tgTypeSignIn.check(R.id.btn_typeGoogle)
                    checkVisibility(GroupPasswordsResponse.Type.GOOGLE)
                }
                GroupPasswordsResponse.Type.OTHER -> {
                    tgTypeSignIn.check(R.id.btn_typeOther)
                    checkVisibility(GroupPasswordsResponse.Type.OTHER)
                }
            }
        }

        private fun checkVisibility(type: GroupPasswordsResponse.Type) {
            when (type) {
                GroupPasswordsResponse.Type.EMAIL -> {
                    changeVisibility(arrayOf(tilUsername, tilPassword, tilEmail), arrayOf())
                    btnGeneratePwd.visibility = View.VISIBLE
                }
                GroupPasswordsResponse.Type.GOOGLE -> {
                    changeVisibility(arrayOf(tilEmail), arrayOf(tilPassword, tilUsername))
                    btnGeneratePwd.visibility = View.GONE
                }
                GroupPasswordsResponse.Type.OTHER -> {
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