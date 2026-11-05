package com.example.taskflow.model;

import com.example.taskflow.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class AttachmentCatalog {

    private static final List<CatalogItem> ITEMS = buildItems();

    private AttachmentCatalog() {
        // Utility class, never instantiated.
    }

    private static List<CatalogItem> buildItems() {
        List<CatalogItem> items = new ArrayList<>();
        items.add(new CatalogItem("cat_sports_project_v2", "sports-projectv2.png",
                AttachmentType.IMAGE, R.drawable.att_sports_project_v2, 0, "2.4 MB"));
        items.add(new CatalogItem("cat_briefing_from_client", "briefing from client.txt",
                AttachmentType.TEXT, 0, R.raw.att_briefing_from_client, "450 KB"));
        items.add(new CatalogItem("cat_mount_asset", "mount asset.png",
                AttachmentType.IMAGE, R.drawable.att_mount_asset, 0, "1.1 MB"));
        items.add(new CatalogItem("cat_wireframe_v3", "wireframe-v3.png",
                AttachmentType.IMAGE, R.drawable.att_wireframe_v3, 0, "860 KB"));
        items.add(new CatalogItem("cat_colour_palette", "colour-palette.png",
                AttachmentType.IMAGE, R.drawable.att_colour_palette, 0, "320 KB"));
        items.add(new CatalogItem("cat_meeting_notes", "meeting-notes.txt",
                AttachmentType.TEXT, 0, R.raw.att_meeting_notes, "12 KB"));
        items.add(new CatalogItem("cat_launch_checklist", "launch-checklist.txt",
                AttachmentType.TEXT, 0, R.raw.att_launch_checklist, "9 KB"));
        return Collections.unmodifiableList(items);
    }

    public static List<CatalogItem> getItems() {
        return ITEMS;
    }

    public static CatalogItem find(String catalogId) {
        for (CatalogItem item : ITEMS) {
            if (item.getId().equals(catalogId)) {
                return item;
            }
        }
        return null;
    }


    public static List<CatalogItem> unlinkedFor(Project project) {
        List<CatalogItem> available = new ArrayList<>();
        for (CatalogItem item : ITEMS) {
            boolean alreadyLinked = false;
            for (AttachmentLink link : project.getAttachments()) {
                if (link.getCatalogId().equals(item.getId())) {
                    alreadyLinked = true;
                    break;
                }
            }
            if (!alreadyLinked) {
                available.add(item);
            }
        }
        return available;
    }
}
