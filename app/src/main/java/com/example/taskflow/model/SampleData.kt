package com.example.taskflow.model

import com.example.taskflow.util.DateUtils

/**
 * The projects the app starts with.
 *
 * Due dates are built relative to today rather than hard coded, so the
 * Overdue and Due today examples stay correct whenever the app is run.
 */
object SampleData {

    fun projects(): MutableList<Project> = mutableListOf(
        servicePageWebsite(),
        loginApi(),
        mobileDashboard(),
        releaseChecklist(),
        designAssets()
    )

    /** The main sample project: 12 tasks, 4 done, 2 overdue, 3 attachments. */
    private fun servicePageWebsite() = Project(
        tag = "User Interface",
        name = "Service Page Website",
        description = "Make a page display about services for the company website in blue " +
            "and red and provide some price information on each product card.",
        attachments = mutableListOf(
            AttachmentLink(catalogId = "cat_sports_project_v2"),
            AttachmentLink(catalogId = "cat_briefing_from_client"),
            AttachmentLink(catalogId = "cat_mount_asset")
        ),
        tasks = mutableListOf(
            task("Build header layout", TaskStatus.TODO, TaskPriority.HIGH, 0),
            task("Add form validation", TaskStatus.TODO, TaskPriority.HIGH, 0),
            task("Create low fidelity service page website", TaskStatus.TODO, TaskPriority.NORMAL, -7),
            task("Write meta descriptions for each section", TaskStatus.TODO, TaskPriority.LOW, 5),
            task("Collect product card copy from client", TaskStatus.TODO, TaskPriority.NORMAL, null),
            task("Configure pricing table responsive grid", TaskStatus.IN_PROGRESS, TaskPriority.NORMAL, 3),
            task("Wire up the contact form endpoint", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, -2),
            task("Build responsive breakpoints for tablet", TaskStatus.IN_PROGRESS, TaskPriority.NORMAL, 6),
            task("Create brand guideline for website design", TaskStatus.DONE, TaskPriority.NORMAL, -14),
            task("Draft hero copy and CTA text", TaskStatus.DONE, TaskPriority.LOW, -12),
            task("Set up the project repository", TaskStatus.DONE, TaskPriority.NORMAL, -20),
            task("Agree colour palette with the client", TaskStatus.DONE, TaskPriority.HIGH, null)
        ),
        commentCount = 3
    )

    /** 10 tasks, 3 done. */
    private fun loginApi() = Project(
        tag = "Backend",
        name = "Login API",
        description = "Build authentication endpoints and JWT token refresh handlers.",
        attachments = mutableListOf(
            AttachmentLink(catalogId = "cat_meeting_notes"),
            AttachmentLink(catalogId = "cat_wireframe_v3")
        ),
        tasks = mutableListOf(
            task("Design the token refresh flow", TaskStatus.DONE, TaskPriority.HIGH, -9),
            task("Write the sign in endpoint", TaskStatus.DONE, TaskPriority.HIGH, -6),
            task("Write the sign out endpoint", TaskStatus.DONE, TaskPriority.NORMAL, -4),
            task("Add rate limiting to the login route", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, 1),
            task("Hash and salt stored passwords", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, -1),
            task("Add refresh token rotation", TaskStatus.TODO, TaskPriority.NORMAL, 4),
            task("Write integration tests for auth", TaskStatus.TODO, TaskPriority.NORMAL, 8),
            task("Document the API for the front end team", TaskStatus.TODO, TaskPriority.LOW, null),
            task("Add password reset by email", TaskStatus.TODO, TaskPriority.LOW, 12),
            task("Review error codes with the client", TaskStatus.TODO, TaskPriority.NORMAL, 2)
        ),
        commentCount = 2
    )

    /** 8 tasks, 2 done. */
    private fun mobileDashboard() = Project(
        tag = "Frontend",
        name = "Mobile Dashboard",
        description = "Responsive overview widgets and telemetry cards.",
        attachments = mutableListOf(
            AttachmentLink(catalogId = "cat_colour_palette")
        ),
        tasks = mutableListOf(
            task("Build the widget grid", TaskStatus.DONE, TaskPriority.NORMAL, -5),
            task("Add the telemetry card component", TaskStatus.DONE, TaskPriority.NORMAL, -3),
            task("Hook the cards to live data", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, 2),
            task("Add a loading state to each widget", TaskStatus.IN_PROGRESS, TaskPriority.NORMAL, 5),
            task("Handle empty telemetry gracefully", TaskStatus.TODO, TaskPriority.NORMAL, -3),
            task("Add pull to refresh", TaskStatus.TODO, TaskPriority.LOW, 9),
            task("Dark mode pass on the dashboard", TaskStatus.TODO, TaskPriority.LOW, null),
            task("Performance check on older devices", TaskStatus.TODO, TaskPriority.HIGH, 7)
        ),
        commentCount = 1
    )

    /** 4 tasks, all done, so this one shows 100%. */
    private fun releaseChecklist() = Project(
        tag = "QA & Ops",
        name = "Release Checklist",
        description = "Pre-production verification and smoke tests.",
        attachments = mutableListOf(
            AttachmentLink(catalogId = "cat_launch_checklist")
        ),
        tasks = mutableListOf(
            task("Run the full smoke test suite", TaskStatus.DONE, TaskPriority.HIGH, -2),
            task("Verify the staging database backup", TaskStatus.DONE, TaskPriority.HIGH, -2),
            task("Check rollback instructions are current", TaskStatus.DONE, TaskPriority.NORMAL, -1),
            task("Confirm the on call rota for launch week", TaskStatus.DONE, TaskPriority.NORMAL, -1)
        ),
        commentCount = 2
    )

    /** No tasks and no attachments, so the empty states can be demonstrated. */
    private fun designAssets() = Project(
        tag = "Design",
        name = "Design Assets",
        description = "Vector icons and illustration library for landing pages.",
        attachments = mutableListOf(),
        tasks = mutableListOf(),
        commentCount = 0
    )

    /** daysFromToday is null for a task with no due date. */
    private fun task(
        description: String,
        status: TaskStatus,
        priority: TaskPriority,
        daysFromToday: Int?
    ) = TaskItem(
        description = description,
        status = status,
        priority = priority,
        dueDate = daysFromToday?.let { DateUtils.daysFromToday(it) }
    )
}
