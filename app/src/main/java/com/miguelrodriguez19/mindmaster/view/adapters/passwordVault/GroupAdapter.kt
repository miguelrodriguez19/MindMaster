package com.miguelrodriguez19.mindmaster.view.adapters.passwordVault

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellAllPasswordsGroupsBinding
import com.miguelrodriguez19.mindmaster.model.comparators.AccountsGroupsComparator
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.structures.dto.PasswordGroupResponse
import com.miguelrodriguez19.mindmaster.model.structures.dto.PasswordGroupResponse.*
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit
import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet
import com.miguelrodriguez19.mindmaster.view.bottomSheets.PasswordGroupBS
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs

@SuppressLint("NotifyDataSetChanged")
class GroupAdapter(private val context: Context, var data: ArrayList<PasswordGroupResponse>) :
    RecyclerView.Adapter<GroupAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_all_passwords_groups, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun setData(newData: List<PasswordGroupResponse>) {
        this.data.clear()
        this.data.addAll(newData)
        notifyItemRangeChanged(0, data.size)
    }

    private fun removeGroupAt(index: Int) {
        data.removeAt(index)
        notifyItemRemoved(index)
    }

    private fun getGroupOf(group: PasswordGroupResponse): Int {
        var index = -1
        data.stream()
            .filter { it.uid == group.uid }
            .findFirst()
            .ifPresent {
                index = data.indexOf(it)
            }
        return index
    }

    fun addItem(group: PasswordGroupResponse) {
        this.data.add(group)
        sortData()
        notifyDataSetChanged()
    }

    fun updateItem(group: PasswordGroupResponse) {
        val index = getGroupOf(group)
        if (index != -1) {
            this.data[index] = group
            notifyItemChanged(index)
        } else {
            this.data.add(group)
            sortData()
            notifyDataSetChanged()
        }
    }

    private fun sortData() {
        data = ArrayList(data.sortedWith(AccountsGroupsComparator()))
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellAllPasswordsGroupsBinding.bind(v)
        private val btnGroupTitle = bind.btnGroupTitle
        private val btnMoreOptions = bind.btnMoreOptions
        private val rvAccounts = bind.rvAccounts
        private lateinit var adapter: AccountAdapter
        private lateinit var menu: PopupMenu
        fun bind(item: PasswordGroupResponse) {
            initRecyclerView(item.accountsList)
            btnGroupTitle.text = item.name
            btnGroupTitle.setOnClickListener {
                if (rvAccounts.visibility == View.GONE) {
                    rvAccounts.visibility = View.VISIBLE
                } else {
                    rvAccounts.visibility = View.GONE
                }
            }
            btnMoreOptions.setOnClickListener { view ->
                menu = PopupMenu(context, view)
                menu.menuInflater.inflate(R.menu.passwords_group_context_menu, menu.menu)
                menu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.edit_group -> {
                            val passwordGroupBS = CustomBottomSheet.get<PasswordGroupResponse>(
                                PasswordGroupBS::class.java.name
                            )

                            passwordGroupBS?.showViewDetailBS(context, item) { group ->
                                updateItem(group)
                            }

                            true
                        }

                        R.id.delete_group -> {
                            AllDialogs.showConfirmationDialog(
                                context,
                                context.getString(R.string.delete_confirmation),
                                context.getString(R.string.delete_password_group_message)
                            ) {
                                if (it) {
                                    FirestoreManagerFacade.deleteGroup(item) { group ->
                                        Toolkit.showUndoSnackBar(context, view) { ok ->
                                            if (ok) {
                                                FirestoreManagerFacade.saveGroup(
                                                    group
                                                ) { newGroup ->
                                                    addItem(newGroup)
                                                }
                                            }
                                        }
                                        removeGroupAt(adapterPosition)
                                    }
                                } else {
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            true
                        }

                        else -> false
                    }
                }
                menu.show()
            }
        }


        private fun initRecyclerView(data: List<Account>) {
            rvAccounts.layoutManager = StaggeredGridLayoutManager(1, 1)
            adapter = AccountAdapter(context, ArrayList(data))
            rvAccounts.adapter = adapter
        }

    }
}