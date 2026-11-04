package com.example.taskflow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.taskflow.model.Attachment
import com.example.taskflow.model.Project
import com.example.taskflow.model.TaskItem

class HomeActivity : AppCompatActivity() {

    private val projects = mutableListOf<Project>()


    private lateinit var projectListContainer: LinearLayout

    private val projectDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult

        val updatedProject = data.serializableExtra(IntentKeys.EXTRA_PROJECT, Project::class.java)
        val position = data.getIntExtra(IntentKeys.EXTRA_PROJECT_POSITION, -1)

        if (updatedProject != null && position in projects.indices) {
            projects[position] = updatedProject
            renderProjectList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        projectListContainer = findViewById(R.id.projectListContainer)

        showWorkspaceDetailsFromIntent()

        findViewById<Button>(R.id.btnNewProject).setOnClickListener {
            showNewProjectDialog()
        }

        loadStartingProjects()
        renderProjectList()
    }

    private fun showWorkspaceDetailsFromIntent() {
        val userName = intent.getStringExtra(IntentKeys.EXTRA_USER_NAME).orEmpty()
        val workspaceName = intent.getStringExtra(IntentKeys.EXTRA_WORKSPACE_NAME).orEmpty()
        val workspaceDate = intent.getStringExtra(IntentKeys.EXTRA_WORKSPACE_DATE).orEmpty()

        findViewById<TextView>(R.id.tvWelcome).text = getString(R.string.welcome_title, userName)
        findViewById<TextView>(R.id.tvWorkspaceName).text = workspaceName
        findViewById<TextView>(R.id.tvWorkspaceDate).text =
            getString(R.string.workspace_created, workspaceDate)
    }


    private fun loadStartingProjects() {
        projects.add(
            Project(
                tag = "User Interface",
                name = "Service Page Website",
                description = "Make a page display about services for websites company with blue and red colors",
                attachments = mutableListOf(
                    Attachment("sports-projectv2.fig", 0),
                    Attachment("briefing from client.pdf", 1),
                    Attachment("mount asset", 2)
                ),
                tasks = mutableListOf(
                    TaskItem(description = "Create brand guideline for website design", isDone = true),
                    TaskItem(description = "Create low fidelity service page website")
                ),
                commentCount = 10
            )
        )
    }

    private fun renderProjectList() {
        projectListContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        projects.forEachIndexed { position, project ->
            val card = inflater.inflate(R.layout.item_project, projectListContainer, false)

            card.findViewById<TextView>(R.id.tvProjectTag).text = project.tag
            card.findViewById<TextView>(R.id.tvProjectName).text = project.name
            card.findViewById<TextView>(R.id.tvProjectDescription).text = project.description

            card.findViewById<TextView>(R.id.tvTaskProgress).text = getString(
                R.string.task_progress,
                project.doneTaskCount(),
                project.totalTaskCount()
            )
            card.findViewById<TextView>(R.id.tvAttachmentCount).text =
                getString(R.string.attachment_count, project.attachments.size)
            card.findViewById<TextView>(R.id.tvCommentCount).text =
                getString(R.string.comment_count, project.commentCount)

            card.setOnClickListener { openProjectScreen(project, position) }

            projectListContainer.addView(card)
        }
    }

    private fun openProjectScreen(project: Project, position: Int) {
        val intent = Intent(this, ProjectActivity::class.java).apply {
            putExtra(IntentKeys.EXTRA_PROJECT, project)
            putExtra(IntentKeys.EXTRA_PROJECT_POSITION, position)
        }
        projectDetailLauncher.launch(intent)
    }

    private fun showNewProjectDialog() {
        val dialogView: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_new_project, null)

        val etName = dialogView.findViewById<EditText>(R.id.etProjectName)
        val etTag = dialogView.findViewById<EditText>(R.id.etProjectTag)
        val etAttachment = dialogView.findViewById<EditText>(R.id.etAttachment)
        val etDescription = dialogView.findViewById<EditText>(R.id.etProjectDescription)
        val btnAddProject = dialogView.findViewById<Button>(R.id.btnAddProject)
        val btnremove = dialogView.findViewById<Button>(R.id.btnClearAll)

        btnremove.setOnClickListener {
            Toast.makeText(this, "Remove all pressed",Toast.LENGTH_SHORT).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)

        btnAddProject.setOnClickListener {
            val name = etName.text.toString().trim()
            val tag = etTag.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = getString(R.string.error_name_required)
                return@setOnClickListener
            }
            if (tag.isEmpty()) {
                etTag.error = getString(R.string.error_tag_required)
                return@setOnClickListener
            }

            projects.add(
                Project(
                    tag = tag,
                    name = name,
                    description = description,
                    attachments = buildAttachments(etAttachment.text.toString())
                )
            )
            renderProjectList()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun buildAttachments(rawInput: String): MutableList<Attachment> =
        rawInput.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapIndexed { index, fileName -> Attachment(fileName, index % 3) }
            .toMutableList()
}
