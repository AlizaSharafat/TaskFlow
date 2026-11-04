package com.example.taskflow.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.taskflow.R
import com.example.taskflow.model.AttachmentCatalog
import com.example.taskflow.model.AttachmentLink
import com.example.taskflow.model.AttachmentType

/**
 * Feeds the attachment ListView in the Files tab.
 *
 * Image rows show a real thumbnail of the linked drawable, text rows show a
 * document icon. Preview and remove are separate click targets on the row.
 */
class AttachmentAdapter(
    private val context: Context,
    private var rows: List<AttachmentLink>,
    private val onPreview: (AttachmentLink) -> Unit,
    private val onRemove: (AttachmentLink) -> Unit
) : BaseAdapter() {

    private class ViewHolder(view: View) {
        val thumb: ImageView = view.findViewById(R.id.ivAttachmentThumb)
        val name: TextView = view.findViewById(R.id.tvAttachmentName)
        val meta: TextView = view.findViewById(R.id.tvAttachmentMeta)
        val preview: TextView = view.findViewById(R.id.btnPreview)
        val remove: ImageView = view.findViewById(R.id.btnRemoveAttachment)
    }

    override fun getCount(): Int = rows.size

    override fun getItem(position: Int): AttachmentLink = rows[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun submit(newRows: List<AttachmentLink>) {
        rows = newRows
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_attachment, parent, false).also { it.tag = ViewHolder(it) }
        val holder = view.tag as ViewHolder
        val link = rows[position]
        val item = AttachmentCatalog.find(link.catalogId)

        if (item == null) {
            holder.name.text = context.getString(R.string.missing_attachment)
            holder.meta.text = ""
            holder.thumb.setImageResource(R.drawable.ic_doc)
            return view
        }

        holder.name.text = item.displayName
        holder.meta.text = context.getString(
            R.string.attachment_meta, item.type.label, item.sizeLabel
        )

        if (item.type == AttachmentType.IMAGE) {
            holder.thumb.setImageResource(item.imageRes)
            holder.thumb.setBackgroundResource(R.drawable.bg_thumb_image)
        } else {
            holder.thumb.setImageResource(R.drawable.ic_doc)
            holder.thumb.setBackgroundResource(R.drawable.bg_thumb_text)
        }

        holder.preview.setOnClickListener { onPreview(link) }
        holder.remove.setOnClickListener { onRemove(link) }
        return view
    }
}
