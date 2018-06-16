package com.ebnbin.card16

import android.app.Application

class AppApplication : Application() {
    override fun onCreate() {
        instance = this

        super.onCreate()
    }

    companion object {
        lateinit var instance: AppApplication private set
    }
}
