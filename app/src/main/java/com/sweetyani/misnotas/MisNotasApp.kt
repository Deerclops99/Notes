package com.sweetyani.misnotas

import android.app.Application
import com.sweetyani.misnotas.settings.AppThemes
class MisNotasApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppThemes.applySaved(this)
    }
}