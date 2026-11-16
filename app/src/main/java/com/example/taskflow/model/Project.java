package com.example.taskflow.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A project and everything inside it.
 *
 * This whole object travels between Activities inside an Intent, which is why
 * it implements Serializable. Every count below is derived from the task list
 * rather than stored, so the numbers can never drift out of step.
 */
public class Project implements Serializable {

    private final String id;
    private final ArrayList<AttachmentLink> attachments;
    private final ArrayList<TaskItem> tasks;

    private String tag;
    private String name;
    private String description;
    private int commentCount;

    public Project(String tag, String name, String description) {
        this.id = UUID.randomUUID().toString();
        this.tag = tag;
        this.name = name;
        this.description = description;
        this.attachments = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.commentCount = 0;
    }

    public String getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AttachmentLink> getAttachments() {
        return attachments;
    }

    public List<TaskItem> getTasks() {
        return tasks;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getTotalTaskCount() {
        return tasks.size();
    }

    public int getDoneTaskCount() {
        return countByStatus(TaskStatus.DONE);
    }

    public int getToDoCount() {
        return countByStatus(TaskStatus.TODO);
    }

    public int getInProgressCount() {
        return countByStatus(TaskStatus.IN_PROGRESS);
    }

    private int countByStatus(TaskStatus wanted) {
        int total = 0;
        for (TaskItem task : tasks) {
            if (task.getStatus() == wanted) {
                total++;
            }
        }
        return total;
    }

    /** Overdue is a subset of the unfinished tasks, never added to the other counts. */
    public int getOverdueCount(long todayStart) {
        int total = 0;
        for (TaskItem task : tasks) {
            if (task.isOverdue(todayStart)) {
                total++;
            }
        }
        return total;
    }

    public int getAttachmentCount() {
        return attachments.size();
    }

    /** Done divided by total as a whole number. An empty project is 0%. */
    public int getProgressPercent() {
        if (tasks.isEmpty()) {
            return 0;
        }
        return getDoneTaskCount() * 100 / tasks.size();
    }

    public TaskItem findTask(String taskId) {
        for (TaskItem task : tasks) {
            if (task.getId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    /** Removes a task by id. Never by description, because duplicates are allowed. */
    public boolean removeTaskById(String taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(taskId)) {
                tasks.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Moves one task so that it takes the place of another.
     *
     * Dragging happens on the visible rows, which may be a filtered subset, so
     * the move is expressed as "put this task where that one is" and applied to
     * the real collection by id. Everything else keeps its relative order, so
     * tasks that are currently filtered out are not disturbed.
     */
    public boolean moveTask(String movingId, String targetId) {
        int from = indexOfTask(movingId);
        int to = indexOfTask(targetId);
        if (from < 0 || to < 0 || from == to) {
            return false;
        }
        TaskItem moving = tasks.remove(from);
        tasks.add(to, moving);
        return true;
    }

    private int indexOfTask(String taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(taskId)) {
                return i;
            }
        }
        return -1;
    }

    /** Removes an attachment link by its own id, not by catalog id. */
    public boolean removeAttachmentById(String linkId) {
        for (int i = 0; i < attachments.size(); i++) {
            if (attachments.get(i).getId().equals(linkId)) {
                attachments.remove(i);
                return true;
            }
        }
        return false;
    }
}
