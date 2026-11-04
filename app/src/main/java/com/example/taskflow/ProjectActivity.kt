package com.example.taskflow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.taskflow.fragment.DiscussionFragment
import com.example.taskflow.fragment.FilesFragment
import com.example.taskflow.fragment.OverviewFragment
import com.example.taskflow.fragment.TasksFragment
import com.example.taskflow.model.DateFilter
import com.example.taskflow.model.FileQuery
import com.example.taskflow.model.Project
import com.example.taskflow.model.TaskQuery
import com.example.taskflow.model.TaskStatus
import com.google.android.material.tabs.TabLayout


class ProjectActivity : AppCompatActivity(), ProjectHost {

    private lateinit var workingProject: Project

    private val taskQuery = TaskQuery()
    private val fileQuery = FileQuery()

    private lateinit var tabLayout: TabLayout
    private lateinit var tvHeaderName: TextView
    private lateinit var tvHeaderTag: TextView

    /** Fragments are created once and then shown and hidden, never rebuilt. */
    private val fragments = mutableMapOf<Int, Fragment>()
    private var currentTab = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        val received = intent.serializableExtra(IntentKeys.EXTRA_PROJECT, Project::class.java)
        if (received == null) {
            finish()
            return
        }
        workingProject = received

        tabLayout = findViewById(R.id.tabLayout)
        tvHeaderName = findViewById(R.id.tvProjectName)
        tvHeaderTag = findViewById(R.id.tvProjectTag)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { returnUpdatedProject() }

        // The system back gesture must behave exactly like the toolbar arrow.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = returnUpdatedProject()
        })

        buildTabs()
        paintHeader()
        showTab(0)
    }

    private fun buildTabs() {
        repeat(TAB_TITLES.size) { tabLayout.addTab(tabLayout.newTab()) }
        updateTabTitles()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = showTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun updateTabTitles() {
        val counts = listOf(
            null,
            workingProject.totalTaskCount(),
            workingProject.attachmentCount(),
            workingProject.commentCount
        )
        TAB_TITLES.forEachIndexed { index, title ->
            val count = counts[index]
            tabLayout.getTabAt(index)?.text =
                if (count == null) title else getString(R.string.tab_with_count, title, count)
        }
    }

    private fun paintHeader() {
        tvHeaderName.text = workingProject.name
        tvHeaderTag.text = workingProject.tag
    }

    private fun fragmentFor(index: Int): Fragment = fragments.getOrPut(index) {
        when (index) {
            0 -> OverviewFragment.newInstance(workingProject.id)
            1 -> TasksFragment.newInstance(workingProject.id)
            2 -> FilesFragment.newInstance(workingProject.id)
            else -> DiscussionFragment.newInstance(workingProject.id)
        }
    }

    // ---- ProjectHost ----

    override fun project(): Project = workingProject

    override fun taskQuery(): TaskQuery = taskQuery

    override fun fileQuery(): FileQuery = fileQuery


    override fun onProjectChanged() {
        paintHeader()
        updateTabTitles()
        (fragments[currentTab] as? ProjectTab)?.refreshTab()
    }

    override fun showTab(index: Int) {
        if (index == currentTab) {
            (fragments[index] as? ProjectTab)?.refreshTab()
            return
        }

        val transaction = supportFragmentManager.beginTransaction()
        val target = fragmentFor(index)

        fragments.forEach { (position, fragment) ->
            if (position != index && fragment.isAdded) transaction.hide(fragment)
        }

        if (target.isAdded) {
            transaction.show(target)
        } else {
            transaction.add(R.id.tabContainer, target, "tab_$index")
        }
        transaction.commit()
        supportFragmentManager.executePendingTransactions()

        currentTab = index
        if (tabLayout.selectedTabPosition != index) {
            tabLayout.getTabAt(index)?.select()
        }
        // A hidden tab reads the records again the moment it becomes visible.
        (target as? ProjectTab)?.refreshTab()
    }


    override fun openTasksFiltered(status: TaskStatus?, date: DateFilter) {
        taskQuery.status = status
        taskQuery.date = date
        showTab(1)
    }


    private fun returnUpdatedProject() {
        val data = Intent()
        data.putExtra(IntentKeys.EXTRA_PROJECT, workingProject)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {
        private val TAB_TITLES = listOf("Overview", "Tasks", "Files", "Discussion")
    }
}
