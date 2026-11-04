package com.example.taskflow.model

import com.example.taskflow.R

/**
 * One item in the shared attachment catalog.
 *
 * Images point at a drawable resource, text documents at a raw resource.
 * Exactly one of imageRes and textRes is set, decided by [type].
 */
data class CatalogItem(
    val id: String,
    val displayName: String,
    val type: AttachmentType,
    val imageRes: Int = 0,
    val textRes: Int = 0,
    val sizeLabel: String = ""
)

/**
 * The fixed catalog every project links its attachments from.
 *
 * A project stores only the catalog id, so the same file can be linked by
 * several projects without copying any bytes around.
 */
object AttachmentCatalog {

    val items: List<CatalogItem> = listOf(
        CatalogItem(
            id = "cat_sports_project_v2",
            displayName = "sports-projectv2.png",
            type = AttachmentType.IMAGE,
            imageRes = R.drawable.att_sports_project_v2,
            sizeLabel = "2.4 MB"
        ),
        CatalogItem(
            id = "cat_briefing_from_client",
            displayName = "briefing from client.txt",
            type = AttachmentType.TEXT,
            textRes = R.raw.att_briefing_from_client,
            sizeLabel = "450 KB"
        ),
        CatalogItem(
            id = "cat_mount_asset",
            displayName = "mount asset.png",
            type = AttachmentType.IMAGE,
            imageRes = R.drawable.att_mount_asset,
            sizeLabel = "1.1 MB"
        ),
        CatalogItem(
            id = "cat_wireframe_v3",
            displayName = "wireframe-v3.png",
            type = AttachmentType.IMAGE,
            imageRes = R.drawable.att_wireframe_v3,
            sizeLabel = "860 KB"
        ),
        CatalogItem(
            id = "cat_colour_palette",
            displayName = "colour-palette.png",
            type = AttachmentType.IMAGE,
            imageRes = R.drawable.att_colour_palette,
            sizeLabel = "320 KB"
        ),
        CatalogItem(
            id = "cat_meeting_notes",
            displayName = "meeting-notes.txt",
            type = AttachmentType.TEXT,
            textRes = R.raw.att_meeting_notes,
            sizeLabel = "12 KB"
        ),
        CatalogItem(
            id = "cat_launch_checklist",
            displayName = "launch-checklist.txt",
            type = AttachmentType.TEXT,
            textRes = R.raw.att_launch_checklist,
            sizeLabel = "9 KB"
        )
    )

    fun find(catalogId: String): CatalogItem? = items.firstOrNull { it.id == catalogId }

    /** Catalog items this project has not linked yet, in catalog order. */
    fun unlinkedFor(project: Project): List<CatalogItem> {
        val linked = project.attachments.map { it.catalogId }.toSet()
        return items.filter { it.id !in linked }
    }
}
