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
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.example.taskflow.model.TaskSort;
import com.example.taskflow.model.TaskStatus;
import com.example.taskflow.util.Menus;

import java.util.ArrayList;
import java.util.List;


public class TasksFragment extends Fragment implements ProjectTab {

    private ProjectHost host;
    private TaskAdapter adapter;

    private EditText etSearch;
    private TextView tvStatus;
    private TextView tvPriority;
    private TextView tvDate;
    private TextView tvSort;
    private TextView tvShowing;
    private ListView listView;
    private View emptyBox;
    private TextView tvEmptyTitle;
    private Button btnEmptyAction;


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
        listView = view.findViewById(R.id.listTasks);
        emptyBox = view.findViewById(R.id.emptyTasks);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTasksTitle);
        btnEmptyAction = view.findViewById(R.id.btnEmptyTasksAction);

        adapter = new TaskAdapter(requireContext(), new ArrayList<>(), (task, isChecked) -> {
            // Ticking the box sets the task to Done, clearing it sends it back To do.
            task.setStatus(isChecked ? TaskStatus.DONE : TaskStatus.TODO);
            host.onProjectChanged();
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(
                (parent, v, position, id) -> showEditTask(adapter.getItem(position)));

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
        List<TaskItem> shown = query.apply(project.getTasks());

        adapter.submit(shown);

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


    private void paintEmptyState(boolean noTasksAtAll, boolean nothingShown) {
        if (!nothingShown) {
            emptyBox.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            return;
        }
        emptyBox.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

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
