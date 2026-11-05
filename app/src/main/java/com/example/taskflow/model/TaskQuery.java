package com.example.taskflow.model;

import com.example.taskflow.util.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * The live search, filter and sort state of the Tasks tab.
 *
 * ProjectActivity owns one of these, which is how the tab keeps its filters
 * when the user switches away and comes back during the same visit.
 */
public class TaskQuery {

    private String search = "";
    private TaskStatus status;      // null means All statuses
    private TaskPriority priority;  // null means All priorities
    private DateFilter date = DateFilter.ALL;
    private TaskSort sort = TaskSort.DUE_ASC;

    /** Puts all five controls back to their starting values. */
    public void reset() {
        search = "";
        status = null;
        priority = null;
        date = DateFilter.ALL;
        sort = TaskSort.DUE_ASC;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search == null ? "" : search;
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

    public DateFilter getDate() {
        return date;
    }

    public void setDate(DateFilter date) {
        this.date = date;
    }

    public TaskSort getSort() {
        return sort;
    }

    public void setSort(TaskSort sort) {
        this.sort = sort;
    }

    public String getStatusLabel() {
        return status == null ? "All" : status.getLabel();
    }

    public String getPriorityLabel() {
        return priority == null ? "All" : priority.getLabel();
    }

    /** A task is kept only when it satisfies the search and every filter. */
    public List<TaskItem> apply(List<TaskItem> tasks) {
        long todayStart = DateUtils.todayStart();
        String needle = search.trim().toLowerCase(Locale.getDefault());

        List<TaskItem> kept = new ArrayList<>();
        for (TaskItem task : tasks) {
            if (matches(task, needle, todayStart)) {
                kept.add(task);
            }
        }
        sortRows(kept);
        return kept;
    }

    private boolean matches(TaskItem task, String needle, long todayStart) {
        boolean matchesSearch = needle.isEmpty()
                || task.getDescription().toLowerCase(Locale.getDefault()).contains(needle);
        if (!matchesSearch) {
            return false;
        }
        if (status != null && task.getStatus() != status) {
            return false;
        }
        if (priority != null && task.getPriority() != priority) {
            return false;
        }
        switch (date) {
            case TODAY:
                return task.getDueDate() != null && DateUtils.isToday(task.getDueDate());
            case OVERDUE:
                return task.isOverdue(todayStart);
            case NO_DUE_DATE:
                return task.getDueDate() == null;
            default:
                return true;
        }
    }

    private void sortRows(List<TaskItem> rows) {
        switch (sort) {
            // Tasks with no due date sort last going up, first going down.
            case DUE_ASC:
                Collections.sort(rows, (a, b) -> Long.compare(dueOrLast(a), dueOrLast(b)));
                break;
            case DUE_DESC:
                Collections.sort(rows, (a, b) -> Long.compare(dueOrFirst(b), dueOrFirst(a)));
                break;
            case PRIORITY:
                Collections.sort(rows, (a, b) ->
                        Integer.compare(a.getPriority().ordinal(), b.getPriority().ordinal()));
                break;
            case RECENT:
                Collections.sort(rows, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                break;
            default:
                break;
        }
    }

    private static long dueOrLast(TaskItem task) {
        return task.getDueDate() == null ? Long.MAX_VALUE : task.getDueDate();
    }

    private static long dueOrFirst(TaskItem task) {
        return task.getDueDate() == null ? Long.MIN_VALUE : task.getDueDate();
    }
}
