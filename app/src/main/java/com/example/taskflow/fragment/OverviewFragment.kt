package com.example.taskflow.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.taskflow.IntentKeys
import com.example.taskflow.ProjectHost
import com.example.taskflow.ProjectTab
import com.example.taskflow.R
import com.example.taskflow.dialog.ProjectFormDialog
import com.example.taskflow.dialog.TaskFormDialog
import com.example.taskflow.model.AttachmentCatalog
import com.example.taskflow.model.DateFilter
import com.example.taskflow.model.TaskItem
import com.example.taskflow.model.TaskStatus
import com.example.taskflow.util.DateUtils

/**
 * The summary tab: description, progress, task breakdown and quick links.
 * Every number here is derived from the task records, never stored.
 */
class OverviewFragment : Fragment(), ProjectTab {

    private lateinit var host: ProjectHost

    private lateinit var tvDescription: TextView
    private lateinit var tvCompleted: TextView
    private lateinit var tvPercent: TextView
    private lateinit var progress: ProgressBar
    private lateinit var tvToDo: TextView
    private lateinit var tvInProgress: TextView
    private lateinit var tvDone: TextView
    private lateinit var tvOverdue: TextView
    private lateinit var tvAttachmentsSummary: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        host = context as ProjectHost
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_overview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDescription = view.findViewById(R.id.tvOverviewDescription)
        tvCompleted = view.findViewById(R.id.tvCompletedTasks)
        tvPercent = view.findViewById(R.id.tvOverviewPercent)
        progress = view.findViewById(R.id.progressOverview)
        tvToDo = view.findViewById(R.id.tvCountToDo)
        tvInProgress = view.findViewById(R.id.tvCountInProgress)
        tvDone = view.findViewById(R.id.tvCountDone)
        tvOverdue = view.findViewById(R.id.tvCountOverdue)
        tvAttachmentsSummary = view.findViewById(R.id.tvAttachmentsSummary)

        view.findViewById<View>(R.id.btnEditProject).setOnClickListener { showEditProject() }

        // Tapping a breakdown card filters the Tasks tab by that status.
        view.findViewById<View>(R.id.cardToDo).setOnClickListener {
            host.openTasksFiltered(TaskStatus.TODO, DateFilter.ALL)
        }
        view.findViewById<View>(R.id.cardInProgress).setOnClickListener {
            host.openTasksFiltered(TaskStatus.IN_PROGRESS, DateFilter.ALL)
        }
        view.findViewById<View>(R.id.cardDone).setOnClickListener {
            host.openTasksFiltered(TaskStatus.DONE, DateFilter.ALL)
        }
        // Overdue is a date filter across every status, not a status of its own.
        view.findViewById<View>(R.id.cardOverdue).setOnClickListener {
            host.openTasksFiltered(null, DateFilter.OVERDUE)
        }

        view.findViewById<View>(R.id.linkAttachments).setOnClickListener { host.showTab(2) }
        view.findViewById<View>(R.id.linkDiscussion).setOnClickListener { host.showTab(3) }

        view.findViewById<Button>(R.id.btnAddTaskOverview).setOnClickListener { showAddTask() }

        refreshTab()
    }

    override fun refreshTab() {
        if (!isAdded) return
        val project = host.project()
        val todayStart = DateUtils.todayStart()

        tvDescription.text = project.description
        tvCompleted.text = getString(
            R.string.completed_of_tasks, project.doneTaskCount(), project.totalTaskCount()
        )
        tvPercent.text = getString(R.string.percent_value, project.progressPercent())
        progress.progress = project.progressPercent()

        tvToDo.text = project.toDoCount().toString()
        tvInProgress.text = project.inProgressCount().toString()
        tvDone.text = project.doneTaskCount().toString()
        tvOverdue.text = project.overdueCount(todayStart).toString()

        val names = project.attachments
            .mapNotNull { AttachmentCatalog.find(it.catalogId)?.displayName }
            .take(2)
            .joinToString(", ")
        tvAttachmentsSummary.text = if (project.attachments.isEmpty()) {
            getString(R.string.no_attachments_yet)
        } else {
            getString(R.string.attachments_summary, project.attachmentCount(), names)
        }
    }

    /** Same validation as New Project, with the current values prefilled. */
    private fun showEditProject() {
        val project = host.project()
        ProjectFormDialog.show(requireContext(), project) { name, tag, description, _ ->
            project.name = name
            project.tag = tag
            project.description = description
            host.onProjectChanged()
        }
    }

    /** The same dialog the Tasks tab uses. After saving we stay on Overview. */
    private fun showAddTask() {
        TaskFormDialog.show(requireContext(), null, { description, status, priority, dueDate ->
            host.project().tasks.add(
                TaskItem(
                    description = description,
                    status = status,
                    priority = priority,
                    dueDate = dueDate
                )
            )
            host.onProjectChanged()
        })
    }

    companion object {
        fun newInstance(projectId: String): OverviewFragment {
            val fragment = OverviewFragment()
            fragment.arguments = Bundle().apply {
                putString(IntentKeys.ARG_PROJECT_ID, projectId)
            }
            return fragment
        }
    }
}
