package com.example.taskflow.model;

import com.example.taskflow.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public final class SampleData {

    private SampleData() {
        // Utility class, never instantiated.
    }

    public static List<Project> projects() {
        List<Project> projects = new ArrayList<>();
        projects.add(servicePageWebsite());
        projects.add(loginApi());
        projects.add(mobileDashboard());
        projects.add(releaseChecklist());
        projects.add(designAssets());
        return projects;
    }

    private static Project servicePageWebsite() {
        Project project = new Project(
                "User Interface",
                "Service Page Website",
                "Make a page display about services for the company website in blue "
                        + "and red and provide some price information on each product card.");

        project.getAttachments().add(new AttachmentLink("cat_sports_project_v2"));
        project.getAttachments().add(new AttachmentLink("cat_briefing_from_client"));
        project.getAttachments().add(new AttachmentLink("cat_mount_asset"));

        List<TaskItem> tasks = project.getTasks();
        tasks.add(task("Build header layout", TaskStatus.TODO, TaskPriority.HIGH, 0));
        tasks.add(task("Add form validation", TaskStatus.TODO, TaskPriority.HIGH, 0));
        tasks.add(task("Create low fidelity service page website", TaskStatus.TODO, TaskPriority.NORMAL, -7));
        tasks.add(task("Write meta descriptions for each section", TaskStatus.TODO, TaskPriority.LOW, 5));
        tasks.add(task("Collect product card copy from client", TaskStatus.TODO, TaskPriority.NORMAL, null));
        tasks.add(task("Configure pricing table responsive grid", TaskStatus.IN_PROGRESS, TaskPriority.NORMAL, 3));
        tasks.add(task("Wire up the contact form endpoint", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, -2));
        tasks.add(task("Build responsive breakpoints for tablet", TaskStatus.IN_PROGRESS, TaskPriority.NORMAL, 6));
        tasks.add(task("Create brand guideline for website design", TaskStatus.DONE, TaskPriority.NORMAL, -14));
        tasks.add(task("Draft hero copy and CTA text", TaskStatus.DONE, TaskPriority.LOW, -12));
        tasks.add(task("Set up the project repository", TaskStatus.DONE, TaskPriority.NORMAL, -20));
        tasks.add(task("Agree colour palette with the client", TaskStatus.DONE, TaskPriority.HIGH, null));

        project.setCommentCount(3);
        return project;
    }

    private static Project loginApi() {
        Project project = new Project(
                "Backend",
                "Login API",
                "Build authentication endpoints and JWT token refresh handlers.");

        project.getAttachments().add(new AttachmentLink("cat_meeting_notes"));
        project.getAttachments().add(new AttachmentLink("cat_wireframe_v3"));

        List<TaskItem> tasks = project.getTasks();
        tasks.add(task("Design the token refresh flow", TaskStatus.DONE, TaskPriority.HIGH, -9));
        tasks.add(task("Write the sign in endpoint", TaskStatus.DONE, TaskPriority.HIGH, -6));
        tasks.add(task("Write the sign out endpoint", TaskStatus.DONE, TaskPriority.NORMAL, -4));
        tasks.add(task("Add rate limiting to the login route", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, 1));
        tasks.add(task("Hash and salt stored passwords", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, -1));
        tasks.add(task("Add refresh token rotation", TaskStatus.TODO, TaskPriority.NORMAL, 4));
        tasks.add(task("Write integration tests for auth", TaskStatus.TODO, TaskPriority.NORMAL, 8));
        tasks.add(task("Document the API for the front end team", TaskStatus.TODO, TaskPriority.LOW, null));
        tasks.add(task("Add password reset by email", TaskStatus.TODO, TaskPriority.LOW, 12));
        tasks.add(task("Review error codes with the client", TaskStatus.TODO, TaskPriority.NORMAL, 2));

        project.setCommentCount(2);
        return project;
    }

    private static Project mobileDashboard() {
        Project project = new Project(
                "Frontend",
                "Mobile Dashboard",
                "Responsive overview widgets and telemetry cards.");

        project.getAttachments().add(new AttachmentLink("cat_colour_palette"));

        List<TaskItem> tasks = project.getTasks();
        tasks.add(task("Build the widget grid", TaskStatus.DONE, TaskPriority.NORMAL, -5));
        tasks.add(task("Add the telemetry card component", TaskStatus.DONE, TaskPriority.NORMAL, -3));
        tasks.add(task("Hook the cards to live data", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, 2));
        tasks.add(task("Add a loading state to each widget", TaskStatus.IN_PROGRESS, TaskPriority.NORMAL, 5));
        tasks.add(task("Handle empty telemetry gracefully", TaskStatus.TODO, TaskPriority.NORMAL, -3));
        tasks.add(task("Add pull to refresh", TaskStatus.TODO, TaskPriority.LOW, 9));
        tasks.add(task("Dark mode pass on the dashboard", TaskStatus.TODO, TaskPriority.LOW, null));
        tasks.add(task("Performance check on older devices", TaskStatus.TODO, TaskPriority.HIGH, 7));

        project.setCommentCount(1);
        return project;
    }


    private static Project releaseChecklist() {
        Project project = new Project(
                "QA & Ops",
                "Release Checklist",
                "Pre-production verification and smoke tests.");

        project.getAttachments().add(new AttachmentLink("cat_launch_checklist"));

        List<TaskItem> tasks = project.getTasks();
        tasks.add(task("Run the full smoke test suite", TaskStatus.DONE, TaskPriority.HIGH, -2));
        tasks.add(task("Verify the staging database backup", TaskStatus.DONE, TaskPriority.HIGH, -2));
        tasks.add(task("Check rollback instructions are current", TaskStatus.DONE, TaskPriority.NORMAL, -1));
        tasks.add(task("Confirm the on call rota for launch week", TaskStatus.DONE, TaskPriority.NORMAL, -1));

        project.setCommentCount(2);
        return project;
    }

    private static Project designAssets() {
        return new Project(
                "Design",
                "Design Assets",
                "Vector icons and illustration library for landing pages.");
    }

    private static TaskItem task(String description, TaskStatus status,
                                 TaskPriority priority, Integer daysFromToday) {
        Long dueDate = daysFromToday == null ? null : DateUtils.daysFromToday(daysFromToday);
        return new TaskItem(description, status, priority, dueDate);
    }
}
