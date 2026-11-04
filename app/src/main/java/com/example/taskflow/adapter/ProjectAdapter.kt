package com.example.taskflow.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.example.taskflow.R
import com.example.taskflow.model.Project

/**
 * Feeds the project ListView on the Home screen.
 *
 * A BaseAdapter is used rather than ArrayAdapter because every row binds
 * eight different views, and the ViewHolder keeps findViewById out of scroll.
 */
class ProjectAdapter(
    private val context: Context,
    private var rows: List<Project>
) : BaseAdapter() {

    private class ViewHolder(view: View) {
        val tag: TextView = view.findViewById(R.id.tvProjectTag)
        val name: TextView = view.findViewById(R.id.tvProjectName)
        val description: TextView = view.findViewById(R.id.tvProjectDescription)
        val taskCount: TextView = view.findViewById(R.id.tvTaskProgress)
        val percent: TextView = view.findViewById(R.id.tvProgressPercent)
        val progress: ProgressBar = view.findViewById(R.id.progressProject)
        val attachments: TextView = view.findViewById(R.id.tvAttachmentCount)
        val comments: TextView = view.findViewById(R.id.tvCommentCount)
    }

    override fun getCount(): Int = rows.size

    override fun getItem(position: Int): Project = rows[position]

    /** Stable ids are not used, so the ListView falls back to position. */
    override fun getItemId(position: Int): Long = position.toLong()

    fun submit(newRows: List<Project>) {
        rows = newRows
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_project, parent, false).also { it.tag = ViewHolder(it) }
        val holder = view.tag as ViewHolder
        val project = rows[position]

        holder.tag.text = project.tag
        holder.name.text = project.name
        holder.description.text = project.description

        val done = project.doneTaskCount()
        val total = project.totalTaskCount()
        val percent = project.progressPercent()

        holder.taskCount.text = context.getString(R.string.task_progress, done, total)
        holder.percent.text = context.getString(R.string.percent_value, percent)
        holder.progress.progress = percent

        holder.attachments.text = context.resources.getQuantityString(
            R.plurals.attachment_count, project.attachmentCount(), project.attachmentCount()
        )
        holder.comments.text = context.resources.getQuantityString(
            R.plurals.comment_count, project.commentCount, project.commentCount
        )
        return view
    }
}
