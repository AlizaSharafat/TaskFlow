package com.example.taskflow.model;

/** Work state of a task. The three values always sum to the task total. */
public enum TaskStatus {

    TODO("To do"),
    IN_PROGRESS("In progress"),
    DONE("Done");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
