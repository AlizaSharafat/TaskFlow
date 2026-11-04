package com.example.taskflow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.taskflow.adapter.ProjectAdapter
import com.example.taskflow.dialog.ProjectFormDialog
import com.example.taskflow.model.AttachmentLink
import com.example.taskflow.model.Project
import com.example.taskflow.model.SampleData


class HomeActivity : AppCompatActivity() {


    private val projects: MutableList<Project> = SampleData.projects()

    private lateinit var adapter: ProjectAdapter
    private lateinit var listView: ListView
    private lateinit var etSearch: EditText
    private lateinit var tvShowing: TextView
    private lateinit var tvEmpty: TextView

    private var searchText: String = ""

    private val projectDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val updated = result.data?.serializableExtra(IntentKeys.EXTRA_PROJECT, Project::class.java)
            ?: return@registerForActivityResult

        val index = projects.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            projects[index] = updated
        }
        refreshList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        listView = findViewById(R.id.listProjects)
        etSearch = findViewById(R.id.etSearchProjects)
        tvShowing = findViewById(R.id.tvShowingProjects)
        tvEmpty = findViewById(R.id.tvNoProjects)

        showWorkspaceDetailsFromIntent()

        adapter = ProjectAdapter(this, emptyList())
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            openProjectScreen(adapter.getItem(position))
        }

        findViewById<Button>(R.id.btnNewProject).setOnClickListener { showNewProjectDialog() }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                searchText = s.toString()
                refreshList()
            }
        })

        refreshList()
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

    /** Search is case-insensitive and matches any part of the name or the tag. */
    private fun visibleProjects(): List<Project> {
        val needle = searchText.trim().lowercase()
        if (needle.isEmpty()) return projects
        return projects.filter {
            it.name.lowercase().contains(needle) || it.tag.lowercase().contains(needle)
        }
    }

    private fun refreshList() {
        val shown = visibleProjects()
        adapter.submit(shown)

        tvShowing.text = if (searchText.trim().isEmpty()) {
            resources.getQuantityString(R.plurals.showing_projects, shown.size, shown.size)
        } else {
            getString(R.string.showing_of_projects, shown.size, projects.size)
        }

        tvEmpty.text = if (projects.isEmpty()) {
            getString(R.string.no_projects_yet)
        } else {
            getString(R.string.no_projects_match)
        }
        tvEmpty.visibility = if (shown.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openProjectScreen(project: Project) {
        val intent = Intent(this, ProjectActivity::class.java)
        intent.putExtra(IntentKeys.EXTRA_PROJECT, project)
        projectDetailLauncher.launch(intent)
    }


    private fun showNewProjectDialog() {
        ProjectFormDialog.show(this, null) { name, tag, description, catalogId ->
            val project = Project(tag = tag, name = name, description = description)
            if (catalogId != null) {
                project.attachments.add(AttachmentLink(catalogId = catalogId))
            }
            projects.add(project)
            refreshList()
        }
    }
}
