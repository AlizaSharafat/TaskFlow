package com.example.taskflow

import com.example.taskflow.model.DateFilter
import com.example.taskflow.model.FileQuery
import com.example.taskflow.model.Project
import com.example.taskflow.model.TaskQuery
import com.example.taskflow.model.TaskStatus

/**
 * How the four fragments talk to ProjectActivity.
 *
 * The Activity owns the single working Project and the live filter state, so
 * no fragment ever keeps its own copy of the data.
 */
interface ProjectHost {

    /** The one working project for this screen. */
    fun project(): Project

    /** Tell the host something changed, so the header, tabs and siblings refresh. */
    fun onProjectChanged()

    /** Switch to a tab by index: 0 Overview, 1 Tasks, 2 Files, 3 Discussion. */
    fun showTab(index: Int)

    /** The shared Tasks tab query, kept alive across tab switches. */
    fun taskQuery(): TaskQuery

    /** The shared Files tab query, kept alive across tab switches. */
    fun fileQuery(): FileQuery

    /** Jump to Tasks with a status already selected, used by Task Breakdown. */
    fun openTasksFiltered(status: TaskStatus?, date: DateFilter)
}
