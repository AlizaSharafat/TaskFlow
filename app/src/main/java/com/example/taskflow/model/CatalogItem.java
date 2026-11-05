package com.example.taskflow.model;


public class CatalogItem {

    private final String id;
    private final String displayName;
    private final AttachmentType type;
    private final int imageRes;
    private final int textRes;
    private final String sizeLabel;

    public CatalogItem(String id, String displayName, AttachmentType type,
                       int imageRes, int textRes, String sizeLabel) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.imageRes = imageRes;
        this.textRes = textRes;
        this.sizeLabel = sizeLabel;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AttachmentType getType() {
        return type;
    }

    public int getImageRes() {
        return imageRes;
    }

    public int getTextRes() {
        return textRes;
    }

    public String getSizeLabel() {
        return sizeLabel;
    }
}
