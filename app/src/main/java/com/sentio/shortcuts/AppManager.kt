package com.sentio.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ComponentInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager.MATCH_ALL
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.support.annotation.RequiresApi
import android.util.ArrayMap
import android.util.Log
import java.util.*


class AppManager(private val context: Context) {
    private val TAG = AppManager::class.java.simpleName
    private val CACHE_SIZE = 100
    private val appIconCache = ArrayMap<String, Drawable>(CACHE_SIZE)
    private val shortcutIconCache = ArrayMap<String, Drawable>(CACHE_SIZE)
    private val packageManager = context.packageManager
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    var launchWithId : Boolean = true

    fun getLaunchableApps(): List<App> {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, MATCH_ALL)
                .map { it.activityInfo }
                .map { App(it.packageName, it.loadLabel(packageManager).toString(), it) }
    }

    fun getShortcutFromApp(packageName: String, shortcutType: Int): List<Shortcut> {
        val shortcutQuery = LauncherApps.ShortcutQuery()
//        shortcutQuery.setQueryFlags(FLAG_MATCH_DYNAMIC or FLAG_MATCH_MANIFEST or FLAG_MATCH_PINNED)
//        shortcutQuery.setQueryFlags(FLAG_MATCH_MANIFEST)
        shortcutQuery.setQueryFlags(shortcutType)
        shortcutQuery.setPackage(packageName)
        return try {
            launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle())
                    .map { Shortcut(it.id, it.`package`, it.shortLabel.toString(), it) }
        } catch (e: SecurityException) {
            Collections.emptyList()
        }
    }

    fun startApp(app: App) {
        context.startActivity(packageManager.getLaunchIntentForPackage(app.packageName))
    }

    fun getAppIcon(componentInfo: ComponentInfo): Drawable?
            = appIconCache[componentInfo.packageName] ?: loadAppIcon(componentInfo)

    private fun loadAppIcon(componentInfo: ComponentInfo): Drawable? {
        return try {
            val drawable = componentInfo.loadIcon(packageManager)
            shortcutIconCache[componentInfo.packageName] = drawable
            drawable
        } catch (e: SecurityException) {
            Log.e(TAG, e.message)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startShortcut(shortcut: Shortcut) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        var intent = shortcutManager?.createShortcutResultIntent(shortcut.shortcutInfo)
//        intent = Intent("myAction")
//        intent.type = PHONE.CONTENT_TYPE
        if (launchWithId) {
            Log.i("benas", "launch with id, $shortcut")
            launcherApps.startShortcut(shortcut.packageName, shortcut.id, null, null, Process.myUserHandle())
        }
        else if (intent != null) {
            Log.i("benas", "action = " + intent.action)
            for ((count, i) in intent.categories.withIndex()) {
                Log.i("benas", "category $count = $i")
            }
            if (intent.extras != null) {
                for ((count, i) in intent.extras!!.keySet().withIndex()) {
                    Log.i("benas", "extra $count, key = $i, value = " + intent.extras?.get(i))
                }
            } else {
                Log.i("benas", "extras = null")
            }
            Log.i("benas", "scheme = " + intent.scheme)
            context.startActivity(intent)
        } else {
            Log.i("benas", "intent = null")
        }
//        else if (shortcut.shortcutInfo.intent != null) {
//            Log.i("benas", "action = " + shortcut.shortcutInfo.intent!!.action)
//            for ((count, i) in shortcut.shortcutInfo.intent!!.categories.withIndex()) {
//                Log.i("benas", "category $count = $i")
//            }
//            if (shortcut.shortcutInfo.intent!!.extras != null) {
//                for ((count, i) in shortcut.shortcutInfo.intent!!.extras!!.keySet().withIndex()) {
//                    Log.i("benas", "extra $count, key = $i, value = " + shortcut.shortcutInfo.intent!!.extras?.get(i))
//                }
//            } else {
//                Log.i("benas", "extras = null")
//            }
//            Log.i("benas", "scheme = " + shortcut.shortcutInfo.intent!!.scheme)
//            context.startActivity(shortcut.shortcutInfo.intent)
//        } else {
//            Log.i("benas", "intent = null")
//        }
    }

    fun getShortcutIcon(shortcutInfo: ShortcutInfo)
            = shortcutIconCache[shortcutIdentity(shortcutInfo)] ?: loadShortcutIcon(shortcutInfo)

    private fun shortcutIdentity(shortcutInfo: ShortcutInfo)
            = "${shortcutInfo.`package`}/${shortcutInfo.id}"

    private fun loadShortcutIcon(shortcutInfo: ShortcutInfo): Drawable? {
        return try {
            val drawable = launcherApps.getShortcutIconDrawable(shortcutInfo,
                    context.resources.displayMetrics.densityDpi)
            shortcutIconCache[shortcutIdentity(shortcutInfo)] = drawable
            drawable
        } catch (e: SecurityException) {
            Log.e(TAG, e.message)
            null
        }
    }
}
