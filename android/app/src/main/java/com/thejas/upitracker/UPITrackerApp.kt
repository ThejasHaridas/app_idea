package com.thejas.upitracker

import android.app.Application
import com.thejas.upitracker.data.db.AppDatabase

class UPITrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getInstance(this) // triggers seed on first launch
    }
}
