package com.miguelrodriguez19.mindmaster.model.utils.diffUtils

import androidx.recyclerview.widget.DiffUtil
import com.miguelrodriguez19.mindmaster.model.structures.dto.accountVault.Account

class AccountDiffCallback(
    private val oldList: List<Account>, private val newList: List<Account>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // If both UIDs are the same, the objects are equals
        return oldList[oldItemPosition].uid == newList[newItemPosition].uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.areDeepEquals(newItem)
    }


    // For detailed information on the changes see getChangePayload()
}
