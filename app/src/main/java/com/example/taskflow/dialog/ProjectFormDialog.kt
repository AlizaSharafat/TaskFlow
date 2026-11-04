package com.example.taskflow.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.taskflow.R
import com.example.taskflow.model.AttachmentCatalog
import com.example.taskflow.model.Project

/**
 * The New Project and Edit Project forms. Both use the same layout and the
 * same validation; the only difference is that editing prefills the fields
 * and hides the attachment selector.
 */
object ProjectFormDialog {

    const val NAME_MAX = 60
    const val TAG_MAX = 20
    const val DESCRIPTION_MAX = 500

    /**
     * @param existing null to create a new project, or the project being edited.
     * @param onSave receives trimmed values, plus a catalog id when the user
     *        picked an optional attachment while creating.
     */
    fun show(
        context: Context,
        existing: Project?,
        onSave: (name: String, tag: String, description: String, catalogId: String?) -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_project_form, null)

        val title = view.findViewById<TextView>(R.id.tvDialogTitle)
        val etName = view.findViewById<EditText>(R.id.etProjectName)
        val etTag = view.findViewById<EditText>(R.id.etProjectTag)
        val etDescription = view.findViewById<EditText>(R.id.etProjectDescription)
        val attachmentRow = view.findViewById<View>(R.id.attachmentRow)
        val tvAttachment = view.findViewById<TextView>(R.id.tvSelectedAttachment)
        val btnSave = view.findViewById<Button>(R.id.btnSaveProject)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelProject)

        val isEdit = existing != null
        title.setText(if (isEdit) R.string.edit_project else R.string.new_project_title)
        btnSave.setText(if (isEdit) R.string.save_changes else R.string.add_project)

        if (existing != null) {
            etName.setText(existing.name)
            etTag.setText(existing.tag)
            etDescription.setText(existing.description)
            // Attachments are managed in the Files tab, not in the edit form.
            attachmentRow.visibility = View.GONE
        }

        var pickedCatalogId: String? = null
        attachmentRow.setOnClickListener {
            val choices = AttachmentCatalog.items
            val names = choices.map { it.displayName }.toTypedArray()
            var selected = -1
            val picker = AlertDialog.Builder(context)
                .setTitle(R.string.select_attachment)
                .setSingleChoiceItems(names, -1) { _, which -> selected = which }
                .setPositiveButton(R.string.select) { _, _ ->
                    if (selected >= 0) {
                        pickedCatalogId = choices[selected].id
                        tvAttachment.text = choices[selected].displayName
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.clear) { _, _ ->
                    pickedCatalogId = null
                    tvAttachment.setText(R.string.no_attachment_selected)
                }
                .create()
            picker.show()
        }

        val dialog = AlertDialog.Builder(context).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            // Trim first: a field holding only spaces is not valid input.
            val name = etName.text.toString().trim()
            val tag = etTag.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (!validate(context, etName, name, NAME_MAX, R.string.field_name)) return@setOnClickListener
            if (!validate(context, etTag, tag, TAG_MAX, R.string.field_tag)) return@setOnClickListener
            if (!validate(context, etDescription, description, DESCRIPTION_MAX, R.string.field_description)) {
                return@setOnClickListener
            }

            onSave(name, tag, description, pickedCatalogId)
            dialog.dismiss()
        }

        dialog.show()
    }

    /** One rule for all three fields: 1 to max characters after trimming. */
    private fun validate(
        context: Context,
        field: EditText,
        value: String,
        max: Int,
        fieldNameRes: Int
    ): Boolean {
        val fieldName = context.getString(fieldNameRes)
        if (value.isEmpty()) {
            field.error = context.getString(R.string.error_required, fieldName)
            field.requestFocus()
            return false
        }
        if (value.length > max) {
            field.error = context.getString(R.string.error_too_long, fieldName, max)
            field.requestFocus()
            return false
        }
        return true
    }
}
