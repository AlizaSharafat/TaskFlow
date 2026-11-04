package com.example.taskflow.model

import com.example.taskflow.util.DateUtils

/** Date choices in the Tasks tab. */
enum class DateFilter(val label: String) {
    ALL("All dates"),
    TODAY("Today"),
    OVERDUE("Overdue"),
    NO_DUE_DATE("No due date")
}

/** Sort choices in the Tasks tab. */
enum class TaskSort(val label: String) {
    DUE_ASC("Due date ↑"),
    DUE_DESC("Due date ↓"),
    PRIORITY("Priority"),
    RECENT("Recently added")
}

/**
 * The live search, filter and sort state of the Tasks tab.
 *
 * ProjectActivity owns one of these, which is how the tab keeps its filters
 * when the user switches away and comes back during the same visit.
 */
data class TaskQuery(
    var search: String = "",
    var status: TaskStatus? = null,
    var priority: TaskPriority? = null,
    var date: DateFilter = DateFilter.ALL,
    var sort: TaskSort = TaskSort.DUE_ASC
) {
    /** Puts all five controls back to their starting values. */
    fun reset() {
        search = ""
        status = null
        priority = null
        date = DateFilter.ALL
        sort = TaskSort.DUE_ASC
    }

    val statusLabel: String get() = status?.label ?: "All"
    val priorityLabel: String get() = priority?.label ?: "All"

    /** A task is kept only when it satisfies the search and every filter. */
    fun apply(tasks: List<TaskItem>): List<TaskItem> {
        val todayStart = DateUtils.todayStart()
        val needle = search.trim().lowercase()

        val kept = tasks.filter { task ->
            val matchesSearch =
                needle.isEmpty() || task.description.lowercase().contains(needle)
            val matchesStatus = status == null || task.status == status
            val matchesPriority = priority == null || task.priority == priority
            val matchesDate = when (date) {
                DateFilter.ALL -> true
                DateFilter.TODAY -> task.dueDate != null && DateUtils.isToday(task.dueDate!!)
                DateFilter.OVERDUE -> task.isOverdue(todayStart)
                DateFilter.NO_DUE_DATE -> task.dueDate == null
            }
            matchesSearch && matchesStatus && matchesPriority && matchesDate
        }

        return when (sort) {
            // Tasks with no due date sort last when going up, first when going down.
            TaskSort.DUE_ASC -> kept.sortedBy { it.dueDate ?: Long.MAX_VALUE }
            TaskSort.DUE_DESC -> kept.sortedByDescending { it.dueDate ?: Long.MIN_VALUE }
            TaskSort.PRIORITY -> kept.sortedBy { it.priority.ordinal }
            TaskSort.RECENT -> kept.sortedByDescending { it.createdAt }
        }
    }
}

/** Type choices in the Files tab. */
enum class FileTypeFilter(val label: String) {
    ALL("All"),
    IMAGES("Images"),
    TEXT("Text")
}

/** The live search and type state of the Files tab, owned by ProjectActivity. */
data class FileQuery(
    var search: String = "",
    var type: FileTypeFilter = FileTypeFilter.ALL
) {
    fun reset() {
        search = ""
        type = FileTypeFilter.ALL
    }

    /** Keeps link order, filters by display name and by type. */
    fun apply(links: List<AttachmentLink>): List<AttachmentLink> {
        val needle = search.trim().lowercase()
        return links.filter { link ->
            val item = AttachmentCatalog.find(link.catalogId) ?: return@filter false
            val matchesSearch =
                needle.isEmpty() || item.displayName.lowercase().contains(needle)
            val matchesType = when (type) {
                FileTypeFilter.ALL -> true
                FileTypeFilter.IMAGES -> item.type == AttachmentType.IMAGE
                FileTypeFilter.TEXT -> item.type == AttachmentType.TEXT
            }
            matchesSearch && matchesType
        }
    }
}
