package me.alex.pet.apps.epicenter.data

import android.content.Context
import android.net.ConnectivityManager
import me.alex.pet.apps.epicenter.domain.repos.ConnectivityRepository

class ConnectivityProvider(val context: Context) : ConnectivityRepository {

    override fun isNetworkConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }
}