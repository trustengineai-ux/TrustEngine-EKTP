package com.trustengine.verifier

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrustEngineApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}