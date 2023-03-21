package com.miguelrodriguez19.mindmaster.expenses

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellDayAllMovementsBinding
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse


class AllMovementsAdapter(private val data: ArrayList<MonthMovementsResponse>) :
    RecyclerView.Adapter<AllMovementsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_day_all_movements, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellDayAllMovementsBinding.bind(v)
        private val btnMonth = bind.btnMonthTitle
        private val rvMonthMovements = bind.rvMonthMovements
        private lateinit var adapter: LastMovementsAdapter

        fun bind(item: MonthMovementsResponse) {
            initRecyclerView(item.movementsList)
            btnMonth.text = item.date
            btnMonth.setOnClickListener {
                if (rvMonthMovements.visibility == View.GONE){
                    rvMonthMovements.visibility = View.VISIBLE
                }else{
                    rvMonthMovements.visibility = View.GONE
                }
            }
        }

        private fun initRecyclerView(data : List<MonthMovementsResponse.Movement>) {
            val dataArrList = ArrayList<MonthMovementsResponse.Movement>(data)
            val mLayoutManager = StaggeredGridLayoutManager(1, 1)
            rvMonthMovements.layoutManager = mLayoutManager

            adapter = LastMovementsAdapter(dataArrList){ movement ->
                Log.i("AllMovementsAdapter", "onViewCreated - event: ${movement.title}")
            }

            rvMonthMovements.adapter = adapter
        }

    }
}