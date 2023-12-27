package com.miguelrodriguez19.mindmaster.views.schedule.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellCalendarEventsBinding
import com.miguelrodriguez19.mindmaster.model.comparators.EventComparator
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import com.miguelrodriguez19.mindmaster.model.structures.enums.ActivityType
import com.miguelrodriguez19.mindmaster.model.structures.enums.Priority
import com.miguelrodriguez19.mindmaster.model.structures.enums.Status
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Event
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Reminder
import com.miguelrodriguez19.mindmaster.model.structures.dto.schedule.Task
import com.miguelrodriguez19.mindmaster.model.utils.AllBottomSheets.Companion.showEventsBS
import com.miguelrodriguez19.mindmaster.model.utils.AllBottomSheets.Companion.showRemindersBS
import com.miguelrodriguez19.mindmaster.model.utils.AllBottomSheets.Companion.showTasksBS

class CalendarEventsAdapter(
    private val context: Context,
    private val data: ArrayList<AbstractActivity>,
    val onClick: (AbstractActivity) -> Unit
) :
    RecyclerView.Adapter<CalendarEventsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_calendar_events, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun removeAt(position: Int) {
        data.removeAt(position)
        notifyItemRangeRemoved(position, 1)
    }

    fun getItemAt(index: Int): AbstractActivity {
        return data[index]
    }

    fun setData(newData: List<AbstractActivity>) {
        this.data.clear()
        this.data.addAll(newData.sortedWith(EventComparator()))
        notifyDataSetChanged()
    }

    fun addItem(item: AbstractActivity) {
        this.data.add(item)
        this.data.sortedWith(EventComparator())
        notifyDataSetChanged()
    }

    fun foundAndUpdateIt(abs: AbstractActivity) {
        var index = 0
        data.stream()
            .filter { it.uid == abs.uid }
            .findFirst()
            .ifPresent {
                index = data.indexOf(it)
                data[index] = abs
            }
        notifyItemChanged(index, abs)
    }


    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellCalendarEventsBinding.bind(v)
        private val cvEventArea = bind.cvEventArea
        private val tvEventType = bind.tvEventType
        private val tvEventTitle = bind.tvEventTitle
        private val civColorTag = bind.civColorTag

        fun bind(item: AbstractActivity) {
            var title: CharSequence = item.title
            if (item.type == ActivityType.TASK) {
                val task = item as Task
                if (task.status == Status.COMPLETED) {
                    val spannableString = SpannableString(item.title)
                    spannableString.setSpan(
                        StrikethroughSpan(),
                        0,
                        item.title.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    title = spannableString
                }
                if (task.priority == Priority.URGENT || task.priority == Priority.HIGH) {
                    setInfoLayout(task.priority)
                }
            }
            tvEventTitle.text = title
            val header = StringBuilder(AbstractActivity.getItemType(context, item.type))
            if (!item.category.isNullOrEmpty()) {
                header.append(" - ").append(item.category!!.joinToString(", "))
            }
            tvEventType.text = header
            civColorTag.setCardBackgroundColor(AbstractActivity.getColor(context, item.colorTag))
            cvEventArea.setOnClickListener {
                onClick(item)
                when (item.type) {
                    ActivityType.EVENT -> showEventsBS(context, item as Event) {
                        foundAndUpdateIt(it)
                    }
                    ActivityType.REMINDER -> showRemindersBS(context, item as Reminder) {
                        foundAndUpdateIt(it)
                    }
                    ActivityType.TASK -> showTasksBS(context, item as Task) {
                        foundAndUpdateIt(it)
                    }
                }
            }
        }

        private fun setInfoLayout(priority: Priority) {
            val text: String
            val icon = AppCompatResources.getDrawable(context, R.drawable.ic_report_problem_24)!!.mutate()

            val color = when (priority) {
                Priority.HIGH -> {
                    text = context.getString(R.string.priority_high)
                    ContextCompat.getColor(context, R.color.orange)
                }
                Priority.URGENT -> {
                    text = context.getString(R.string.priority_urgent)
                    ContextCompat.getColor(context, R.color.red_error_500)
                }
                else -> {
                    text = ""
                    ContextCompat.getColor(context, R.color.black)
                }
            }

            val colorStateList = ColorStateList.valueOf(color)
            DrawableCompat.setTintList(icon, colorStateList)
            bind.ivInfoIcon.setImageDrawable(icon)
            bind.tvInfoLabel.setTextColor(color)
            bind.tvInfoLabel.text = text
            bind.llInfoIcons.visibility = View.VISIBLE
        }
    }
}