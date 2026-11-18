package com.example.taskflow;

/** Every Intent extra key lives here so no key string is ever typed twice. */
public final class IntentKeys {

    public static final String EXTRA_USER_NAME = "extra_user_name";
    public static final String EXTRA_WORKSPACE_NAME = "extra_workspace_name";
    public static final String EXTRA_WORKSPACE_DATE = "extra_workspace_date";

    /** The whole Project object, travelling in both directions. */
    public static final String EXTRA_PROJECT = "extra_project";

    /** Fragment argument: which project this fragment is showing. */
    public static final String ARG_PROJECT_ID = "arg_project_id";

    private IntentKeys() {
        // Constants holder, never instantiated.
    }
}
