package com.example.taskflow;

import com.example.taskflow.model.DateFilter;
import com.example.taskflow.model.FileQuery;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.TaskQuery;
import com.example.taskflow.model.TaskStatus;

/**
 * How the four fragments talk to ProjectActivity.
 *
 * The Activity owns the single working Project and the live filter state, so
 * no fragment ever keeps its own copy of the data.
 */
public interface ProjectHost {

    /** The one working project for this screen. */
    Project getProject();

    /** Tell the host something changed, so the header, tabs and siblings refresh. */
    void onProjectChanged();

    /** Switch to a tab by index: 0 Overview, 1 Tasks, 2 Files, 3 Discussion. */
    void showTab(int index);

    /** The shared Tasks tab query, kept alive across tab switches. */
    TaskQuery getTaskQuery();

    /** The shared Files tab query, kept alive across tab switches. */
    FileQuery getFileQuery();

    /** Jump to Tasks with a filter already applied, used by Task Breakdown. */
    void openTasksFiltered(TaskStatus status, DateFilter date);
}
