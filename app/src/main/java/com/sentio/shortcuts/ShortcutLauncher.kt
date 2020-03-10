package com.sentio.shortcuts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Bundle
import android.os.Process

class ShortcutLauncher : Activity() {
    private lateinit var launcherApps : LauncherApps

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

        val benasPackageName = intent.getStringExtra("package_name")
        val benasShortcut = intent.getStringExtra("shortcut_id")

        val result = Intent("benas", Uri.parse("benas_search://open"))

        if (benasPackageName != null && benasShortcut != null) {
            launcherApps.startShortcut(benasPackageName, benasShortcut, null, null, Process.myUserHandle())
            setResult(RESULT_OK, result)
        } else {
            setResult(RESULT_CANCELED, result)
        }

        finish()
    }
}