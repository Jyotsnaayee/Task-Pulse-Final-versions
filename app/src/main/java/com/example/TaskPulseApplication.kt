package com.example

import android.app.Application
import com.example.ads.AdManager

class TaskPulseApplication : Application() {

    lateinit var adManager: AdManager
        private set

    override fun onCreate() {
        super.onCreate()
        adManager = AdManager(this)
    }
}
