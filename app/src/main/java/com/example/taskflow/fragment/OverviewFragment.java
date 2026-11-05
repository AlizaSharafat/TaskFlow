package com.example.taskflow.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.taskflow.IntentKeys;
import com.example.taskflow.ProjectHost;
import com.example.taskflow.ProjectTab;
import com.example.taskflow.R;
import com.example.taskflow.dialog.ProjectFormDialog;
import com.example.taskflow.dialog.TaskFormDialog;
import com.example.taskflow.model.AttachmentCatalog;
import com.example.taskflow.model.AttachmentLink;
import com.example.taskflow.model.CatalogItem;
import com.example.taskflow.model.DateFilter;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.TaskItem;
import com.example.taskflow.model.TaskStatus;
import com.example.taskflow.util.DateUtils;

import java.util.ArrayList;
import java.util.List;


public class OverviewFragment extends Fragment implements ProjectTab {

    private ProjectHost host;

    private TextView tvDescription;
    private TextView tvCompleted;
    private TextView tvPercent;
    private ProgressBar progress;
    private TextView tvToDo;
    private TextView tvInProgress;
    private TextView tvDone;
    private TextView tvOverdue;
    private TextView tvAttachmentsSummary;

    public static OverviewFragment newInstance(String projectId) {
        OverviewFragment fragment = new OverviewFragment();
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
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDescription = view.findViewById(R.id.tvOverviewDescription);
        tvCompleted = view.findViewById(R.id.tvCompletedTasks);
        tvPercent = view.findViewById(R.id.tvOverviewPercent);
        progress = view.findViewById(R.id.progressOverview);
        tvToDo = view.findViewById(R.id.tvCountToDo);
        tvInProgress = view.findViewById(R.id.tvCountInProgress);
        tvDone = view.findViewById(R.id.tvCountDone);
        tvOverdue = view.findViewById(R.id.tvCountOverdue);
        tvAttachmentsSummary = view.findViewById(R.id.tvAttachmentsSummary);

        view.findViewById(R.id.btnEditProject).setOnClickListener(v -> showEditProject());

        // Tapping a breakdown card filters the Tasks tab by that status.
        view.findViewById(R.id.cardToDo).setOnClickListener(
                v -> host.openTasksFiltered(TaskStatus.TODO, DateFilter.ALL));
        view.findViewById(R.id.cardInProgress).setOnClickListener(
                v -> host.openTasksFiltered(TaskStatus.IN_PROGRESS, DateFilter.ALL));
        view.findViewById(R.id.cardDone).setOnClickListener(
                v -> host.openTasksFiltered(TaskStatus.DONE, DateFilter.ALL));
        // Overdue is a date filter across every status, not a status of its own.
        view.findViewById(R.id.cardOverdue).setOnClickListener(
                v -> host.openTasksFiltered(null, DateFilter.OVERDUE));

        view.findViewById(R.id.linkAttachments).setOnClickListener(v -> host.showTab(2));
        view.findViewById(R.id.linkDiscussion).setOnClickListener(v -> host.showTab(3));

        Button btnAddTask = view.findViewById(R.id.btnAddTaskOverview);
        btnAddTask.setOnClickListener(v -> showAddTask());

        refreshTab();
    }

    @Override
    public void refreshTab() {

        if (getView() == null) {
            return;
        }
        Project project = host.getProject();
        long todayStart = DateUtils.todayStart();

        tvDescription.setText(project.getDescription());
        tvCompleted.setText(getString(R.string.completed_of_tasks,
                project.getDoneTaskCount(), project.getTotalTaskCount()));
        tvPercent.setText(getString(R.string.percent_value, project.getProgressPercent()));
        progress.setProgress(project.getProgressPercent());

        tvToDo.setText(String.valueOf(project.getToDoCount()));
        tvInProgress.setText(String.valueOf(project.getInProgressCount()));
        tvDone.setText(String.valueOf(project.getDoneTaskCount()));
        tvOverdue.setText(String.valueOf(project.getOverdueCount(todayStart)));

        if (project.getAttachments().isEmpty()) {
            tvAttachmentsSummary.setText(R.string.no_attachments_yet);
        } else {
            tvAttachmentsSummary.setText(getString(R.string.attachments_summary,
                    project.getAttachmentCount(), firstTwoNames(project)));
        }
    }

    private String firstTwoNames(Project project) {
        List<String> names = new ArrayList<>();
        for (AttachmentLink link : project.getAttachments()) {
            CatalogItem item = AttachmentCatalog.find(link.getCatalogId());
            if (item != null) {
                names.add(item.getDisplayName());
            }
            if (names.size() == 2) {
                break;
            }
        }
        return android.text.TextUtils.join(", ", names);
    }

    private void showEditProject() {
        final Project project = host.getProject();
        ProjectFormDialog.show(requireContext(), project,
                (name, tag, description, catalogId) -> {
                    project.setName(name);
                    project.setTag(tag);
                    project.setDescription(description);
                    host.onProjectChanged();
                });
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
}
