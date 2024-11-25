package com.miguelrodriguez19.mindmaster.view.adapters.expenses

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellMovementBinding
import com.miguelrodriguez19.mindmaster.model.comparators.MovementComparator
import com.miguelrodriguez19.mindmaster.model.structures.dto.expenses.Movement
import com.miguelrodriguez19.mindmaster.model.structures.enums.MovementType
import com.miguelrodriguez19.mindmaster.model.utils.Preferences.getCurrency
import com.miguelrodriguez19.mindmaster.view.bottomSheets.CustomBottomSheet
import com.miguelrodriguez19.mindmaster.view.bottomSheets.MovementBS

class MovementAdapter(
    private val activity: Activity,
    private val data: ArrayList<Movement>,
    val onClick: (Movement) -> Unit
) :
    RecyclerView.Adapter<MovementAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cell_movement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun removeAt(position: Int){
        data.removeAt(position)
        notifyItemRangeRemoved(position, 1)
    }

    fun getItemAt(index: Int): Movement {
        return data[index]
    }

    fun setData(newData: List<Movement>) {
        this.data.clear()
        this.data.addAll(newData.sortedWith(MovementComparator()))
        notifyDataSetChanged()
    }

    fun addItem(item: Movement) {
        this.data.add(item)
        this.data.sortedWith(MovementComparator())
        notifyDataSetChanged()
    }

    fun foundAndUpdateIt(movement: Movement) {
        var index = 0
        data.stream()
            .filter { it.uid == movement.uid }
            .findFirst()
            .ifPresent {
                index = data.indexOf(it)
                data[index] = movement
            }
        notifyItemChanged(index, movement)
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellMovementBinding.bind(v)
        private val rlMovementArea = bind.rlLatestMovementsCell
        private val tvDate = bind.tvMovementsDate
        private val tvTitle = bind.tvMovementTitle
        private val tvAmount = bind.tvMovementsAmount

        fun bind(item: Movement) {
            val mapColors = mapOf(
                "green" to ContextCompat.getColor(itemView.context, R.color.green_jade_500),
                "red" to ContextCompat.getColor(itemView.context, R.color.red_bittersweet_200),
                "black" to ContextCompat.getColor(itemView.context, R.color.black)
            )

            when (item.type) {
                MovementType.INCOME -> mapColors["green"]?.let {
                    tvAmount.setTextColor(
                        it
                    )
                }
                MovementType.EXPENSE -> mapColors["red"]?.let {
                    tvAmount.setTextColor(
                        it
                    )
                }
            }
            tvTitle.text = item.concept
            tvDate.text = item.date
            tvAmount.text = "${item.amount} ${getCurrency()}"
            rlMovementArea.setOnClickListener {
                onClick(item)
                val movementBS = CustomBottomSheet.get<Movement>(MovementBS::class.java.name)

                movementBS?.showViewDetailBS(activity, item){
                    foundAndUpdateIt(it)
                }
            }
        }
    }
}