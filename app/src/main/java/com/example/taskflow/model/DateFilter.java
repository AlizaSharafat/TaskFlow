package com.example.taskflow.model;

public enum DateFilter {

    ALL("All dates"),
    TODAY("Today"),
    OVERDUE("Overdue"),
    NO_DUE_DATE("No due date");

    private final String label;

    DateFilter(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
