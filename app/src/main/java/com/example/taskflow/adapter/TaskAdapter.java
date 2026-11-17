package com.example.taskflow.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskflow.R;
import com.example.taskflow.model.TaskGroup;
import com.example.taskflow.model.TaskItem;
import com.example.taskflow.model.TaskRow;
import com.example.taskflow.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The one adapter behind the one RecyclerView in the Tasks tab.
 *
 * The screen looks like four lists stacked up, but it is a single list of rows
 * of two different shapes. getItemViewType reports which shape each row is,
 * onCreateViewHolder inflates the matching layout, and onBindViewHolder sends
 * the row to the matching ViewHolder. Four separate RecyclerViews would not
 * scroll as one and could not hand a task from one group to another.
 *
 * Updates go through DiffUtil, so notifyDataSetChanged is never called.
 */
public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnToggleDone {
        void onToggled(TaskItem task, boolean isChecked);
    }

    public interface OnTaskClick {
        void onClick(TaskItem task);
    }

    private final Context context;
    private final OnToggleDone onToggleDone;
    private final OnTaskClick onTaskClick;
    private final List<TaskRow> rows = new ArrayList<>();

    public TaskAdapter(Context context, OnToggleDone onToggleDone, OnTaskClick onTaskClick) {
        this.context = context;
        this.onToggleDone = onToggleDone;
        this.onTaskClick = onTaskClick;
    }

    /**
     * Swaps in a freshly grouped list and lets DiffUtil work out the smallest
     * set of changes that gets the screen from the old list to the new one.
     */
    public void submit(List<TaskRow> newRows) {
        DiffUtil.DiffResult result =
                DiffUtil.calculateDiff(new RowDiff(new ArrayList<>(rows), newRows));
        rows.clear();
        rows.addAll(newRows);
        result.dispatchUpdatesTo(this);
    }

    /** Null rather than an exception, because a gesture can outlive a row. */
    public TaskRow rowAt(int position) {
        if (position < 0 || position >= rows.size()) {
            return null;
        }
        return rows.get(position);
    }

    /**
     * Moves a row inside the adapter's own list while a drag is in progress.
     *
     * This keeps the adapter and the screen in step frame by frame. The real
     * collection is reordered separately, and the list is rebuilt from it once
     * the finger lifts.
     */
    public void moveRow(int from, int to) {
        if (from < 0 || to < 0 || from >= rows.size() || to >= rows.size() || from == to) {
            return;
        }
        TaskRow moving = rows.remove(from);
        rows.add(to, moving);
        notifyItemMoved(from, to);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    /** This is what lets one adapter draw two different kinds of line. */
    @Override
    public int getItemViewType(int position) {
        return rows.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TaskRow.TYPE_HEADER) {
            return new HeaderViewHolder(
                    inflater.inflate(R.layout.item_task_header, parent, false));
        }
        return new TaskViewHolder(inflater.inflate(R.layout.item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskRow row = rows.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(row);
        } else {
            ((TaskViewHolder) holder).bind(context, row.getTask(), onToggleDone, onTaskClick);
        }
    }

    /** Each group gets its own colour dot, matching the Overview breakdown. */
    private static int dotFor(TaskGroup group) {
        switch (group) {
            case OVERDUE:
                return R.drawable.dot_overdue;
            case IN_PROGRESS:
                return R.drawable.dot_in_progress;
            case DONE:
                return R.drawable.dot_done;
            default:
                return R.drawable.dot_todo;
        }
    }

    // ---- View holders ----

    /** A section heading: the group name and how many tasks sit under it. */
    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final ImageView dot;
        private final TextView name;
        private final TextView count;

        HeaderViewHolder(@NonNull View view) {
            super(view);
            dot = view.findViewById(R.id.ivGroupDot);
            name = view.findViewById(R.id.tvGroupName);
            count = view.findViewById(R.id.tvGroupCount);
        }

        void bind(TaskRow row) {
            dot.setImageResource(dotFor(row.getGroup()));
            name.setText(row.getGroup().getLabel());
            count.setText(String.valueOf(row.getCount()));
        }
    }

    /**
     * A task row.
     *
     * The checkbox listener is detached before setChecked and reattached
     * afterwards. Without that, recycling a row would fire the listener left
     * behind by the previous task and quietly mark the wrong one done.
     */
    static class TaskViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox check;
        private final TextView description;
        private final TextView meta;
        private final TextView overdue;

        TaskViewHolder(@NonNull View view) {
            super(view);
            check = view.findViewById(R.id.cbTask);
            description = view.findViewById(R.id.tvTaskDescription);
            meta = view.findViewById(R.id.tvTaskMeta);
            overdue = view.findViewById(R.id.tvOverdueBadge);
        }

        void bind(Context context, final TaskItem task,
                  final OnToggleDone onToggleDone, final OnTaskClick onTaskClick) {

            long todayStart = DateUtils.todayStart();
            boolean isOverdue = task.isOverdue(todayStart);

            description.setText(task.getDescription());
            meta.setText(buildMeta(context, task));

            // A done row stays readable: struck through and muted, never hidden.
            if (task.isDone()) {
                description.setPaintFlags(
                        description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                description.setPaintFlags(
                        description.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
            description.setTextColor(ContextCompat.getColor(
                    context, task.isDone() ? R.color.ink_soft : R.color.ink));

            int metaColour;
            if (isOverdue) {
                metaColour = R.color.danger;
            } else if (task.getDueDate() != null && DateUtils.isToday(task.getDueDate())) {
                metaColour = R.color.warning;
            } else {
                metaColour = R.color.ink_muted;
            }
            meta.setTextColor(ContextCompat.getColor(context, metaColour));

            overdue.setVisibility(isOverdue ? View.VISIBLE : View.GONE);

            check.setOnCheckedChangeListener(null);
            check.setChecked(task.isDone());
            check.setOnCheckedChangeListener(
                    (button, isChecked) -> onToggleDone.onToggled(task, isChecked));

            itemView.setOnClickListener(v -> onTaskClick.onClick(task));
        }

        /** Builds the "To do • High • Due today" supporting line. */
        private String buildMeta(Context context, TaskItem task) {
            String dueLabel;
            if (task.getDueDate() == null) {
                dueLabel = context.getString(R.string.no_due_date);
            } else if (DateUtils.isToday(task.getDueDate())) {
                dueLabel = context.getString(R.string.due_today);
            } else {
                dueLabel = DateUtils.format(task.getDueDate());
            }
            return context.getString(R.string.task_meta,
                    task.getStatus().getLabel(), task.getPriority().getLabel(), dueLabel);
        }
    }

    // ---- Diff ----

    /**
     * Teaches DiffUtil how to compare two grouped lists.
     *
     * Headers are identified by their group and tasks by their id, so a task
     * that moves from the To do section to the Done section is recognised as
     * the same task moving rather than one disappearing and another arriving.
     */
    private static class RowDiff extends DiffUtil.Callback {

        private final List<TaskRow> oldRows;
        private final List<TaskRow> newRows;

        RowDiff(List<TaskRow> oldRows, List<TaskRow> newRows) {
            this.oldRows = oldRows;
            this.newRows = newRows;
        }

        @Override
        public int getOldListSize() {
            return oldRows.size();
        }

        @Override
        public int getNewListSize() {
            return newRows.size();
        }

        @Override
        public boolean areItemsTheSame(int oldPosition, int newPosition) {
            TaskRow older = oldRows.get(oldPosition);
            TaskRow newer = newRows.get(newPosition);
            if (older.getType() != newer.getType()) {
                return false;
            }
            if (older.isHeader()) {
                return older.getGroup() == newer.getGroup();
            }
            return older.getTask().getId().equals(newer.getTask().getId());
        }

        @Override
        public boolean areContentsTheSame(int oldPosition, int newPosition) {
            return oldRows.get(oldPosition).sameContentAs(newRows.get(newPosition));
        }
    }
}
