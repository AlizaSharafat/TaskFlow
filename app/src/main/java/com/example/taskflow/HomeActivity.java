package com.example.taskflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taskflow.adapter.ProjectAdapter;
import com.example.taskflow.dialog.ProjectFormDialog;
import com.example.taskflow.model.AttachmentLink;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.SampleData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The project list.
 *
 * This Activity owns the full project collection. Everything else works on a
 * copy that travels by Intent and comes back through the activity result.
 */
public class HomeActivity extends AppCompatActivity {

    /** The one true collection. Nothing else keeps a second copy. */
    private final List<Project> projects = SampleData.projects();

    private ProjectAdapter adapter;
    private ListView listView;
    private EditText etSearch;
    private TextView tvShowing;
    private TextView tvEmpty;

    private String searchText = "";

    /**
     * Receives the edited project back from ProjectActivity and updates the
     * collection by ID. Row position is never used: the list can be filtered
     * by a search, so a position would point at the wrong project.
     */
    private final ActivityResultLauncher<Intent> projectDetailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK) {
                            return;
                        }
                        Project updated = IntentCompat.serializableExtra(
                                result.getData(), IntentKeys.EXTRA_PROJECT, Project.class);
                        if (updated == null) {
                            return;
                        }
                        for (int i = 0; i < projects.size(); i++) {
                            if (projects.get(i).getId().equals(updated.getId())) {
                                projects.set(i, updated);
                                break;
                            }
                        }
                        refreshList();
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        listView = findViewById(R.id.listProjects);
        etSearch = findViewById(R.id.etSearchProjects);
        tvShowing = findViewById(R.id.tvShowingProjects);
        tvEmpty = findViewById(R.id.tvNoProjects);

        showWorkspaceDetailsFromIntent();

        adapter = new ProjectAdapter(this, new ArrayList<>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(
                (parent, view, position, id) -> openProjectScreen(adapter.getItem(position)));

        Button btnNewProject = findViewById(R.id.btnNewProject);
        btnNewProject.setOnClickListener(v -> showNewProjectDialog());

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
                searchText = s.toString();
                refreshList();
            }
        });

        refreshList();
    }

    /** Reads the sample values SplashActivity put on the Intent. */
    private void showWorkspaceDetailsFromIntent() {
        String userName = getIntent().getStringExtra(IntentKeys.EXTRA_USER_NAME);
        String workspaceName = getIntent().getStringExtra(IntentKeys.EXTRA_WORKSPACE_NAME);
        String workspaceDate = getIntent().getStringExtra(IntentKeys.EXTRA_WORKSPACE_DATE);

        ((TextView) findViewById(R.id.tvWelcome))
                .setText(getString(R.string.welcome_title, orEmpty(userName)));
        ((TextView) findViewById(R.id.tvWorkspaceName)).setText(orEmpty(workspaceName));
        ((TextView) findViewById(R.id.tvWorkspaceDate))
                .setText(getString(R.string.workspace_created, orEmpty(workspaceDate)));
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /** Search is case-insensitive and matches any part of the name or the tag. */
    private List<Project> visibleProjects() {
        String needle = searchText.trim().toLowerCase(Locale.getDefault());
        if (needle.isEmpty()) {
            return projects;
        }
        List<Project> kept = new ArrayList<>();
        for (Project project : projects) {
            boolean matches = project.getName().toLowerCase(Locale.getDefault()).contains(needle)
                    || project.getTag().toLowerCase(Locale.getDefault()).contains(needle);
            if (matches) {
                kept.add(project);
            }
        }
        return kept;
    }

    private void refreshList() {
        List<Project> shown = visibleProjects();
        adapter.submit(shown);

        if (searchText.trim().isEmpty()) {
            tvShowing.setText(getResources()
                    .getQuantityString(R.plurals.showing_projects, shown.size(), shown.size()));
        } else {
            tvShowing.setText(
                    getString(R.string.showing_of_projects, shown.size(), projects.size()));
        }

        tvEmpty.setText(projects.isEmpty()
                ? getString(R.string.no_projects_yet)
                : getString(R.string.no_projects_match));
        tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Hands the whole project object to the detail screen through the Intent. */
    private void openProjectScreen(Project project) {
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra(IntentKeys.EXTRA_PROJECT, project);
        projectDetailLauncher.launch(intent);
    }

    /**
     * A new project starts empty: no tasks, no comments, 0/0 and 0%. It gets
     * one attachment only if the user picked one in the dialog.
     */
    private void showNewProjectDialog() {
        ProjectFormDialog.show(this, null, (name, tag, description, catalogId) -> {
            Project project = new Project(tag, name, description);
            if (catalogId != null) {
                project.getAttachments().add(new AttachmentLink(catalogId));
            }
            projects.add(project);
            refreshList();
        });
    }
}
