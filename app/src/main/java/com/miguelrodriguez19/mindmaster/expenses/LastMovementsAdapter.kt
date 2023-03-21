package com.miguelrodriguez19.mindmaster.expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellMovementBinding
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse

class LastMovementsAdapter(
    private val data: ArrayList<MonthMovementsResponse.Movement>,
    val onClick: (MonthMovementsResponse.Movement) -> Unit
) :
    RecyclerView.Adapter<LastMovementsAdapter.ViewHolder>() {
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
                "income" -> mapColors["green"]?.let { tvAmount.setTextColor(it) }
                "expense" -> mapColors["red"]?.let { tvAmount.setTextColor(it) }
                else -> {
                    mapColors["black"]?.let { tvAmount.setTextColor(it) }
                }
            }
            tvTitle.text = item.title
            tvDate.text = item.date
            tvAmount.text = "${item.amount}€"
            rlMovementArea.setOnClickListener {
                onClick(item)
            }

        }
    }
}