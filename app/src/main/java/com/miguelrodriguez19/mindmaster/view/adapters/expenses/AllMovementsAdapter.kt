package com.miguelrodriguez19.mindmaster.view.adapters.expenses

import android.app.Activity
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
import com.miguelrodriguez19.mindmaster.model.comparators.MovementComparator
import com.miguelrodriguez19.mindmaster.model.comparators.MovementsGroupComparator
import com.miguelrodriguez19.mindmaster.model.firebase.FirestoreManagerFacade
import com.miguelrodriguez19.mindmaster.model.structures.dto.expenses.MonthMovementsResponse
import com.miguelrodriguez19.mindmaster.model.structures.dto.expenses.Movement
import com.miguelrodriguez19.mindmaster.model.structures.enums.MovementType
import com.miguelrodriguez19.mindmaster.model.utils.DateTimeUtils.getMonthYearOf
import com.miguelrodriguez19.mindmaster.model.utils.Toolkit
import com.miguelrodriguez19.mindmaster.view.dialogs.AllDialogs
import java.util.stream.Collectors

class AllMovementsAdapter(
    private val activity: Activity, var data: ArrayList<MonthMovementsResponse>
) : RecyclerView.Adapter<AllMovementsAdapter.ViewHolder>() {
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
        movement: Movement, callback: (Int, Boolean) -> Unit
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

    private fun removeItemAt(groupPosition: Int, eventPosition: Int) {
        val group = data[groupPosition]
        val newList =
            ArrayList((group.expensesList + group.incomeList).sortedWith(MovementComparator()))
        newList.removeAt(eventPosition)

        val incomes = newList.stream().filter { it.type == MovementType.INCOME }.collect(Collectors.toList())
        val expenses = newList.stream().filter { it.type == MovementType.EXPENSE }.collect(Collectors.toList())

        data[groupPosition] = group.copy(expensesList = expenses, incomeList = incomes)
        notifyItemChanged(groupPosition)
    }

    private fun getGroupOf(movement: Movement): Int {
        val date = getMonthYearOf(movement.date)
        var index = -1
        data.stream().filter { it.date == date }.findFirst().ifPresent {
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
                MovementType.INCOME -> {
                    updatedList = group.incomeList.toMutableList().apply { add(movement) }
                    MonthMovementsResponse(
                        group.date, updatedList, group.expensesList
                    )
                }
                MovementType.EXPENSE -> {
                    updatedList = group.expensesList.toMutableList().apply { add(movement) }
                    MonthMovementsResponse(
                        group.date, group.incomeList, updatedList
                    )
                }
            }
            data[index] = updatedGroup
        } else {
            val newGroup = when (movement.type) {
                MovementType.INCOME -> MonthMovementsResponse(
                    getMonthYearOf(movement.date), listOf(movement), listOf()
                )
                MovementType.EXPENSE -> MonthMovementsResponse(
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
            adapter = MovementAdapter(activity, ArrayList(data)) { }
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
                            activity, activity.getString(R.string.delete_confirmation),
                            activity.getString(R.string.delete_event_message)
                        ) {
                            val position = viewHolder.adapterPosition
                            if (it) {
                                FirestoreManagerFacade.deleteMovement(adapter.getItemAt(position)) { movement ->
                                    Toolkit.showUndoSnackBar(activity, view) { ok ->
                                        if (ok) {
                                            FirestoreManagerFacade.saveMovement(movement) { item ->
                                                addItem(item)
                                            }
                                        }
                                    }
                                    isDateGroupEmptyOf(movement) { index, isEmpty ->
                                        removeItemAt(index, position)
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
                            ContextCompat.getDrawable(activity, R.drawable.ic_delete_24)?.mutate()
                        icon?.setTint(ContextCompat.getColor(activity, android.R.color.white))
                        val background =
                            ColorDrawable(activity.getColor(R.color.red_bittersweet_200))
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
                            c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                        )
                    }
                })
            itemTouchHelper.attachToRecyclerView(rvMonthMovements)
        }
    }
}