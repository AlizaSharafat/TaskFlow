package com.example.taskflow.dialog

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.taskflow.R
import com.example.taskflow.model.AttachmentCatalog
import com.example.taskflow.model.AttachmentType
import com.example.taskflow.model.CatalogItem
import com.example.taskflow.model.Project

/** The three attachment flows: pick from the catalog, preview, and remove. */
object AttachmentDialogs {

    /**
     * Single-choice list of catalog items this project has not linked yet.
     * Add stays disabled until something eligible is selected.
     */
    fun showCatalogPicker(context: Context, project: Project, onPick: (CatalogItem) -> Unit) {
        val choices = AttachmentCatalog.unlinkedFor(project)

        if (choices.isEmpty()) {
            AlertDialog.Builder(context)
                .setTitle(R.string.add_attachment)
                .setMessage(R.string.catalog_all_linked)
                .setPositiveButton(R.string.ok, null)
                .show()
            return
        }

        val names = choices.map { "${it.displayName}  ·  ${it.type.label}" }.toTypedArray()
        var selected = -1

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.select_attachment)
            .setSingleChoiceItems(names, -1) { d, which ->
                selected = which
                (d as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
            }
            .setPositiveButton(R.string.add) { _, _ ->
                if (selected >= 0) onPick(choices[selected])
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        dialog.show()
    }

    /**
     * Shows the real content: the bitmap for an image, the file's text for a
     * document. An icon or a Toast would not count as a preview.
     */
    fun showPreview(context: Context, item: CatalogItem) {
        if (item.type == AttachmentType.IMAGE) {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_preview_image, null)
            view.findViewById<TextView>(R.id.tvPreviewTitle).text = item.displayName
            view.findViewById<ImageView>(R.id.ivPreview).setImageResource(item.imageRes)
            AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(R.string.close, null)
                .create()
                .also { it.window?.setBackgroundDrawableResource(R.drawable.bg_dialog) }
                .show()
            return
        }

        val text = readRawText(context, item.textRes)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_preview_text, null)
        view.findViewById<TextView>(R.id.tvPreviewTitle).text = item.displayName
        view.findViewById<TextView>(R.id.tvPreviewBody).text = text
        AlertDialog.Builder(context)
            .setView(view)
            .setPositiveButton(R.string.close, null)
            .create()
            .also { it.window?.setBackgroundDrawableResource(R.drawable.bg_dialog) }
            .show()
    }

    /**
     * Removing is confirmed by typing the attachment's display name, so it
     * cannot happen by a stray tap.
     */
    fun showRemoveConfirm(context: Context, item: CatalogItem, onConfirm: () -> Unit) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_remove_attachment, null)
        val prompt = view.findViewById<TextView>(R.id.tvRemovePrompt)
        val input = view.findViewById<EditText>(R.id.etConfirmName)
        val btnRemove = view.findViewById<Button>(R.id.btnConfirmRemove)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelRemove)

        prompt.text = context.getString(R.string.remove_prompt, item.displayName)
        btnRemove.isEnabled = false

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                btnRemove.isEnabled =
                    s.toString().trim().equals(item.displayName, ignoreCase = true)
            }
        })

        val dialog = AlertDialog.Builder(context).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnRemove.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }
        dialog.show()
    }

    /** Reads a bundled text document out of res/raw. */
    private fun readRawText(context: Context, rawRes: Int): String =
        context.resources.openRawResource(rawRes).bufferedReader().use { it.readText() }
}
