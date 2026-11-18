package com.example.taskflow;

/**
 * Every tab fragment implements this so ProjectActivity can tell it to read
 * the project again. Fragments are shown and hidden rather than recreated,
 * so onResume does not run a second time and this call takes its place.
 */
public interface ProjectTab {
    void refreshTab();
}
