package com.example.taskflow.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.taskflow.IntentKeys
import com.example.taskflow.ProjectTab
import com.example.taskflow.R

/** A placeholder tab. The assignment asks only for a Coming Soon message. */
class DiscussionFragment : Fragment(), ProjectTab {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_discussion, container, false)

    override fun refreshTab() = Unit

    companion object {
        fun newInstance(projectId: String): DiscussionFragment {
            val fragment = DiscussionFragment()
            fragment.arguments = Bundle().apply {
                putString(IntentKeys.ARG_PROJECT_ID, projectId)
            }
            return fragment
        }
    }
}
