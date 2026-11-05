package com.example.taskflow.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.taskflow.R;
import com.example.taskflow.model.TaskItem;
import com.example.taskflow.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends BaseAdapter {

    public interface OnToggleDone {
        void onToggled(TaskItem task, boolean isChecked);
    }

    private final Context context;
    private final OnToggleDone onToggleDone;
    private List<TaskItem> rows;

    public TaskAdapter(Context context, List<TaskItem> rows, OnToggleDone onToggleDone) {
        this.context = context;
        this.rows = new ArrayList<>(rows);
        this.onToggleDone = onToggleDone;
    }

    public void submit(List<TaskItem> newRows) {
        this.rows = new ArrayList<>(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public TaskItem getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
            view.setTag(new ViewHolder(view));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        final TaskItem task = rows.get(position);

        long todayStart = DateUtils.todayStart();
        boolean overdue = task.isOverdue(todayStart);

        holder.description.setText(task.getDescription());
        holder.meta.setText(buildMeta(task));

        // A done row stays readable: struck through and muted, never invisible.
        if (task.isDone()) {
            holder.description.setPaintFlags(
                    holder.description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.description.setPaintFlags(
                    holder.description.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
        holder.description.setTextColor(ContextCompat.getColor(
                context, task.isDone() ? R.color.ink_soft : R.color.ink));

        int metaColour;
        if (overdue) {
            metaColour = R.color.danger;
        } else if (task.getDueDate() != null && DateUtils.isToday(task.getDueDate())) {
            metaColour = R.color.warning;
        } else {
            metaColour = R.color.ink_muted;
        }
        holder.meta.setTextColor(ContextCompat.getColor(context, metaColour));

        holder.overdue.setVisibility(overdue ? View.VISIBLE : View.GONE);

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(task.isDone());
        holder.check.setOnCheckedChangeListener(
                (button, isChecked) -> onToggleDone.onToggled(task, isChecked));

        return view;
    }

    private String buildMeta(TaskItem task) {
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

    private static class ViewHolder {

        final CheckBox check;
        final TextView description;
        final TextView meta;
        final TextView overdue;

        ViewHolder(View view) {
            check = view.findViewById(R.id.cbTask);
            description = view.findViewById(R.id.tvTaskDescription);
            meta = view.findViewById(R.id.tvTaskMeta);
            overdue = view.findViewById(R.id.tvOverdueBadge);
        }
    }
}
