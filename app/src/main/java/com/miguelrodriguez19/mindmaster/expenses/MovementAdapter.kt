package com.miguelrodriguez19.mindmaster.expenses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellMovementBinding
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.utils.AllBottomSheets

class MovementAdapter(
    private val context: Context,
    private val data: ArrayList<MonthMovementsResponse.Movement>,
    val onClick: (MonthMovementsResponse.Movement) -> Unit
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
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellMovementBinding.bind(v)
        private val rlMovementArea = bind.rlLatestMovementsCell
        private val tvDate = bind.tvMovementsDate
        private val tvTitle = bind.tvMovementTitle
        private val tvAmount = bind.tvMovementsAmount

        fun bind(item: MonthMovementsResponse.Movement) {
            val mapColors = mapOf(
                "green" to ContextCompat.getColor(itemView.context, R.color.green_jade_500),
                "red" to ContextCompat.getColor(itemView.context, R.color.red_bittersweet_200),
                "black" to ContextCompat.getColor(itemView.context, R.color.black)
            )

            when (item.type) {
                MonthMovementsResponse.Type.INCOME -> mapColors["green"]?.let { tvAmount.setTextColor(it) }
                MonthMovementsResponse.Type.EXPENSE -> mapColors["red"]?.let { tvAmount.setTextColor(it) }
                else -> {
                    mapColors["black"]?.let { tvAmount.setTextColor(it) }
                }
            }
            tvTitle.text = item.concept
            tvDate.text = item.date
            tvAmount.text = "${item.amount}€"
            rlMovementArea.setOnClickListener {
                AllBottomSheets.showMovementBS(context, item, item.type)
                onClick(item)
            }

        }
    }
}