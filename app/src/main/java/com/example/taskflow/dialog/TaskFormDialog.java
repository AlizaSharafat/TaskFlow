package com.example.taskflow.dialog;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.taskflow.R;
import com.example.taskflow.model.TaskItem;
import com.example.taskflow.model.TaskPriority;
import com.example.taskflow.model.TaskStatus;
import com.example.taskflow.util.DateUtils;
import com.example.taskflow.util.Menus;

import java.util.ArrayList;
import java.util.List;

/**
 * The Add Task and Edit Task form.
 *
 * Overview and Tasks both open this same dialog, which is why it lives in its
 * own class rather than inside either fragment.
 */
public final class TaskFormDialog {

    public static final int DESCRIPTION_MAX = 500;

    private TaskFormDialog() {
        // Utility class, never instantiated.
    }

    public interface OnSaved {
        void onSaved(String description, TaskStatus status, TaskPriority priority, Long dueDate);
    }

    public interface OnDeleted {
        void onDeleted();
    }

    /**
     * @param existing null to add a task, or the task being edited.
     * @param onDeleted supplied only when editing, which is what shows Delete.
     */
    public static void show(final Context context, final TaskItem existing,
                            final OnSaved onSaved, final OnDeleted onDeleted) {

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_task_form, null);

        TextView title = view.findViewById(R.id.tvDialogTitle);
        final EditText etDescription = view.findViewById(R.id.etTaskDescription);
        final TextView tvCounter = view.findViewById(R.id.tvCharCounter);
        final TextView tvPriority = view.findViewById(R.id.tvPrioritySelector);
        final TextView tvStatus = view.findViewById(R.id.tvStatusSelector);
        final TextView tvDueDate = view.findViewById(R.id.tvDueDateSelector);
        final TextView btnClearDate = view.findViewById(R.id.btnClearDate);
        Button btnSave = view.findViewById(R.id.btnSaveTask);
        Button btnCancel = view.findViewById(R.id.btnCancelTask);
        Button btnDelete = view.findViewById(R.id.btnDeleteTask);

        // Defaults for a new task: Normal, To do, no due date.
        final TaskStatus[] status = {existing == null ? TaskStatus.TODO : existing.getStatus()};
        final TaskPriority[] priority =
                {existing == null ? TaskPriority.NORMAL : existing.getPriority()};
        final Long[] dueDate = {existing == null ? null : existing.getDueDate()};

        title.setText(existing == null ? R.string.add_new_task : R.string.edit_task);
        btnSave.setText(existing == null ? R.string.save_task : R.string.save_changes);
        btnDelete.setVisibility(onDeleted == null ? View.GONE : View.VISIBLE);

        if (existing != null) {
            etDescription.setText(existing.getDescription());
        }

        tvPriority.setText(priority[0].getLabel());
        tvStatus.setText(status[0].getLabel());
        paintDate(context, tvDueDate, btnClearDate, dueDate[0]);
        paintCounter(context, tvCounter, etDescription.getText().length());

        // Live 0/500 counter under the description box.
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do.
            }

            @Override
            public void afterTextChanged(Editable s) {
                paintCounter(context, tvCounter, s.length());
            }
        });

        tvPriority.setOnClickListener(anchor -> {
            final TaskPriority[] options = TaskPriority.values();
            List<String> labels = new ArrayList<>();
            for (TaskPriority option : options) {
                labels.add(option.getLabel());
            }
            Menus.show(anchor, labels, index -> {
                priority[0] = options[index];
                tvPriority.setText(priority[0].getLabel());
            });
        });

        tvStatus.setOnClickListener(anchor -> {
            final TaskStatus[] options = TaskStatus.values();
            List<String> labels = new ArrayList<>();
            for (TaskStatus option : options) {
                labels.add(option.getLabel());
            }
            Menus.show(anchor, labels, index -> {
                status[0] = options[index];
                tvStatus.setText(status[0].getLabel());
            });
        });

        tvDueDate.setOnClickListener(v -> {
            // Past dates are allowed, so no minimum is set on the picker.
            long seed = dueDate[0] == null ? DateUtils.todayStart() : dueDate[0];
            new DatePickerDialog(
                    context,
                    (picker, year, month, dayOfMonth) -> {
                        dueDate[0] = DateUtils.fromPicker(year, month, dayOfMonth);
                        paintDate(context, tvDueDate, btnClearDate, dueDate[0]);
                    },
                    DateUtils.yearOf(seed),
                    DateUtils.monthOf(seed),
                    DateUtils.dayOf(seed)
            ).show();
        });

        btnClearDate.setOnClickListener(v -> {
            dueDate[0] = null;
            paintDate(context, tvDueDate, btnClearDate, null);
        });

        final AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle(R.string.delete_task)
                .setMessage(R.string.delete_task_message)
                .setPositiveButton(R.string.delete, (d, which) -> {
                    if (onDeleted != null) {
                        onDeleted.onDeleted();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show());

        btnSave.setOnClickListener(v -> {
            String description = etDescription.getText().toString().trim();
            if (description.isEmpty()) {
                etDescription.setError(context.getString(R.string.error_task_required));
                return;
            }
            if (description.length() > DESCRIPTION_MAX) {
                etDescription.setError(
                        context.getString(R.string.error_task_too_long, DESCRIPTION_MAX));
                return;
            }
            onSaved.onSaved(description, status[0], priority[0], dueDate[0]);
            dialog.dismiss();
        });

        dialog.show();
    }

    private static void paintDate(Context context, TextView tvDueDate,
                                  TextView btnClearDate, Long dueDate) {
        if (dueDate == null) {
            tvDueDate.setText(R.string.no_due_date);
        } else if (DateUtils.isToday(dueDate)) {
            tvDueDate.setText(R.string.due_today);
        } else {
            tvDueDate.setText(DateUtils.format(dueDate));
        }
        btnClearDate.setVisibility(dueDate == null ? View.GONE : View.VISIBLE);
    }

    private static void paintCounter(Context context, TextView tvCounter, int length) {
        tvCounter.setText(context.getString(R.string.char_counter, length, DESCRIPTION_MAX));
    }
}
