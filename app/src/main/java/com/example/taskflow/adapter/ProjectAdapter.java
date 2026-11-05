package com.example.taskflow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.taskflow.R;
import com.example.taskflow.model.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * Feeds the project ListView on the Home screen.
 *
 * A BaseAdapter is used rather than ArrayAdapter because every row binds eight
 * different views, and the ViewHolder keeps findViewById out of the scroll.
 */
public class ProjectAdapter extends BaseAdapter {

    private final Context context;
    private List<Project> rows;

    public ProjectAdapter(Context context, List<Project> rows) {
        this.context = context;
        this.rows = new ArrayList<>(rows);
    }

    public void submit(List<Project> newRows) {
        this.rows = new ArrayList<>(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Project getItem(int position) {
        return rows.get(position);
    }

    /** Stable ids are not used, so the ListView falls back to position. */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_project, parent, false);
            view.setTag(new ViewHolder(view));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        Project project = rows.get(position);

        holder.tag.setText(project.getTag());
        holder.name.setText(project.getName());
        holder.description.setText(project.getDescription());

        int done = project.getDoneTaskCount();
        int total = project.getTotalTaskCount();
        int percent = project.getProgressPercent();

        holder.taskCount.setText(context.getString(R.string.task_progress, done, total));
        holder.percent.setText(context.getString(R.string.percent_value, percent));
        holder.progress.setProgress(percent);

        int attachments = project.getAttachmentCount();
        int comments = project.getCommentCount();
        holder.attachments.setText(context.getResources()
                .getQuantityString(R.plurals.attachment_count, attachments, attachments));
        holder.comments.setText(context.getResources()
                .getQuantityString(R.plurals.comment_count, comments, comments));

        return view;
    }

    /** Holds the row's views so they are looked up once per inflated row. */
    private static class ViewHolder {

        final TextView tag;
        final TextView name;
        final TextView description;
        final TextView taskCount;
        final TextView percent;
        final ProgressBar progress;
        final TextView attachments;
        final TextView comments;

        ViewHolder(View view) {
            tag = view.findViewById(R.id.tvProjectTag);
            name = view.findViewById(R.id.tvProjectName);
            description = view.findViewById(R.id.tvProjectDescription);
            taskCount = view.findViewById(R.id.tvTaskProgress);
            percent = view.findViewById(R.id.tvProgressPercent);
            progress = view.findViewById(R.id.progressProject);
            attachments = view.findViewById(R.id.tvAttachmentCount);
            comments = view.findViewById(R.id.tvCommentCount);
        }
    }
}
