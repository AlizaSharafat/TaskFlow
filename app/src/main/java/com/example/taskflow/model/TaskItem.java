package com.example.taskflow.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * One task inside a project.
 *
 * dueDate holds the first millisecond of the due day, or null when the task
 * has no due date. createdAt lets tasks be sorted by when they were added.
 */
public class TaskItem implements Serializable {

    private final String id;
    private final long createdAt;

    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long dueDate;

    public TaskItem(String description, TaskStatus status, TaskPriority priority, Long dueDate) {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    /** A new task defaults to To do, Normal and no due date. */
    public TaskItem(String description) {
        this(description, TaskStatus.TODO, TaskPriority.NORMAL, null);
    }

    public String getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDone() {
        return status == TaskStatus.DONE;
    }

    /** Overdue means the due day is before today AND the task is not finished. */
    public boolean isOverdue(long todayStart) {
        if (dueDate == null) {
            return false;
        }
        return dueDate < todayStart && status != TaskStatus.DONE;
    }
}
