package com.example.taskflow.model;

/**
 * Sort choices in the Tasks tab.
 *
 * MANUAL means "leave the tasks in the order the project holds them". It is
 * what dragging a row switches to, because a hand-made order and an automatic
 * sort cannot both be in charge of the same list.
 */
public enum TaskSort {

    DUE_ASC("Due date (earliest)"),
    DUE_DESC("Due date (latest)"),
    PRIORITY("Priority"),
    RECENT("Recently added"),
    MANUAL("Manual order");

    private final String label;

    TaskSort(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
