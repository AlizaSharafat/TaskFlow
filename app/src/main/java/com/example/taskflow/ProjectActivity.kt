package com.example.taskflow

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.taskflow.model.Project
import com.example.taskflow.model.TaskItem

class ProjectActivity : AppCompatActivity() {

    private lateinit var project: Project
    private var projectPosition: Int = -1

    private lateinit var taskListContainer: LinearLayout
    private lateinit var attachmentContainer: LinearLayout

    private val attachmentTileStyles = listOf(
        R.drawable.bg_attachment_pink,
        R.drawable.bg_attachment_lavender,
        R.drawable.bg_attachment_cream
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        val receivedProject = intent.serializableExtra(IntentKeys.EXTRA_PROJECT, Project::class.java)
        if (receivedProject == null) {
            finish()
            return
        }
        project = receivedProject
        projectPosition = intent.getIntExtra(IntentKeys.EXTRA_PROJECT_POSITION, -1)

        taskListContainer = findViewById(R.id.taskListContainer)
        attachmentContainer = findViewById(R.id.attachmentContainer)

        showProjectDetails()
        renderAttachments()
        renderTasks()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { returnUpdatedProject() }
        findViewById<Button>(R.id.btnAddTask).setOnClickListener { showAddTaskDialog() }

        // System back gesture returns the same result as the back arrow.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = returnUpdatedProject()
        })
    }

    private fun showProjectDetails() {
        findViewById<TextView>(R.id.tvProjectTag).text = project.tag
        findViewById<TextView>(R.id.tvProjectName).text = project.name
        findViewById<TextView>(R.id.tvProjectDescription).text = project.description
    }

    private fun renderAttachments() {
        attachmentContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        project.attachments.forEach { attachment ->
            val tile = inflater.inflate(R.layout.item_attachment, attachmentContainer, false)
            tile.findViewById<View>(R.id.attachmentBackground)
                .setBackgroundResource(attachmentTileStyles[attachment.styleIndex % attachmentTileStyles.size])
            tile.findViewById<TextView>(R.id.tvAttachmentName).text = attachment.fileName
            tile.findViewById<ImageView>(R.id.ivAttachmentIcon)
                .setImageResource(R.drawable.ic_attachment)
            attachmentContainer.addView(tile)
        }
    }

    private fun renderTasks() {
        taskListContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        project.tasks.forEach { task ->
            val row = inflater.inflate(R.layout.item_task, taskListContainer, false)
            val checkBox = row.findViewById<CheckBox>(R.id.cbTask)
            val tvDescription = row.findViewById<TextView>(R.id.tvTaskDescription)

            tvDescription.text = task.description
            checkBox.isChecked = task.isDone
            applyDoneStyle(tvDescription, task.isDone)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                task.isDone = isChecked
                applyDoneStyle(tvDescription, isChecked)
            }

            row.setOnClickListener { checkBox.isChecked = !checkBox.isChecked }

            taskListContainer.addView(row)
        }
    }

    private fun applyDoneStyle(textView: TextView, isDone: Boolean) {
        textView.paintFlags = if (isDone) {
            textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val etTask = dialogView.findViewById<EditText>(R.id.etTaskDescription)
        val btnSaveTask = dialogView.findViewById<Button>(R.id.btnSaveTask)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)

        btnSaveTask.setOnClickListener {
            val description = etTask.text.toString().trim()

            if (description.isEmpty()) {
                etTask.error = getString(R.string.error_task_required)
                return@setOnClickListener
            }
            if (description.length > MAX_TASK_LENGTH) {
                etTask.error = getString(R.string.error_task_too_long, MAX_TASK_LENGTH)
                return@setOnClickListener
            }

            project.tasks.add(TaskItem(description = description))
            renderTasks()
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun returnUpdatedProject() {
        val resultIntent = Intent().apply {
            putExtra(IntentKeys.EXTRA_PROJECT, project)
            putExtra(IntentKeys.EXTRA_PROJECT_POSITION, projectPosition)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        private const val MAX_TASK_LENGTH = 500
    }
}
