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


    Project getProject();


    void onProjectChanged();


    void showTab(int index);


    TaskQuery getTaskQuery();

    FileQuery getFileQuery();

    void openTasksFiltered(TaskStatus status, DateFilter date);
}
