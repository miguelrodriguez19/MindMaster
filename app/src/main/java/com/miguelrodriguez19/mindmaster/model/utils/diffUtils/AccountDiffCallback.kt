package com.miguelrodriguez19.mindmaster.model.utils.diffUtils

import androidx.recyclerview.widget.DiffUtil
import com.miguelrodriguez19.mindmaster.model.structures.dto.PasswordGroupResponse.Account

class AccountDiffCallback(
    private val oldList: List<Account>,
    private val newList: List<Account>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Aquí puedes comparar los identificadores únicos de los elementos (por ejemplo, ID).
        return oldList[oldItemPosition].uid == newList[newItemPosition].uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Aquí comparas si el contenido de los elementos es el mismo.
        // Esta comprobación es más detallada que `areItemsTheSame`.
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    // Si también necesitas información detallada sobre los cambios de contenido,
    // puedes sobrescribir getChangePayload().
}
