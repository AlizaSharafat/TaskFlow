package com.example.taskflow.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.taskflow.R;
import com.example.taskflow.model.AttachmentCatalog;
import com.example.taskflow.model.CatalogItem;
import com.example.taskflow.model.Project;

import java.util.List;

/**
 * The New Project and Edit Project forms. Both use the same layout and the
 * same validation; editing prefills the fields and hides the attachment row.
 */
public final class ProjectFormDialog {

    public static final int NAME_MAX = 60;
    public static final int TAG_MAX = 20;
    public static final int DESCRIPTION_MAX = 500;

    private ProjectFormDialog() {
        // Utility class, never instantiated.
    }

    /** Reports the trimmed values, plus a catalog id when one was picked. */
    public interface OnSaved {
        void onSaved(String name, String tag, String description, String catalogId);
    }

    /**
     * @param existing null to create a new project, or the project being edited.
     */
    public static void show(final Context context, final Project existing, final OnSaved onSaved) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_project_form, null);

        TextView title = view.findViewById(R.id.tvDialogTitle);
        final EditText etName = view.findViewById(R.id.etProjectName);
        final EditText etTag = view.findViewById(R.id.etProjectTag);
        final EditText etDescription = view.findViewById(R.id.etProjectDescription);
        View attachmentRow = view.findViewById(R.id.attachmentRow);
        final TextView tvAttachment = view.findViewById(R.id.tvSelectedAttachment);
        Button btnSave = view.findViewById(R.id.btnSaveProject);
        Button btnCancel = view.findViewById(R.id.btnCancelProject);

        boolean isEdit = existing != null;
        title.setText(isEdit ? R.string.edit_project : R.string.new_project_title);
        btnSave.setText(isEdit ? R.string.save_changes : R.string.add_project);

        if (isEdit) {
            etName.setText(existing.getName());
            etTag.setText(existing.getTag());
            etDescription.setText(existing.getDescription());
            // Attachments are managed in the Files tab, not in the edit form.
            attachmentRow.setVisibility(View.GONE);
        }

        // A one element array so the click listener can write back into it.
        final String[] pickedCatalogId = {null};

        attachmentRow.setOnClickListener(v -> {
            final List<CatalogItem> choices = AttachmentCatalog.getItems();
            String[] names = new String[choices.size()];
            for (int i = 0; i < choices.size(); i++) {
                names[i] = choices.get(i).getDisplayName();
            }
            final int[] selected = {-1};

            new AlertDialog.Builder(context)
                    .setTitle(R.string.select_attachment)
                    .setSingleChoiceItems(names, -1, (d, which) -> selected[0] = which)
                    .setPositiveButton(R.string.select, (d, which) -> {
                        if (selected[0] >= 0) {
                            pickedCatalogId[0] = choices.get(selected[0]).getId();
                            tvAttachment.setText(choices.get(selected[0]).getDisplayName());
                        }
                    })
                    .setNeutralButton(R.string.clear, (d, which) -> {
                        pickedCatalogId[0] = null;
                        tvAttachment.setText(R.string.no_attachment_selected);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        final AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            // Trim first: a field holding only spaces is not valid input.
            String name = etName.getText().toString().trim();
            String tag = etTag.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (!validate(context, etName, name, NAME_MAX, R.string.field_name)) {
                return;
            }
            if (!validate(context, etTag, tag, TAG_MAX, R.string.field_tag)) {
                return;
            }
            if (!validate(context, etDescription, description, DESCRIPTION_MAX,
                    R.string.field_description)) {
                return;
            }

            onSaved.onSaved(name, tag, description, pickedCatalogId[0]);
            dialog.dismiss();
        });

        dialog.show();
    }

    /** One rule for all three fields: 1 to max characters after trimming. */
    private static boolean validate(Context context, EditText field, String value,
                                    int max, int fieldNameRes) {
        String fieldName = context.getString(fieldNameRes);
        if (value.isEmpty()) {
            field.setError(context.getString(R.string.error_required, fieldName));
            field.requestFocus();
            return false;
        }
        if (value.length() > max) {
            field.setError(context.getString(R.string.error_too_long, fieldName, max));
            field.requestFocus();
            return false;
        }
        return true;
    }
}
