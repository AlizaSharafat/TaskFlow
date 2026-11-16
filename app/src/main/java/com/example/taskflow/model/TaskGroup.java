package com.example.taskflow.model;

/**
 * The four sections the Tasks tab is divided into, in the order they appear.
 *
 * Overdue is not a TaskStatus: it is worked out from the due date. A task that
 * is To do and past its due date therefore belongs to Overdue and not to To do,
 * which means the four groups never overlap and every task lands in exactly
 * one of them.
 *
 * The enum holds no drawable ids on purpose, so the model package never has to
 * import the generated R class. TaskAdapter maps a group to its colour dot.
 */
public enum TaskGroup {

    OVERDUE("Overdue"),
    IN_PROGRESS("In progress"),
    TODO("To do"),
    DONE("Done");

    private final String label;

    TaskGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Decides which section a task belongs to.
     *
     * Overdue is tested first, so it wins over the task's own status. A Done
     * task is never overdue, because isOverdue already excludes finished work.
     */
    public static TaskGroup of(TaskItem task, long todayStart) {
        if (task.isOverdue(todayStart)) {
            return OVERDUE;
        }
        switch (task.getStatus()) {
            case IN_PROGRESS:
                return IN_PROGRESS;
            case DONE:
                return DONE;
            default:
                return TODO;
        }
    }
}
