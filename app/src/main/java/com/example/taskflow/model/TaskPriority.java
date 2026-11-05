package com.example.taskflow.model;

/** How urgent a task is. Declared high to low so ordinal() can sort them. */
public enum TaskPriority {

    HIGH("High"),
    NORMAL("Normal"),
    LOW("Low");

    private final String label;

    TaskPriority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
