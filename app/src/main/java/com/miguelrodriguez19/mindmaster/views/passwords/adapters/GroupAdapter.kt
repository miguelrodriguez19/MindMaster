package com.miguelrodriguez19.mindmaster.views.passwords.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellAllPasswordsGroupsBinding
import com.miguelrodriguez19.mindmaster.models.structures.GroupPasswordsResponse
import com.miguelrodriguez19.mindmaster.models.utils.AllBottomSheets
import com.miguelrodriguez19.mindmaster.models.utils.AllDialogs
import com.miguelrodriguez19.mindmaster.models.utils.FirebaseManager


class GroupAdapter(private val context: Context, var data: ArrayList<GroupPasswordsResponse>) :
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
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellAllPasswordsGroupsBinding.bind(v)
        private val btnGroupTitle = bind.btnGroupTitle
        private val btnMoreOptions = bind.btnMoreOptions
        private val rvAccounts = bind.rvAccounts
        private lateinit var adapter: AccountAdapter
        private lateinit var menu: PopupMenu
        fun bind(item: GroupPasswordsResponse) {
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
                            AllBottomSheets.showPasswordsBS(context, item){
                                //TODO()
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
                                    FirebaseManager.deleteGroup(context, item){

                                    }
                                }
                                data.removeAt(adapterPosition)
                                notifyItemRangeRemoved(adapterPosition, 1)
                            }
                            true
                        }
                        else -> false
                    }
                }
                menu.show()
            }
        }


        private fun initRecyclerView(data: List<GroupPasswordsResponse.Account>) {
            val dataArrList = ArrayList<GroupPasswordsResponse.Account>(data)
            val mLayoutManager = StaggeredGridLayoutManager(1, 1)
            rvAccounts.layoutManager = mLayoutManager

            adapter = AccountAdapter(context, dataArrList)

            rvAccounts.adapter = adapter
        }

    }
}