package com.miguelrodriguez19.mindmaster.expenses

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellDayAllMovementsBinding
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse.Movement
import com.miguelrodriguez19.mindmaster.models.MonthMovementsResponse.Type
import com.miguelrodriguez19.mindmaster.models.comparators.MovementComparator
import com.miguelrodriguez19.mindmaster.models.comparators.MovementsGroupComparator
import com.miguelrodriguez19.mindmaster.utils.AllDialogs
import com.miguelrodriguez19.mindmaster.utils.FirebaseManager
import com.miguelrodriguez19.mindmaster.utils.Toolkit
import com.miguelrodriguez19.mindmaster.utils.Toolkit.getMonthYearOf
import kotlin.streams.toList

class AllMovementsAdapter(
    private val context: Context,
    var data: ArrayList<MonthMovementsResponse>
) :
    RecyclerView.Adapter<AllMovementsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_day_all_movements, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
    fun setData(newData: List<MonthMovementsResponse>) {
        this.data.clear()
        this.data.addAll(newData)
        notifyItemRangeChanged(0, data.size)
    }

    private fun isDateGroupEmptyOf(
        movement: Movement,
        callback: (Int, Boolean) -> Unit
    ) {
        val index = getGroupOf(movement)
        val group = data[index]
        val isEmpty = (group.incomeList + group.expensesList).size <= 1
        callback(index, isEmpty)
    }

    private fun removeDateGroupAt(index: Int) {
        data.removeAt(index)
        notifyItemRemoved(index)
    }

    private fun removeEventAt(groupPosition: Int, eventPosition: Int) {
        val group = data[groupPosition]
        val newList = ArrayList((group.expensesList + group.incomeList).sortedWith(MovementComparator()))
        newList.removeAt(eventPosition)

        val incomes = newList.stream().filter { it.type == Type.INCOME }.toList()
        val expenses = newList.stream().filter { it.type == Type.EXPENSE }.toList()

        data[groupPosition] = group.copy(expensesList = expenses, incomeList = incomes)
        notifyItemChanged(groupPosition)
    }

    private fun getGroupOf(movement: Movement): Int {
        val date = getMonthYearOf(movement.date)
        var index = -1
        data.stream()
            .filter { it.date == date }
            .findFirst()
            .ifPresent {
                index = data.indexOf(it)
            }
        return index
    }

    fun addItem(movement: Movement) {
        val index = getGroupOf(movement)
        if (index != -1) {
            val group = data[index]
            val updatedList: List<Movement>
            val updatedGroup = when (movement.type) {
                Type.INCOME -> {
                    updatedList = group.incomeList.toMutableList().apply { add(movement) }
                    MonthMovementsResponse(
                        group.date, updatedList, group.expensesList
                    )
                }
                Type.EXPENSE -> {
                    updatedList = group.expensesList.toMutableList().apply { add(movement) }
                    MonthMovementsResponse(
                        group.date, group.incomeList, updatedList
                    )
                }
            }
            data[index] = updatedGroup
        } else {
            val newGroup = when (movement.type) {
                Type.INCOME -> MonthMovementsResponse(
                    getMonthYearOf(movement.date), listOf(movement), listOf()
                )
                Type.EXPENSE -> MonthMovementsResponse(
                    getMonthYearOf(movement.date), listOf(), listOf(movement)
                )
            }
            data.add(newGroup)
        }
        data = ArrayList(data.sortedWith(MovementsGroupComparator()))
        notifyDataSetChanged()
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellDayAllMovementsBinding.bind(v)
        private val btnMonth = bind.btnMonthTitle
        private val rvMonthMovements = bind.rvMonthMovements
        private lateinit var adapter: MovementAdapter
        private val view = v
        fun bind(item: MonthMovementsResponse) {
            initRecyclerView((item.expensesList + item.incomeList).sortedWith(MovementComparator()))
            btnMonth.text = item.date
            btnMonth.setOnClickListener {
                if (rvMonthMovements.visibility == View.GONE) {
                    rvMonthMovements.visibility = View.VISIBLE
                } else {
                    rvMonthMovements.visibility = View.GONE
                }
            }
        }

        private fun initRecyclerView(data: List<Movement>) {
            val mLayoutManager = StaggeredGridLayoutManager(1, 1)
            rvMonthMovements.layoutManager = mLayoutManager
            adapter = MovementAdapter(context, ArrayList(data)) { }
            rvMonthMovements.adapter = adapter

            val itemTouchHelper =
                ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        AllDialogs.showConfirmationDialog(
                            context,
                            context.getString(R.string.delete_confirmation),
                            context.getString(R.string.delete_event_message)
                        ) {
                            val position = viewHolder.adapterPosition
                            if (it) {
                                FirebaseManager.deleteMovement(
                                    context, adapter.getItemAt(position)
                                ) { movement ->
                                    Toolkit.showUndoSnackBar(context, view) { ok ->
                                        if (ok) {
                                            FirebaseManager.saveMovement(
                                                context, movement
                                            ) { item ->
                                                addItem(item)
                                            }
                                        }
                                    }
                                    isDateGroupEmptyOf(movement) { index, isEmpty ->
                                        removeEventAt(index, position)
                                        adapter.removeAt(position)
                                        if (isEmpty) {
                                            removeDateGroupAt(index)
                                        }
                                    }
                                }
                            } else {
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onChildDraw(
                        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
                    ) {
                        val icon =
                            ContextCompat.getDrawable(context, R.drawable.ic_delete_24)?.mutate()
                        icon?.setTint(ContextCompat.getColor(context, android.R.color.white))
                        val background =
                            ColorDrawable(context.getColor(R.color.red_bittersweet_200))
                        val itemView = viewHolder.itemView
                        val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
                        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                        val iconBottom = iconTop + icon.intrinsicHeight

                        // Swipe to right
                        if (dX > 0) {
                            val iconLeft = itemView.left + iconMargin
                            val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                            background.setBounds(
                                itemView.left,
                                itemView.top,
                                itemView.left + dX.toInt(),
                                itemView.bottom
                            )
                        } else { // Other swipes
                            background.setBounds(0, 0, 0, 0)
                        }
                        background.draw(c)
                        icon.draw(c)
                        super.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }
                })
            itemTouchHelper.attachToRecyclerView(rvMonthMovements)
        }
    }
}