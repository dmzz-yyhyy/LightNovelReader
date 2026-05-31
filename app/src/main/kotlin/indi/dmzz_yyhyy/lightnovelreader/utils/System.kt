package indi.dmzz_yyhyy.lightnovelreader.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import kotlin.system.exitProcess


fun restart(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?: return

    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

    Handler(Looper.getMainLooper()).postDelayed({
        context.startActivity(launchIntent)
        if (context is Activity) {
            context.finishAffinity()
        }
        exitProcess(0)
    }, 500)
}