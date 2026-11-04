package com.example.taskflow.model

import java.io.Serializable
import java.util.UUID

/** Work state of a task. The three values must always sum to the task total. */
enum class TaskStatus(val label: String) {
    TODO("To do"),
    IN_PROGRESS("In progress"),
    DONE("Done")
}

/** How urgent a task is. */
enum class TaskPriority(val label: String) {
    HIGH("High"),
    NORMAL("Normal"),
    LOW("Low")
}

/** What kind of content a catalog item holds. */
enum class AttachmentType(val label: String) {
    IMAGE("Image"),
    TEXT("Document")
}

/**
 * One task inside a project.
 *
 * dueDate is the start of the due day in epoch milliseconds, or null for no due date.
 * createdAt is kept so tasks can be sorted by when they were added.
 */
data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    var description: String,
    var status: TaskStatus = TaskStatus.TODO,
    var priority: TaskPriority = TaskPriority.NORMAL,
    var dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {

    val isDone: Boolean get() = status == TaskStatus.DONE

    /** Overdue means the due day is before today AND the task is not finished. */
    fun isOverdue(todayStart: Long): Boolean {
        val due = dueDate ?: return false
        return due < todayStart && status != TaskStatus.DONE
    }
}

/**
 * A link between a project and one item in the attachment catalog.
 * Only the catalog id travels, never the image bytes.
 */
data class AttachmentLink(
    val id: String = UUID.randomUUID().toString(),
    val catalogId: String
) : Serializable

/**
 * A project and everything inside it. This whole object travels between
 * Activities inside an Intent, which is why it implements Serializable.
 */
data class Project(
    val id: String = UUID.randomUUID().toString(),
    var tag: String,
    var name: String,
    var description: String,
    var attachments: MutableList<AttachmentLink> = mutableListOf(),
    var tasks: MutableList<TaskItem> = mutableListOf(),
    var commentCount: Int = 0
) : Serializable {

    fun totalTaskCount(): Int = tasks.size

    fun doneTaskCount(): Int = tasks.count { it.status == TaskStatus.DONE }

    fun toDoCount(): Int = tasks.count { it.status == TaskStatus.TODO }

    fun inProgressCount(): Int = tasks.count { it.status == TaskStatus.IN_PROGRESS }

    /** Overdue is a subset of unfinished tasks, so it is never added to the other counts. */
    fun overdueCount(todayStart: Long): Int = tasks.count { it.isOverdue(todayStart) }

    fun attachmentCount(): Int = attachments.size

    /** Done divided by total as a whole number. An empty project is 0%. */
    fun progressPercent(): Int {
        if (tasks.isEmpty()) return 0
        return doneTaskCount() * 100 / tasks.size
    }

    fun findTask(taskId: String): TaskItem? = tasks.firstOrNull { it.id == taskId }
}
