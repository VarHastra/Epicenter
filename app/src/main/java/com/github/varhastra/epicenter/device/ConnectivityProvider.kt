package com.github.varhastra.epicenter.device

import android.content.Context
import android.net.ConnectivityManager
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.domain.repos.ConnectivityRepository

class ConnectivityProvider(val context: Context = App.instance) : ConnectivityRepository {

    override fun isNetworkConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }
}