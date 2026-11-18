package com.example.taskflow.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskflow.IntentKeys;
import com.example.taskflow.ProjectHost;
import com.example.taskflow.ProjectTab;
import com.example.taskflow.R;
import com.example.taskflow.adapter.TaskAdapter;
import com.example.taskflow.dialog.TaskFormDialog;
import com.example.taskflow.model.DateFilter;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.TaskItem;
import com.example.taskflow.model.TaskPriority;
import com.example.taskflow.model.TaskQuery;
import com.example.taskflow.model.TaskRow;
import com.example.taskflow.model.TaskSort;
import com.example.taskflow.model.TaskStatus;
import com.example.taskflow.touch.TaskTouchCallback;
import com.example.taskflow.util.DateUtils;
import com.example.taskflow.util.Menus;

import java.util.ArrayList;
import java.util.List;

/**
 * The task list: search, three filters, a sort, and the grouped RecyclerView.
 *
 * One RecyclerView holds the whole screen. The four sections are not four
 * lists, they are header rows mixed into the same list, which is what lets a
 * task swiped to the left travel from its old group to Done without anything
 * being moved between adapters.
 *
 * The query lives in ProjectActivity rather than here, which is how the tab
 * keeps its filters when the user switches away and comes back.
 */
public class TasksFragment extends Fragment implements ProjectTab, TaskTouchCallback.Listener {

    private ProjectHost host;
    private TaskAdapter adapter;

    private EditText etSearch;
    private TextView tvStatus;
    private TextView tvPriority;
    private TextView tvDate;
    private TextView tvSort;
    private TextView tvShowing;
    private RecyclerView recyclerView;
    private View emptyBox;
    private TextView tvEmptyTitle;
    private Button btnEmptyAction;

    /** Stops the search box from re-filtering while we are writing into it. */
    private boolean bindingSearch = false;

    public static TasksFragment newInstance(String projectId) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putString(IntentKeys.ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (ProjectHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearchTasks);
        tvStatus = view.findViewById(R.id.filterStatus);
        tvPriority = view.findViewById(R.id.filterPriority);
        tvDate = view.findViewById(R.id.filterDate);
        tvSort = view.findViewById(R.id.filterSort);
        tvShowing = view.findViewById(R.id.tvShowingTasks);
        recyclerView = view.findViewById(R.id.rvTasks);
        emptyBox = view.findViewById(R.id.emptyTasks);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTasksTitle);
        btnEmptyAction = view.findViewById(R.id.btnEmptyTasksAction);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new TaskAdapter(requireContext(),
                (task, isChecked) -> {
                    // Ticking the box sets the task Done, clearing it sends it back To do.
                    task.setStatus(isChecked ? TaskStatus.DONE : TaskStatus.TODO);
                    host.onProjectChanged();
                },
                this::showEditTask);
        recyclerView.setAdapter(adapter);

        // ItemTouchHelper is what turns finger movement on a row into a swipe
        // or a drag. The rules for which rows allow what live in the callback.
        new ItemTouchHelper(new TaskTouchCallback(requireContext(), adapter, this))
                .attachToRecyclerView(recyclerView);

        etSearch.addTextChangedListener(new TextWatcher() {
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
                if (bindingSearch) {
                    return;
                }
                host.getTaskQuery().setSearch(s.toString());
                refreshTab();
            }
        });

        tvStatus.setOnClickListener(anchor -> {
            final TaskStatus[] options = TaskStatus.values();
            List<String> labels = new ArrayList<>();
            labels.add(getString(R.string.all_statuses));
            for (TaskStatus option : options) {
                labels.add(option.getLabel());
            }
            Menus.show(anchor, labels, index -> {
                // Index 0 is "All statuses", so the enum values start at 1.
                host.getTaskQuery().setStatus(index == 0 ? null : options[index - 1]);
                refreshTab();
            });
        });

        tvPriority.setOnClickListener(anchor -> {
            final TaskPriority[] options = TaskPriority.values();
            List<String> labels = new ArrayList<>();
            labels.add(getString(R.string.all_priorities));
            for (TaskPriority option : options) {
                labels.add(option.getLabel());
            }
            Menus.show(anchor, labels, index -> {
                host.getTaskQuery().setPriority(index == 0 ? null : options[index - 1]);
                refreshTab();
            });
        });

        tvDate.setOnClickListener(anchor -> {
            final DateFilter[] options = DateFilter.values();
            List<String> labels = new ArrayList<>();
            for (DateFilter option : options) {
                labels.add(option.getLabel());
            }
            Menus.show(anchor, labels, index -> {
                host.getTaskQuery().setDate(options[index]);
                refreshTab();
            });
        });

        tvSort.setOnClickListener(anchor -> {
            final TaskSort[] options = TaskSort.values();
            List<String> labels = new ArrayList<>();
            for (TaskSort option : options) {
                labels.add(option.getLabel());
            }
            Menus.show(anchor, labels, index -> {
                host.getTaskQuery().setSort(options[index]);
                refreshTab();
            });
        });

        view.findViewById(R.id.btnResetFilters).setOnClickListener(v -> {
            host.getTaskQuery().reset();
            refreshTab();
        });

        Button btnAddTask = view.findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(v -> showAddTask());

        refreshTab();
    }

    @Override
    public void refreshTab() {
        if (getView() == null) {
            return;
        }
        Project project = host.getProject();
        TaskQuery query = host.getTaskQuery();

        // Filter and sort first, then split what survives into sections. A
        // group that lost all of its tasks contributes no header at all.
        List<TaskItem> shown = query.apply(project.getTasks());
        List<TaskRow> rows = TaskQuery.group(shown, DateUtils.todayStart());
        adapter.submit(rows);

        // Write the query back into the controls, because Overview can change it.
        bindingSearch = true;
        if (!etSearch.getText().toString().equals(query.getSearch())) {
            etSearch.setText(query.getSearch());
        }
        bindingSearch = false;

        tvStatus.setText(getString(R.string.filter_status, query.getStatusLabel()));
        tvPriority.setText(getString(R.string.filter_priority, query.getPriorityLabel()));
        tvDate.setText(getString(R.string.filter_date, query.getDate().getLabel()));
        tvSort.setText(getString(R.string.filter_sort, query.getSort().getLabel()));

        tvShowing.setText(getString(R.string.showing_tasks,
                shown.size(), project.getTotalTaskCount()));

        paintEmptyState(project.getTasks().isEmpty(), shown.isEmpty());
    }

    // ---- Swipe and drag ----

    /**
     * Swiped left. The row is put back first, because the task is moving to
     * another section rather than disappearing, and a row left translated off
     * the edge would stay there.
     */
    @Override
    public void onCompleteSwipe(int position, TaskItem task) {
        restoreRow(position);

        if (task.isDone()) {
            // Already finished: nothing to change, and nothing is duplicated.
            Toast.makeText(requireContext(), R.string.task_already_done,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        task.setStatus(TaskStatus.DONE);
        host.onProjectChanged();
    }

    /** Swiped right. Nothing is removed until the dialog says so. */
    @Override
    public void onDeleteSwipe(final int position, final TaskItem task) {
        final boolean[] deleted = {false};

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_task)
                .setMessage(getString(R.string.delete_task_confirm, task.getDescription()))
                .setPositiveButton(R.string.delete, (d, which) -> {
                    deleted[0] = true;
                    host.getProject().removeTaskById(task.getId());
                    host.onProjectChanged();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Cancel, the back button and a tap outside all land here, so the row
        // is restored once for every way of saying no.
        dialog.setOnDismissListener(d -> {
            if (!deleted[0]) {
                restoreRow(position);
            }
        });
        dialog.show();
    }

    @Override
    public void onTaskMoved(TaskItem moving, TaskItem target) {
        host.getProject().moveTask(moving.getId(), target.getId());
    }

    @Override
    public void onDragFinished() {
        // A hand-made order and an automatic sort cannot both be in charge of
        // the same list, so dropping a row switches the sort to Manual. The
        // pill says so, and Reset filters puts the automatic sort back.
        host.getTaskQuery().setSort(TaskSort.MANUAL);
        host.onProjectChanged();
    }

    /** Slides a swiped row back into place by rebinding it. */
    private void restoreRow(int position) {
        if (adapter != null && position >= 0 && position < adapter.getItemCount()) {
            adapter.notifyItemChanged(position);
        }
    }

    // ---- Empty states ----

    /**
     * Two different empty states: a project with no tasks at all, and a project
     * whose tasks are all filtered out.
     */
    private void paintEmptyState(boolean noTasksAtAll, boolean nothingShown) {
        if (!nothingShown) {
            emptyBox.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            return;
        }
        emptyBox.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        if (noTasksAtAll) {
            tvEmptyTitle.setText(R.string.no_tasks_yet);
            btnEmptyAction.setText(R.string.add_task);
            btnEmptyAction.setOnClickListener(v -> showAddTask());
        } else {
            tvEmptyTitle.setText(R.string.no_tasks_match);
            btnEmptyAction.setText(R.string.reset_filters);
            btnEmptyAction.setOnClickListener(v -> {
                host.getTaskQuery().reset();
                refreshTab();
            });
        }
    }

    private void showAddTask() {
        TaskFormDialog.show(requireContext(), null,
                (description, status, priority, dueDate) -> {
                    host.getProject().getTasks()
                            .add(new TaskItem(description, status, priority, dueDate));
                    host.onProjectChanged();
                },
                null);
    }

    /** The editor shows the full description, not the two-line row preview. */
    private void showEditTask(final TaskItem task) {
        TaskFormDialog.show(requireContext(), task,
                (description, status, priority, dueDate) -> {
                    task.setDescription(description);
                    task.setStatus(status);
                    task.setPriority(priority);
                    task.setDueDate(dueDate);
                    host.onProjectChanged();
                },
                () -> {
                    host.getProject().removeTaskById(task.getId());
                    host.onProjectChanged();
                });
    }
}
