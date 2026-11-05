package com.example.taskflow.model;

import java.io.Serializable;
import java.util.UUID;


public class AttachmentLink implements Serializable {

    private final String id;
    private final String catalogId;

    public AttachmentLink(String catalogId) {
        this.id = UUID.randomUUID().toString();
        this.catalogId = catalogId;
    }

    public String getId() {
        return id;
    }

    public String getCatalogId() {
        return catalogId;
    }
}
