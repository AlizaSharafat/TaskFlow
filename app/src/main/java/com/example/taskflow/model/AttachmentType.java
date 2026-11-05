package com.example.taskflow.model;

public enum AttachmentType {

    IMAGE("Image"),
    TEXT("Document");

    private final String label;

    AttachmentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
