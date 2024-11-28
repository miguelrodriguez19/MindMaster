package com.miguelrodriguez19.mindmaster.view.adapters.passwordVault

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellAccountBinding
import com.miguelrodriguez19.mindmaster.model.comparators.AccountComparator
import com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault.Account
import com.miguelrodriguez19.mindmaster.model.structures.enums.AccountType
import com.miguelrodriguez19.mindmaster.model.utils.diffUtils.AccountDiffCallback

class AccountAdapter(
    private val activity: Activity, private val data: ArrayList<Account>
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun removeAt(position: Int) {
        data.removeAt(position)
        notifyItemRangeRemoved(position, 1)
    }

    fun getItemAt(index: Int): Account {
        return data[index]
    }

    fun setData(newData: List<Account>) {
        val diffCallback = AccountDiffCallback(this.data, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.data.clear()
        this.data.addAll(newData.sortedWith(AccountComparator()))
        diffResult.dispatchUpdatesTo(this)
    }


    fun foundAndUpdateIt(account: Account) {
        var index = 0
        data.stream()
            .filter { it.uid == account.uid }
            .findFirst()
            .ifPresent {
                index = data.indexOf(it)
                data[index] = account
            }
        notifyItemChanged(index, account)
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellAccountBinding.bind(v)

        //private val tilTitleAccount = bind.tilTitleAccount
        private val etTitleAccount = bind.etTitleAccount
        private val tvTypeAccount = bind.tvTypeAccount
        private val llUsername = bind.llUsername
        //private val tilUsername = bind.tilUsername
        private val etUsername = bind.etUsername
        private val btnCopyUsername = bind.btnCopyUsername
        private val llEmail = bind.llEmail
        //private val tilEmail = bind.tilEmail
        private val etEmail = bind.etEmail
        private val btnCopyEmail = bind.btnCopyEmail
        private val llPassword = bind.llPassword
        //private val tilPassword = bind.tilPassword
        private val etPassword = bind.etPassword
        private val btnCopyPassword = bind.btnCopyPassword
        private val etDescription = bind.etDescription
        private val tilDescription = bind.tilDescription

        fun bind(item: Account) {
            initWidgets(item)

            btnCopyUsername.setOnClickListener {
                copyToClipboardAndShowToast(
                    activity, "Username", etUsername.text ?: "",
                    activity.resources.getString(R.string.username_copied)
                )
            }

            btnCopyEmail.setOnClickListener {
                copyToClipboardAndShowToast(
                    activity, "Email", etEmail.text ?: "",
                    activity.resources.getString(R.string.email_copied)
                )
            }

            btnCopyPassword.setOnClickListener {
                copyToClipboardAndShowToast(
                    activity, "Password", etPassword.text ?: "",
                    activity.resources.getString(R.string.password_copied)
                )
            }
        }

        private fun copyToClipboardAndShowToast(
            context: Context, label: String, text: CharSequence, toastMessage: String
        ) {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(data)
            Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
        }

        private fun initWidgets(item: Account) {
            etTitleAccount.text = item.name
            when (item.type) {
                AccountType.GOOGLE -> configureGoogleAccount(item)
                else -> configureOtherAccount(item)
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

        private fun configureGoogleAccount(item: Account) {
            tvTypeAccount.apply {
                visibility = View.VISIBLE
                text = activity.getString(R.string.google_sign_up_type)
            }
            llUsername.visibility = View.GONE
            etEmail.setText(item.email!!)
            llPassword.visibility = View.GONE

            setOrHideDescription(item.description)
        }

        private fun configureOtherAccount(item: Account) {
            setTextOrHide(etUsername, llUsername, item.username)
            setTextOrHide(etEmail, llEmail, item.email)
            etPassword.setText(item.password!!)

            setOrHideDescription(item.description)
        }

        private fun setTextOrHide(editText: EditText, layout: View, text: String?) {
            if (!text.isNullOrBlank()) editText.setText(text)
            else layout.visibility = View.GONE
        }

        private fun setOrHideDescription(description: String?) {
            if (!description.isNullOrBlank()) etDescription.setText(description)
            else tilDescription.visibility = View.GONE
        }

    }
}