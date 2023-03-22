package com.miguelrodriguez19.mindmaster.passwords

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellAllPasswordsGroupsBinding
import com.miguelrodriguez19.mindmaster.expenses.LastMovementsAdapter
import com.miguelrodriguez19.mindmaster.models.GroupPasswordsResponse
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse


class GroupAdapter(private val context: Context, var data: ArrayList<GroupPasswordsResponse>) :
    RecyclerView.Adapter<GroupAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_all_passwords_groups, parent, false)
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

        fun bind(item: GroupPasswordsResponse) {
            initRecyclerView(item.accountsList)
            btnGroupTitle.text = item.name
            btnGroupTitle.setOnClickListener {
                if (rvAccounts.visibility == View.GONE){
                    rvAccounts.visibility = View.VISIBLE
                }else{
                    rvAccounts.visibility = View.GONE
                }
            }
        }

        private fun initRecyclerView(data : List<GroupPasswordsResponse.Account>) {
            val dataArrList = ArrayList<GroupPasswordsResponse.Account>(data)
            val mLayoutManager = StaggeredGridLayoutManager(1, 1)
            rvAccounts.layoutManager = mLayoutManager

            adapter = AccountAdapter(context, dataArrList)

            rvAccounts.adapter = adapter
        }

    }
}