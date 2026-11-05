package com.example.taskflow.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public boolean removeTaskById(String taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(taskId)) {
                tasks.remove(i);
                return true;
            }
        }
        return false;
    }

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
