package com.example.taskflow.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.taskflow.IntentKeys
import com.example.taskflow.ProjectHost
import com.example.taskflow.ProjectTab
import com.example.taskflow.R
import com.example.taskflow.adapter.TaskAdapter
import com.example.taskflow.dialog.TaskFormDialog
import com.example.taskflow.model.DateFilter
import com.example.taskflow.model.TaskItem
import com.example.taskflow.model.TaskPriority
import com.example.taskflow.model.TaskSort
import com.example.taskflow.model.TaskStatus
import com.example.taskflow.util.Menus

/**
 * The task list with search, three filters and a sort.
 *
 * The query lives in ProjectActivity, not here, which is how the tab keeps
 * its filters when the user switches away and comes back.
 */
class TasksFragment : Fragment(), ProjectTab {

    private lateinit var host: ProjectHost
    private lateinit var adapter: TaskAdapter

    private lateinit var etSearch: EditText
    private lateinit var tvStatus: TextView
    private lateinit var tvPriority: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvSort: TextView
    private lateinit var tvShowing: TextView
    private lateinit var listView: ListView
    private lateinit var emptyBox: View
    private lateinit var tvEmptyTitle: TextView
    private lateinit var btnEmptyAction: Button

    /** Stops the search box from re-filtering while we are writing into it. */
    private var bindingSearch = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        host = context as ProjectHost
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tasks, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearch = view.findViewById(R.id.etSearchTasks)
        tvStatus = view.findViewById(R.id.filterStatus)
        tvPriority = view.findViewById(R.id.filterPriority)
        tvDate = view.findViewById(R.id.filterDate)
        tvSort = view.findViewById(R.id.filterSort)
        tvShowing = view.findViewById(R.id.tvShowingTasks)
        listView = view.findViewById(R.id.listTasks)
        emptyBox = view.findViewById(R.id.emptyTasks)
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTasksTitle)
        btnEmptyAction = view.findViewById(R.id.btnEmptyTasksAction)

        adapter = TaskAdapter(requireContext(), emptyList()) { task, checked ->
            // Ticking the box sets the task to Done, clearing it sends it back To do.
            task.status = if (checked) TaskStatus.DONE else TaskStatus.TODO
            host.onProjectChanged()
        }
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            showEditTask(adapter.getItem(position))
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (bindingSearch) return
                host.taskQuery().search = s.toString()
                refreshTab()
            }
        })

        tvStatus.setOnClickListener { anchor ->
            val options = listOf<TaskStatus?>(null) + TaskStatus.values().toList()
            val labels = listOf(getString(R.string.all_statuses)) + TaskStatus.values().map { it.label }
            Menus.show(anchor, labels) { index ->
                host.taskQuery().status = options[index]
                refreshTab()
            }
        }

        tvPriority.setOnClickListener { anchor ->
            val options = listOf<TaskPriority?>(null) + TaskPriority.values().toList()
            val labels = listOf(getString(R.string.all_priorities)) + TaskPriority.values().map { it.label }
            Menus.show(anchor, labels) { index ->
                host.taskQuery().priority = options[index]
                refreshTab()
            }
        }

        tvDate.setOnClickListener { anchor ->
            val options = DateFilter.values()
            Menus.show(anchor, options.map { it.label }) { index ->
                host.taskQuery().date = options[index]
                refreshTab()
            }
        }

        tvSort.setOnClickListener { anchor ->
            val options = TaskSort.values()
            Menus.show(anchor, options.map { it.label }) { index ->
                host.taskQuery().sort = options[index]
                refreshTab()
            }
        }

        view.findViewById<View>(R.id.btnResetFilters).setOnClickListener {
            host.taskQuery().reset()
            refreshTab()
        }

        view.findViewById<Button>(R.id.btnAddTask).setOnClickListener { showAddTask() }

        refreshTab()
    }

    override fun refreshTab() {
        if (view == null) return
        val project = host.project()
        val query = host.taskQuery()
        val shown = query.apply(project.tasks)

        adapter.submit(shown)

        // Write the query back into the controls, because Overview can change it.
        bindingSearch = true
        if (etSearch.text.toString() != query.search) etSearch.setText(query.search)
        bindingSearch = false

        tvStatus.text = getString(R.string.filter_status, query.statusLabel)
        tvPriority.text = getString(R.string.filter_priority, query.priorityLabel)
        tvDate.text = getString(R.string.filter_date, query.date.label)
        tvSort.text = getString(R.string.filter_sort, query.sort.label)

        tvShowing.text = getString(R.string.showing_tasks, shown.size, project.tasks.size)

        paintEmptyState(project.tasks.isEmpty(), shown.isEmpty())
    }

    /**
     * Two different empty states: a project with no tasks at all, and a
     * project whose tasks are all filtered out.
     */
    private fun paintEmptyState(noTasksAtAll: Boolean, nothingShown: Boolean) {
        if (!nothingShown) {
            emptyBox.visibility = View.GONE
            listView.visibility = View.VISIBLE
            return
        }
        emptyBox.visibility = View.VISIBLE
        listView.visibility = View.GONE

        if (noTasksAtAll) {
            tvEmptyTitle.setText(R.string.no_tasks_yet)
            btnEmptyAction.setText(R.string.add_task)
            btnEmptyAction.setOnClickListener { showAddTask() }
        } else {
            tvEmptyTitle.setText(R.string.no_tasks_match)
            btnEmptyAction.setText(R.string.reset_filters)
            btnEmptyAction.setOnClickListener {
                host.taskQuery().reset()
                refreshTab()
            }
        }
    }

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

    /** The editor shows the full description, not the two-line row preview. */
    private fun showEditTask(task: TaskItem) {
        TaskFormDialog.show(
            requireContext(),
            task,
            { description, status, priority, dueDate ->
                task.description = description
                task.status = status
                task.priority = priority
                task.dueDate = dueDate
                host.onProjectChanged()
            },
            {
                host.project().tasks.removeAll { it.id == task.id }
                host.onProjectChanged()
            }
        )
    }

    companion object {
        fun newInstance(projectId: String): TasksFragment {
            val fragment = TasksFragment()
            fragment.arguments = Bundle().apply {
                putString(IntentKeys.ARG_PROJECT_ID, projectId)
            }
            return fragment
        }
    }
}
