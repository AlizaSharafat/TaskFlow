package com.example.taskflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskflow.R;
import com.example.taskflow.model.Project;

/**
 * Feeds the project RecyclerView on the Home screen.
 *
 * This extends ListAdapter rather than plain RecyclerView.Adapter. ListAdapter
 * keeps the current list itself and, every time a new list is submitted, runs
 * DiffUtil to work out exactly which rows changed. That is why
 * notifyDataSetChanged is never called anywhere in this class: redrawing the
 * whole list would throw away the scroll position and the row animations, and
 * would repaint four rows that did not change to update the one that did.
 */
public class ProjectAdapter extends ListAdapter<Project, ProjectAdapter.ProjectViewHolder> {

    /** Java has no function type, so the row click is a small interface. */
    public interface OnProjectClick {
        void onClick(Project project);
    }

    /**
     * How DiffUtil compares two versions of the list.
     *
     * areItemsTheSame asks "is this the same project?", which only the unique
     * id can answer: names can be edited and two projects may share one.
     *
     * areContentsTheSame asks "does the row need redrawing?" and compares only
     * the values the row actually puts on screen. The percentage is left out
     * because it is derived from the done and total counts already compared.
     */
    private static final DiffUtil.ItemCallback<Project> DIFF =
            new DiffUtil.ItemCallback<Project>() {

                @Override
                public boolean areItemsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Project oldItem,
                                                  @NonNull Project newItem) {
                    return oldItem.getTag().equals(newItem.getTag())
                            && oldItem.getName().equals(newItem.getName())
                            && oldItem.getDescription().equals(newItem.getDescription())
                            && oldItem.getDoneTaskCount() == newItem.getDoneTaskCount()
                            && oldItem.getTotalTaskCount() == newItem.getTotalTaskCount()
                            && oldItem.getAttachmentCount() == newItem.getAttachmentCount()
                            && oldItem.getCommentCount() == newItem.getCommentCount();
                }
            };

    private final OnProjectClick onProjectClick;

    public ProjectAdapter(OnProjectClick onProjectClick) {
        super(DIFF);
        this.onProjectClick = onProjectClick;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(getItem(position), onProjectClick);
    }

    /**
     * Holds the row's views so findViewById runs once per inflated row instead
     * of once per bind. RecyclerView makes this compulsory rather than optional:
     * the ViewHolder is the unit it recycles.
     */
    static class ProjectViewHolder extends RecyclerView.ViewHolder {

        private final TextView tag;
        private final TextView name;
        private final TextView description;
        private final TextView taskCount;
        private final TextView percent;
        private final ProgressBar progress;
        private final TextView attachments;
        private final TextView comments;

        ProjectViewHolder(@NonNull View view) {
            super(view);
            tag = view.findViewById(R.id.tvProjectTag);
            name = view.findViewById(R.id.tvProjectName);
            description = view.findViewById(R.id.tvProjectDescription);
            taskCount = view.findViewById(R.id.tvTaskProgress);
            percent = view.findViewById(R.id.tvProgressPercent);
            progress = view.findViewById(R.id.progressProject);
            attachments = view.findViewById(R.id.tvAttachmentCount);
            comments = view.findViewById(R.id.tvCommentCount);
        }

        void bind(final Project project, final OnProjectClick onProjectClick) {
            tag.setText(project.getTag());
            name.setText(project.getName());
            description.setText(project.getDescription());

            int done = project.getDoneTaskCount();
            int total = project.getTotalTaskCount();
            int percentValue = project.getProgressPercent();

            taskCount.setText(itemView.getContext()
                    .getString(R.string.task_progress, done, total));
            percent.setText(itemView.getContext()
                    .getString(R.string.percent_value, percentValue));
            progress.setProgress(percentValue);

            int attachmentCount = project.getAttachmentCount();
            int commentCount = project.getCommentCount();
            attachments.setText(itemView.getResources()
                    .getQuantityString(R.plurals.attachment_count, attachmentCount,
                            attachmentCount));
            comments.setText(itemView.getResources()
                    .getQuantityString(R.plurals.comment_count, commentCount, commentCount));

            // The project is captured rather than the position: a position can
            // go stale between binding and the tap, an object cannot.
            itemView.setOnClickListener(v -> onProjectClick.onClick(project));
        }
    }
}
