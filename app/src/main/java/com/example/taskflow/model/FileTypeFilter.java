package com.example.taskflow.model;


public enum FileTypeFilter {

    ALL("All"),
    IMAGES("Images"),
    TEXT("Text");

    private final String label;

    FileTypeFilter(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
