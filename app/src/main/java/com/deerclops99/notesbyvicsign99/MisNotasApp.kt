package com.deerclops99.notesbyvicsign99

import android.app.Application
import com.deerclops99.notesbyvicsign99.settings.AppSettings
class MisNotasApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppSettings.applySavedTheme(this)
    }
}