package com.example.taskflow.model;

/**
 * One line in the grouped task list: either a section header or a task.
 *
 * The Tasks tab uses a single RecyclerView with a single adapter, so both kinds
 * of line travel in the same list and getItemViewType tells them apart by the
 * type held here.
 *
 * A task row also keeps a copy of the values it was built from. The TaskItem
 * object is edited in place, so comparing a task with itself would never show
 * a change: DiffUtil compares these copies instead, which is how a task that
 * was just marked Done is redrawn rather than silently left struck out.
 */
public class TaskRow {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TASK = 1;

    private final int type;
    private final TaskGroup group;

    /** Header rows only: how many tasks sit under this heading. */
    private final int count;

    /** Task rows only: the live task the row is bound to. */
    private final TaskItem task;

    // The snapshot DiffUtil compares. Header rows leave these empty.
    private final String description;
    private final TaskStatus status;
    private final TaskPriority priority;
    private final Long dueDate;
    private final boolean overdue;

    private TaskRow(int type, TaskGroup group, int count, TaskItem task, boolean overdue) {
        this.type = type;
        this.group = group;
        this.count = count;
        this.task = task;
        this.overdue = overdue;
        if (task == null) {
            this.description = null;
            this.status = null;
            this.priority = null;
            this.dueDate = null;
        } else {
            this.description = task.getDescription();
            this.status = task.getStatus();
            this.priority = task.getPriority();
            this.dueDate = task.getDueDate();
        }
    }

    public static TaskRow header(TaskGroup group, int count) {
        return new TaskRow(TYPE_HEADER, group, count, null, false);
    }

    public static TaskRow task(TaskGroup group, TaskItem task, long todayStart) {
        return new TaskRow(TYPE_TASK, group, 0, task, task.isOverdue(todayStart));
    }

    public int getType() {
        return type;
    }

    public boolean isHeader() {
        return type == TYPE_HEADER;
    }

    public TaskGroup getGroup() {
        return group;
    }

    public int getCount() {
        return count;
    }

    public TaskItem getTask() {
        return task;
    }

    /** True when this row shows the same thing as the given one. */
    public boolean sameContentAs(TaskRow other) {
        if (other == null || type != other.type) {
            return false;
        }
        if (isHeader()) {
            return group == other.group && count == other.count;
        }
        return overdue == other.overdue
                && status == other.status
                && priority == other.priority
                && equalText(description, other.description)
                && equalDate(dueDate, other.dueDate);
    }

    private static boolean equalText(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static boolean equalDate(Long a, Long b) {
        return a == null ? b == null : a.equals(b);
    }
}
