package com.example.taskflow.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.taskflow.R
import com.example.taskflow.model.TaskItem
import com.example.taskflow.util.DateUtils

/**
 * Feeds the task ListView in the Tasks tab.
 *
 * The checkbox listener is detached before binding and reattached afterwards,
 * otherwise recycling a row would fire the listener of the previous task.
 */
class TaskAdapter(
    private val context: Context,
    private var rows: List<TaskItem>,
    private val onToggleDone: (TaskItem, Boolean) -> Unit
) : BaseAdapter() {

    private class ViewHolder(view: View) {
        val check: CheckBox = view.findViewById(R.id.cbTask)
        val description: TextView = view.findViewById(R.id.tvTaskDescription)
        val meta: TextView = view.findViewById(R.id.tvTaskMeta)
        val overdue: TextView = view.findViewById(R.id.tvOverdueBadge)
    }

    override fun getCount(): Int = rows.size

    override fun getItem(position: Int): TaskItem = rows[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun submit(newRows: List<TaskItem>) {
        rows = newRows
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_task, parent, false).also { it.tag = ViewHolder(it) }
        val holder = view.tag as ViewHolder
        val task = rows[position]
        val todayStart = DateUtils.todayStart()
        val overdue = task.isOverdue(todayStart)

        holder.description.text = task.description
        holder.meta.text = buildMeta(task)

        // A done row stays readable: struck through and muted, never invisible.
        holder.description.paintFlags = if (task.isDone) {
            holder.description.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.description.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        holder.description.setTextColor(
            ContextCompat.getColor(context, if (task.isDone) R.color.ink_soft else R.color.ink)
        )
        holder.meta.setTextColor(
            ContextCompat.getColor(
                context,
                when {
                    overdue -> R.color.danger
                    task.dueDate != null && DateUtils.isToday(task.dueDate!!) -> R.color.warning
                    else -> R.color.ink_muted
                }
            )
        )

        holder.overdue.visibility = if (overdue) View.VISIBLE else View.GONE

        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = task.isDone
        holder.check.setOnCheckedChangeListener { _, isChecked ->
            onToggleDone(task, isChecked)
        }
        return view
    }

    /** "To do · High · Due today" or "Done · Normal · No due date". */
    private fun buildMeta(task: TaskItem): String {
        val due = task.dueDate
        val dueLabel = when {
            due == null -> context.getString(R.string.no_due_date)
            DateUtils.isToday(due) -> context.getString(R.string.due_today)
            else -> DateUtils.format(due)
        }
        return "${task.status.label} · ${task.priority.label} · $dueLabel"
    }
}
