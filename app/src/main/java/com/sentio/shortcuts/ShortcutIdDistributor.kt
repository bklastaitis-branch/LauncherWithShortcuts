package com.sentio.shortcuts

import android.app.Activity
import android.content.Intent
import android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
import android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
import android.net.Uri
import android.os.Bundle

class ShortcutIdDistributor : Activity() {
    private lateinit var appManager: AppManager

    // result intent to be sent to Branch search app, will store extras as follows: "packageName" = ["shortcut1", "shortcut2"]
    private val result = Intent("benas", Uri.parse("benas_search://open"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val shortcutType = intent.getIntExtra("shortcut_type", FLAG_MATCH_MANIFEST or FLAG_MATCH_DYNAMIC)

        appManager = AppManager(applicationContext)
        val apps: List<App> = appManager.getLaunchableApps()
        for (app in apps) {
            val shortcutIDs = appManager.getShortcutFromApp(app.packageName, shortcutType).map { it.id }.toTypedArray()
            if (shortcutIDs.isEmpty()) {
                continue
            }

            result.putExtra(app.packageName, shortcutIDs)
        }

        setResult(RESULT_OK, result)
        finish()
    }
}