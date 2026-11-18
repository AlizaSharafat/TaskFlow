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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskflow.IntentKeys;
import com.example.taskflow.ProjectHost;
import com.example.taskflow.ProjectTab;
import com.example.taskflow.R;
import com.example.taskflow.adapter.AttachmentAdapter;
import com.example.taskflow.dialog.AttachmentDialogs;
import com.example.taskflow.model.AttachmentCatalog;
import com.example.taskflow.model.AttachmentLink;
import com.example.taskflow.model.AttachmentType;
import com.example.taskflow.model.CatalogItem;
import com.example.taskflow.model.FileQuery;
import com.example.taskflow.model.FileTypeFilter;
import com.example.taskflow.model.Project;
import com.example.taskflow.touch.AttachmentTouchCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * The attachment list, with a name search and an All / Images / Text filter.
 *
 * Attachments stay in the order they were linked, so the RecyclerView here has
 * no drag. A swipe to the right asks to remove one, and the answer is the same
 * typed-name confirmation the bin icon has always used: a swipe is easier to
 * do by accident than a tap, so it cannot be allowed to be quicker.
 */
public class FilesFragment extends Fragment
        implements ProjectTab, AttachmentTouchCallback.Listener {

    private ProjectHost host;
    private AttachmentAdapter adapter;

    private EditText etSearch;
    private TextView chipAll;
    private TextView chipImages;
    private TextView chipText;
    private TextView tvShowing;
    private RecyclerView recyclerView;
    private View emptyBox;
    private TextView tvEmptyTitle;
    private Button btnEmptyAction;

    private boolean bindingSearch = false;

    public static FilesFragment newInstance(String projectId) {
        FilesFragment fragment = new FilesFragment();
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
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearchFiles);
        chipAll = view.findViewById(R.id.chipAll);
        chipImages = view.findViewById(R.id.chipImages);
        chipText = view.findViewById(R.id.chipText);
        tvShowing = view.findViewById(R.id.tvShowingFiles);
        recyclerView = view.findViewById(R.id.rvFiles);
        emptyBox = view.findViewById(R.id.emptyFiles);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyFilesTitle);
        btnEmptyAction = view.findViewById(R.id.btnEmptyFilesAction);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new AttachmentAdapter(
                link -> {
                    CatalogItem item = AttachmentCatalog.find(link.getCatalogId());
                    if (item != null) {
                        AttachmentDialogs.showPreview(requireContext(), item);
                    }
                },
                // The bin icon asks the same question a swipe does, but no row
                // was moved, so there is nothing to restore afterwards.
                link -> confirmRemove(RecyclerView.NO_POSITION, link));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new AttachmentTouchCallback(requireContext(), adapter, this))
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
                host.getFileQuery().setSearch(s.toString());
                refreshTab();
            }
        });

        chipAll.setOnClickListener(v -> selectType(FileTypeFilter.ALL));
        chipImages.setOnClickListener(v -> selectType(FileTypeFilter.IMAGES));
        chipText.setOnClickListener(v -> selectType(FileTypeFilter.TEXT));

        Button btnAdd = view.findViewById(R.id.btnAddAttachment);
        btnAdd.setOnClickListener(v -> showCatalog());

        refreshTab();
    }

    private void selectType(FileTypeFilter type) {
        host.getFileQuery().setType(type);
        refreshTab();
    }

    @Override
    public void refreshTab() {
        if (getView() == null) {
            return;
        }
        Project project = host.getProject();
        FileQuery query = host.getFileQuery();
        List<AttachmentLink> shown = query.apply(project.getAttachments());

        // A new list every time, so DiffUtil has an old and a new to compare.
        adapter.submitList(new ArrayList<>(shown));

        bindingSearch = true;
        if (!etSearch.getText().toString().equals(query.getSearch())) {
            etSearch.setText(query.getSearch());
        }
        bindingSearch = false;

        // Chip counts describe the whole collection, not the filtered result.
        int total = project.getAttachments().size();
        int images = 0;
        for (AttachmentLink link : project.getAttachments()) {
            CatalogItem item = AttachmentCatalog.find(link.getCatalogId());
            if (item != null && item.getType() == AttachmentType.IMAGE) {
                images++;
            }
        }
        int texts = total - images;

        chipAll.setText(getString(R.string.chip_all, total));
        chipImages.setText(getString(R.string.chip_images, images));
        chipText.setText(getString(R.string.chip_text, texts));

        chipAll.setSelected(query.getType() == FileTypeFilter.ALL);
        chipImages.setSelected(query.getType() == FileTypeFilter.IMAGES);
        chipText.setSelected(query.getType() == FileTypeFilter.TEXT);

        tvShowing.setText(getString(R.string.showing_attachments, shown.size(), total));

        paintEmptyState(total == 0, shown.isEmpty());
    }

    // ---- Swipe ----

    @Override
    public void onRemoveSwipe(int position, AttachmentLink link) {
        confirmRemove(position, link);
    }

    /**
     * Removing asks the user to type the attachment name first. Nothing is
     * unlinked until the typed name matches and Remove is pressed, and any
     * other ending puts a swiped row back where it was.
     */
    private void confirmRemove(final int position, final AttachmentLink link) {
        CatalogItem item = AttachmentCatalog.find(link.getCatalogId());
        if (item == null) {
            restoreRow(position);
            return;
        }
        AttachmentDialogs.showRemoveConfirm(requireContext(), item,
                () -> {
                    host.getProject().removeAttachmentById(link.getId());
                    host.onProjectChanged();
                },
                removed -> {
                    if (!removed) {
                        restoreRow(position);
                    }
                });
    }

    /** Slides a swiped row back into place by rebinding it. */
    private void restoreRow(int position) {
        if (adapter != null && position >= 0 && position < adapter.getItemCount()) {
            adapter.notifyItemChanged(position);
        }
    }

    // ---- Empty states ----

    private void paintEmptyState(boolean noAttachmentsAtAll, boolean nothingShown) {
        if (!nothingShown) {
            emptyBox.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            return;
        }
        emptyBox.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        if (noAttachmentsAtAll) {
            tvEmptyTitle.setText(R.string.no_attachments_yet);
            btnEmptyAction.setText(R.string.add_attachment);
            btnEmptyAction.setOnClickListener(v -> showCatalog());
        } else {
            tvEmptyTitle.setText(R.string.no_attachments_match);
            btnEmptyAction.setText(R.string.reset);
            btnEmptyAction.setOnClickListener(v -> {
                host.getFileQuery().reset();
                refreshTab();
            });
        }
    }

    private void showCatalog() {
        AttachmentDialogs.showCatalogPicker(requireContext(), host.getProject(), item -> {
            host.getProject().getAttachments().add(new AttachmentLink(item.getId()));
            host.onProjectChanged();
        });
    }
}
