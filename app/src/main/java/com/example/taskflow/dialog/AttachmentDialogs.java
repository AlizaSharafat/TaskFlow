package com.example.taskflow.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.taskflow.R;
import com.example.taskflow.model.AttachmentCatalog;
import com.example.taskflow.model.AttachmentType;
import com.example.taskflow.model.CatalogItem;
import com.example.taskflow.model.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

/** three attachment flows; catalog, preview, and remove. */
public final class AttachmentDialogs {

    private AttachmentDialogs() {
        // Utility class, never instantiated.
    }

    public interface OnPicked {
        void onPicked(CatalogItem item);
    }

    public interface OnConfirmed {
        void onConfirmed();
    }

    public static void showCatalogPicker(Context context, Project project, OnPicked onPicked) {
        final List<CatalogItem> choices = AttachmentCatalog.unlinkedFor(project);

        if (choices.isEmpty()) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.add_attachment)
                    .setMessage(R.string.catalog_all_linked)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }

        String[] names = new String[choices.size()];
        for (int i = 0; i < choices.size(); i++) {
            CatalogItem item = choices.get(i);
            names[i] = context.getString(R.string.catalog_row,
                    item.getDisplayName(), item.getType().getLabel());
        }
        final int[] selected = {-1};

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.select_attachment)
                .setSingleChoiceItems(names, -1, (d, which) -> {
                    selected[0] = which;
                    ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                })
                .setPositiveButton(R.string.add, (d, which) -> {
                    if (selected[0] >= 0) {
                        onPicked.onPicked(choices.get(selected[0]));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false));
        dialog.show();
    }

    public static void showPreview(Context context, CatalogItem item) {
        if (item.getType() == AttachmentType.IMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_preview_image, null);
            ((TextView) view.findViewById(R.id.tvPreviewTitle)).setText(item.getDisplayName());
            ((ImageView) view.findViewById(R.id.ivPreview)).setImageResource(item.getImageRes());
            showPreviewDialog(context, view);
            return;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_preview_text, null);
        ((TextView) view.findViewById(R.id.tvPreviewTitle)).setText(item.getDisplayName());
        ((TextView) view.findViewById(R.id.tvPreviewBody))
                .setText(readRawText(context, item.getTextRes()));
        showPreviewDialog(context, view);
    }

    private static void showPreviewDialog(Context context, View view) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(R.string.close, null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        }
        dialog.show();
    }

    public static void showRemoveConfirm(Context context, final CatalogItem item,
                                         final OnConfirmed onConfirmed) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_remove_attachment, null);

        TextView prompt = view.findViewById(R.id.tvRemovePrompt);
        EditText input = view.findViewById(R.id.etConfirmName);
        final Button btnRemove = view.findViewById(R.id.btnConfirmRemove);
        Button btnCancel = view.findViewById(R.id.btnCancelRemove);

        prompt.setText(context.getString(R.string.remove_prompt, item.getDisplayName()));
        btnRemove.setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
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
                btnRemove.setEnabled(s.toString().trim().equalsIgnoreCase(item.getDisplayName()));
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnRemove.setOnClickListener(v -> {
            onConfirmed.onConfirmed();
            dialog.dismiss();
        });
        dialog.show();
    }

    private static String readRawText(Context context, int rawRes) {
        StringBuilder builder = new StringBuilder();
        InputStream stream = context.getResources().openRawResource(rawRes);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        try {
            String line = reader.readLine();
            while (line != null) {
                builder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            return "";
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
                // Nothing useful to do if closing fails.
            }
        }
        return builder.toString();
    }
}
