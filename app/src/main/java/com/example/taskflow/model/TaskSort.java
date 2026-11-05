package com.example.taskflow.model;

public enum TaskSort {

    DUE_ASC("Due date (earliest)"),
    DUE_DESC("Due date (latest)"),
    PRIORITY("Priority"),
    RECENT("Recently added");

    private final String label;

    TaskSort(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
