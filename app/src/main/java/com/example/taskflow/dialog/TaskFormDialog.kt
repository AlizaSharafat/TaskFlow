package com.example.taskflow.dialog

import android.app.DatePickerDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.taskflow.R
import com.example.taskflow.model.TaskItem
import com.example.taskflow.model.TaskPriority
import com.example.taskflow.model.TaskStatus
import com.example.taskflow.util.DateUtils
import com.example.taskflow.util.Menus

/**
 * The Add Task and Edit Task form.
 *
 * Overview and Tasks both open this same dialog, which is why it lives in its
 * own file rather than inside either fragment.
 */
object TaskFormDialog {

    const val DESCRIPTION_MAX = 500

    /**
     * @param existing null to add a task, or the task being edited.
     * @param onDelete supplied only when editing, which is what shows Delete.
     */
    fun show(
        context: Context,
        existing: TaskItem?,
        onSave: (description: String, status: TaskStatus, priority: TaskPriority, dueDate: Long?) -> Unit,
        onDelete: (() -> Unit)? = null
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_task_form, null)

        val title = view.findViewById<TextView>(R.id.tvDialogTitle)
        val etDescription = view.findViewById<EditText>(R.id.etTaskDescription)
        val tvCounter = view.findViewById<TextView>(R.id.tvCharCounter)
        val tvPriority = view.findViewById<TextView>(R.id.tvPrioritySelector)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatusSelector)
        val tvDueDate = view.findViewById<TextView>(R.id.tvDueDateSelector)
        val btnClearDate = view.findViewById<TextView>(R.id.btnClearDate)
        val btnSave = view.findViewById<Button>(R.id.btnSaveTask)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelTask)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteTask)

        // Defaults for a new task: Normal, To do, no due date.
        var status = existing?.status ?: TaskStatus.TODO
        var priority = existing?.priority ?: TaskPriority.NORMAL
        var dueDate: Long? = existing?.dueDate

        title.setText(if (existing == null) R.string.add_new_task else R.string.edit_task)
        btnSave.setText(if (existing == null) R.string.save_task else R.string.save_changes)
        btnDelete.visibility = if (onDelete == null) View.GONE else View.VISIBLE

        existing?.let { etDescription.setText(it.description) }

        fun paintDate() {
            val current = dueDate
            tvDueDate.text = when {
                current == null -> context.getString(R.string.no_due_date)
                DateUtils.isToday(current) -> context.getString(R.string.due_today)
                else -> DateUtils.format(current)
            }
            btnClearDate.visibility = if (current == null) View.GONE else View.VISIBLE
        }

        fun paintCounter() {
            tvCounter.text = context.getString(
                R.string.char_counter, etDescription.text.length, DESCRIPTION_MAX
            )
        }

        tvPriority.text = priority.label
        tvStatus.text = status.label
        paintDate()
        paintCounter()

        // Live 0/500 counter under the description box.
        etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun afterTextChanged(s: Editable?) = paintCounter()
        })

        tvPriority.setOnClickListener { anchor ->
            val options = TaskPriority.values()
            Menus.show(anchor, options.map { it.label }) { index ->
                priority = options[index]
                tvPriority.text = priority.label
            }
        }

        tvStatus.setOnClickListener { anchor ->
            val options = TaskStatus.values()
            Menus.show(anchor, options.map { it.label }) { index ->
                status = options[index]
                tvStatus.text = status.label
            }
        }

        tvDueDate.setOnClickListener {
            // Past dates are allowed, so no minimum is set on the picker.
            val seed = dueDate ?: DateUtils.todayStart()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    dueDate = DateUtils.fromPicker(year, month, dayOfMonth)
                    paintDate()
                },
                DateUtils.yearOf(seed),
                DateUtils.monthOf(seed),
                DateUtils.dayOf(seed)
            ).show()
        }

        btnClearDate.setOnClickListener {
            dueDate = null
            paintDate()
        }

        val dialog = AlertDialog.Builder(context).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(R.string.delete_task)
                .setMessage(R.string.delete_task_message)
                .setPositiveButton(R.string.delete) { _, _ ->
                    onDelete?.invoke()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        btnSave.setOnClickListener {
            val description = etDescription.text.toString().trim()
            if (description.isEmpty()) {
                etDescription.error = context.getString(R.string.error_task_required)
                return@setOnClickListener
            }
            if (description.length > DESCRIPTION_MAX) {
                etDescription.error =
                    context.getString(R.string.error_task_too_long, DESCRIPTION_MAX)
                return@setOnClickListener
            }
            onSave(description, status, priority, dueDate)
            dialog.dismiss()
        }

        dialog.show()
    }
}
