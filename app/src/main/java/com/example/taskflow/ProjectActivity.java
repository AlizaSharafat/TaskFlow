package com.example.taskflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.taskflow.fragment.DiscussionFragment;
import com.example.taskflow.fragment.FilesFragment;
import com.example.taskflow.fragment.OverviewFragment;
import com.example.taskflow.fragment.TasksFragment;
import com.example.taskflow.model.DateFilter;
import com.example.taskflow.model.FileQuery;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.TaskQuery;
import com.example.taskflow.model.TaskStatus;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Hosts the four project tabs.
 *
 * It holds one working Project and the live filter state for Tasks and Files,
 * so switching tabs never loses a search and no fragment keeps its own copy
 * of the data.
 */
public class ProjectActivity extends AppCompatActivity implements ProjectHost {

    private static final String[] TAB_TITLES = {"Overview", "Tasks", "Files", "Discussion"};

    private Project workingProject;

    private final TaskQuery taskQuery = new TaskQuery();
    private final FileQuery fileQuery = new FileQuery();

    private TabLayout tabLayout;
    private TextView tvHeaderName;
    private TextView tvHeaderTag;

    /** Fragments are created once and then shown and hidden, never rebuilt. */
    private final Map<Integer, Fragment> fragments = new HashMap<>();
    private int currentTab = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        Project received = IntentCompat.serializableExtra(
                getIntent(), IntentKeys.EXTRA_PROJECT, Project.class);
        if (received == null) {
            finish();
            return;
        }
        workingProject = received;

        tabLayout = findViewById(R.id.tabLayout);
        tvHeaderName = findViewById(R.id.tvProjectName);
        tvHeaderTag = findViewById(R.id.tvProjectTag);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> returnUpdatedProject());

        // The system back gesture must behave exactly like the toolbar arrow.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnUpdatedProject();
            }
        });

        buildTabs();
        paintHeader();
        showTab(0);
    }

    private void buildTabs() {
        for (int i = 0; i < TAB_TITLES.length; i++) {
            tabLayout.addTab(tabLayout.newTab());
        }
        updateTabTitles();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Nothing to do.
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Nothing to do.
            }
        });
    }

    /** Overview has no count; the other three show the project's totals. */
    private void updateTabTitles() {
        int[] counts = {
                -1,
                workingProject.getTotalTaskCount(),
                workingProject.getAttachmentCount(),
                workingProject.getCommentCount()
        };
        for (int i = 0; i < TAB_TITLES.length; i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab == null) {
                continue;
            }
            tab.setText(counts[i] < 0
                    ? TAB_TITLES[i]
                    : getString(R.string.tab_with_count, TAB_TITLES[i], counts[i]));
        }
    }

    private void paintHeader() {
        tvHeaderName.setText(workingProject.getName());
        tvHeaderTag.setText(workingProject.getTag());
    }

    private Fragment fragmentFor(int index) {
        Fragment existing = fragments.get(index);
        if (existing != null) {
            return existing;
        }
        Fragment created;
        switch (index) {
            case 0:
                created = OverviewFragment.newInstance(workingProject.getId());
                break;
            case 1:
                created = TasksFragment.newInstance(workingProject.getId());
                break;
            case 2:
                created = FilesFragment.newInstance(workingProject.getId());
                break;
            default:
                created = DiscussionFragment.newInstance(workingProject.getId());
                break;
        }
        fragments.put(index, created);
        return created;
    }

    // ---- ProjectHost ----

    @Override
    public Project getProject() {
        return workingProject;
    }

    @Override
    public TaskQuery getTaskQuery() {
        return taskQuery;
    }

    @Override
    public FileQuery getFileQuery() {
        return fileQuery;
    }

    /**
     * Something changed inside a fragment. Repaint everything that shows a
     * number derived from the project, then let the visible tab redraw.
     */
    @Override
    public void onProjectChanged() {
        paintHeader();
        updateTabTitles();
        refreshIfTab(fragments.get(currentTab));
    }

    @Override
    public void showTab(int index) {
        if (index == currentTab) {
            refreshIfTab(fragments.get(index));
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment target = fragmentFor(index);

        for (Map.Entry<Integer, Fragment> entry : fragments.entrySet()) {
            if (entry.getKey() != index && entry.getValue().isAdded()) {
                transaction.hide(entry.getValue());
            }
        }

        if (target.isAdded()) {
            transaction.show(target);
        } else {
            transaction.add(R.id.tabContainer, target, "tab_" + index);
        }
        transaction.commit();
        getSupportFragmentManager().executePendingTransactions();

        currentTab = index;
        if (tabLayout.getSelectedTabPosition() != index) {
            TabLayout.Tab tab = tabLayout.getTabAt(index);
            if (tab != null) {
                tab.select();
            }
        }
        // A hidden tab reads the records again the moment it becomes visible.
        refreshIfTab(target);
    }

    private void refreshIfTab(Fragment fragment) {
        if (fragment instanceof ProjectTab) {
            ((ProjectTab) fragment).refreshTab();
        }
    }

    /** Task Breakdown on Overview jumps here, with the filter already set. */
    @Override
    public void openTasksFiltered(TaskStatus status, DateFilter date) {
        taskQuery.setStatus(status);
        taskQuery.setDate(date);
        showTab(1);
    }

    /**
     * Hands the edited project back to HomeActivity, which finds it by ID.
     * Nothing is appended and no second HomeActivity is created.
     */
    private void returnUpdatedProject() {
        Intent data = new Intent();
        data.putExtra(IntentKeys.EXTRA_PROJECT, workingProject);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
