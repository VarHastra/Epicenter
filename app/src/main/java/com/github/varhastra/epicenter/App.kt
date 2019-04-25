package com.github.varhastra.epicenter

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        instance = this
    }


    companion object {
        lateinit var instance: App
            private set
    }
}