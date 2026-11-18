package com.example.taskflow.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The live search and type state of the Files tab, owned by ProjectActivity. */
public class FileQuery {

    private String search = "";
    private FileTypeFilter type = FileTypeFilter.ALL;

    public void reset() {
        search = "";
        type = FileTypeFilter.ALL;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search == null ? "" : search;
    }

    public FileTypeFilter getType() {
        return type;
    }

    public void setType(FileTypeFilter type) {
        this.type = type;
    }

    /** Keeps link-insertion order, filters by display name and by type. */
    public List<AttachmentLink> apply(List<AttachmentLink> links) {
        String needle = search.trim().toLowerCase(Locale.getDefault());
        List<AttachmentLink> kept = new ArrayList<>();

        for (AttachmentLink link : links) {
            CatalogItem item = AttachmentCatalog.find(link.getCatalogId());
            if (item == null) {
                continue;
            }
            boolean matchesSearch = needle.isEmpty()
                    || item.getDisplayName().toLowerCase(Locale.getDefault()).contains(needle);
            if (!matchesSearch) {
                continue;
            }
            if (type == FileTypeFilter.IMAGES && item.getType() != AttachmentType.IMAGE) {
                continue;
            }
            if (type == FileTypeFilter.TEXT && item.getType() != AttachmentType.TEXT) {
                continue;
            }
            kept.add(link);
        }
        return kept;
    }
}
