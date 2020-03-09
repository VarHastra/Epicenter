package com.github.varhastra.epicenter

import android.app.Application
import com.chibatching.kotpref.Kotpref
import com.github.varhastra.epicenter.di.dataModule
import com.github.varhastra.epicenter.di.dbModule
import com.github.varhastra.epicenter.di.networkModule
import com.jakewharton.threetenabp.AndroidThreeTen
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        Kotpref.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(dataModule, dbModule, networkModule)
        }

        instance = this
    }


    companion object {
        lateinit var instance: App
            private set
    }
}