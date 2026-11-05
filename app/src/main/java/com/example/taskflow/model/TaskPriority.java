package com.example.taskflow.model;

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
