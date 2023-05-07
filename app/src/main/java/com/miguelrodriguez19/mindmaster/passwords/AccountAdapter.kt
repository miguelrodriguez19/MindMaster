package com.miguelrodriguez19.mindmaster.passwords

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellAccountBinding
import com.miguelrodriguez19.mindmaster.models.GroupPasswordsResponse
import com.miguelrodriguez19.mindmaster.models.GroupPasswordsResponse.Type

class AccountAdapter(
    private val context: Context, private val data: ArrayList<GroupPasswordsResponse.Account>
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellAccountBinding.bind(v)
        //private val tilTitleAccount = bind.tilTitleAccount
        private val etTitleAccount = bind.etTitleAccount
        private val tvTypeAccount = bind.tvTypeAccount
        private val llUsername = bind.llUsername
        private val tilUsername = bind.tilUsername
        private val etUsername = bind.etUsername
        private val btnCopyUsername = bind.btnCopyUsername
        private val llEmail = bind.llEmail
        private val tilEmail = bind.tilEmail
        private val etEmail = bind.etEmail
        private val btnCopyEmail = bind.btnCopyEmail
        private val llPassword = bind.llPassword
        private val tilPassword = bind.tilPassword
        private val etPassword = bind.etPassword
        private val btnCopyPassword = bind.btnCopyPassword
        private val etDescription = bind.etDescription
        private val tilDescription = bind.tilDescription

        fun bind(item: GroupPasswordsResponse.Account) {
            initWidgets(item)

            btnCopyUsername.setOnClickListener {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Username", etUsername.text ?: "")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.username_copied),
                    Toast.LENGTH_SHORT
                ).show()
            }
            btnCopyEmail.setOnClickListener {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Email", etEmail.text ?: "")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    context, context.getString(R.string.email_copied), Toast.LENGTH_SHORT
                ).show()
            }
            btnCopyPassword.setOnClickListener {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", etPassword.text ?: "")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    context, context.getString(R.string.password_copied), Toast.LENGTH_SHORT
                ).show()
            }

        }

        private fun initWidgets(item: GroupPasswordsResponse.Account) {
            etTitleAccount.setText(item.name)
            when (item.type) {
                Type.GOOGLE -> {
                    tvTypeAccount.visibility = View.VISIBLE
                    tvTypeAccount.text = context.getString(R.string.google_sign_up_type)
                    llUsername.visibility = View.GONE
                    etEmail.setText(item.email!!)
                    llPassword.visibility = View.GONE
                    if (item.note != null) etDescription.setText(item.note)
                    else tilDescription.visibility = View.GONE
                }

                else -> {
                    if (item.username != null) etUsername.setText(item.username)
                    else llUsername.visibility = View.GONE

                    if (item.email != null) etEmail.setText(item.email)
                    else llEmail.visibility = View.GONE

                    etPassword.setText(item.password!!)

                    if (item.note != null) etDescription.setText(item.note)
                    else tilDescription.visibility = View.GONE
                }
            }
        }
    }
}