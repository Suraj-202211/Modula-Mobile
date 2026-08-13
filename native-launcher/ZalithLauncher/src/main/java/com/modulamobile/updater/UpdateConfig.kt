package com.modulamobile.updater

import com.movtery.zalithlauncher.BuildConfig

object UpdateConfig {

    val GITHUB_TOKEN: String
        get() = BuildConfig.GITHUB_TOKEN

    const val OWNER = "Suraj-202211"
    const val REPO  = "Modula-Mobile"

    // GitHub API URL for update config file
    val UPDATE_CHECK_URL =
        "https://api.github.com/repos/" +
        "$OWNER/$REPO/contents/updates/latest.json"

    // Check every 6 hours
    const val CHECK_INTERVAL_HOURS = 6L
}
