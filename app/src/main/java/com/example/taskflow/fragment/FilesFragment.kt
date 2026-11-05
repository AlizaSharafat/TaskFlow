package com.example.taskflow.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.taskflow.IntentKeys
import com.example.taskflow.ProjectHost
import com.example.taskflow.ProjectTab
import com.example.taskflow.R
import com.example.taskflow.adapter.AttachmentAdapter
import com.example.taskflow.dialog.AttachmentDialogs
import com.example.taskflow.model.AttachmentCatalog
import com.example.taskflow.model.AttachmentLink
import com.example.taskflow.model.AttachmentType
import com.example.taskflow.model.FileTypeFilter

/**
 * The attachment list, with a name search and an All / Images / Text filter.
 * Attachments stay in the order they were linked.
 */
class FilesFragment : Fragment(), ProjectTab {

    private lateinit var host: ProjectHost
    private lateinit var adapter: AttachmentAdapter

    private lateinit var etSearch: EditText
    private lateinit var chipAll: TextView
    private lateinit var chipImages: TextView
    private lateinit var chipText: TextView
    private lateinit var tvShowing: TextView
    private lateinit var listView: ListView
    private lateinit var emptyBox: View
    private lateinit var tvEmptyTitle: TextView
    private lateinit var btnEmptyAction: Button

    private var bindingSearch = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        host = context as ProjectHost
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_files, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearch = view.findViewById(R.id.etSearchFiles)
        chipAll = view.findViewById(R.id.chipAll)
        chipImages = view.findViewById(R.id.chipImages)
        chipText = view.findViewById(R.id.chipText)
        tvShowing = view.findViewById(R.id.tvShowingFiles)
        listView = view.findViewById(R.id.listFiles)
        emptyBox = view.findViewById(R.id.emptyFiles)
        tvEmptyTitle = view.findViewById(R.id.tvEmptyFilesTitle)
        btnEmptyAction = view.findViewById(R.id.btnEmptyFilesAction)

        adapter = AttachmentAdapter(
            requireContext(),
            emptyList(),
            onPreview = { link ->
                AttachmentCatalog.find(link.catalogId)?.let {
                    AttachmentDialogs.showPreview(requireContext(), it)
                }
            },
            onRemove = { link -> confirmRemove(link) }
        )
        listView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (bindingSearch) return
                host.fileQuery().search = s.toString()
                refreshTab()
            }
        })

        chipAll.setOnClickListener { selectType(FileTypeFilter.ALL) }
        chipImages.setOnClickListener { selectType(FileTypeFilter.IMAGES) }
        chipText.setOnClickListener { selectType(FileTypeFilter.TEXT) }

        view.findViewById<Button>(R.id.btnAddAttachment).setOnClickListener { showCatalog() }

        refreshTab()
    }

    private fun selectType(type: FileTypeFilter) {
        host.fileQuery().type = type
        refreshTab()
    }

    override fun refreshTab() {
        if (!isAdded) return
        val project = host.project()
        val query = host.fileQuery()
        val shown = query.apply(project.attachments)

        adapter.submit(shown)

        bindingSearch = true
        if (etSearch.text.toString() != query.search) etSearch.setText(query.search)
        bindingSearch = false

        // Chip counts describe the whole collection, not the filtered result.
        val total = project.attachments.size
        val images = project.attachments.count {
            AttachmentCatalog.find(it.catalogId)?.type == AttachmentType.IMAGE
        }
        val texts = total - images

        chipAll.text = getString(R.string.chip_all, total)
        chipImages.text = getString(R.string.chip_images, images)
        chipText.text = getString(R.string.chip_text, texts)

        chipAll.isSelected = query.type == FileTypeFilter.ALL
        chipImages.isSelected = query.type == FileTypeFilter.IMAGES
        chipText.isSelected = query.type == FileTypeFilter.TEXT

        tvShowing.text = getString(R.string.showing_attachments, shown.size, total)

        paintEmptyState(total == 0, shown.isEmpty())
    }

    private fun paintEmptyState(noAttachmentsAtAll: Boolean, nothingShown: Boolean) {
        if (!nothingShown) {
            emptyBox.visibility = View.GONE
            listView.visibility = View.VISIBLE
            return
        }
        emptyBox.visibility = View.VISIBLE
        listView.visibility = View.GONE

        if (noAttachmentsAtAll) {
            tvEmptyTitle.setText(R.string.no_attachments_yet)
            btnEmptyAction.setText(R.string.add_attachment)
            btnEmptyAction.setOnClickListener { showCatalog() }
        } else {
            tvEmptyTitle.setText(R.string.no_attachments_match)
            btnEmptyAction.setText(R.string.reset)
            btnEmptyAction.setOnClickListener {
                host.fileQuery().reset()
                refreshTab()
            }
        }
    }

    private fun showCatalog() {
        AttachmentDialogs.showCatalogPicker(requireContext(), host.project()) { item ->
            host.project().attachments.add(AttachmentLink(catalogId = item.id))
            host.onProjectChanged()
        }
    }

    /** Removing asks the user to type the attachment name first. */
    private fun confirmRemove(link: AttachmentLink) {
        val item = AttachmentCatalog.find(link.catalogId) ?: return
        AttachmentDialogs.showRemoveConfirm(requireContext(), item) {
            host.project().attachments.removeAll { it.id == link.id }
            host.onProjectChanged()
        }
    }

    companion object {
        fun newInstance(projectId: String): FilesFragment {
            val fragment = FilesFragment()
            fragment.arguments = Bundle().apply {
                putString(IntentKeys.ARG_PROJECT_ID, projectId)
            }
            return fragment
        }
    }
}
