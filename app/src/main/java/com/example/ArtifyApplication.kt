package com.example

import android.app.Application
import android.content.Intent
import android.os.Process
import kotlin.system.exitProcess

/**
 * Installs a process-wide crash handler for internal testing builds. Sideloaded debug APKs
 * have no Play Console crash reporting, and the OEM's own "app has stopped" dialog on many
 * devices shows only a generic message with no technical detail — leaving testers unable to
 * report anything actionable. This catches the crash before the OS does, launches
 * CrashReportActivity with the full exception, then terminates the process cleanly.
 */
class ArtifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val details = buildString {
                    appendLine(throwable::class.java.name)
                    appendLine(throwable.message ?: "(no message)")
                    appendLine()
                    appendLine(throwable.stackTraceToString())
                    var cause = throwable.cause
                    while (cause != null) {
                        appendLine()
                        appendLine("Caused by: ${cause::class.java.name}: ${cause.message}")
                        appendLine(cause.stackTraceToString())
                        cause = cause.cause
                    }
                }
                val intent = Intent(applicationContext, CrashReportActivity::class.java).apply {
                    putExtra(CrashReportActivity.EXTRA_DETAILS, details)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } catch (e: Exception) {
                // If even the crash screen fails to launch, fall through to the platform handler.
                defaultHandler?.uncaughtException(thread, throwable)
            } finally {
                Process.killProcess(Process.myPid())
                exitProcess(1)
            }
        }
    }
}
