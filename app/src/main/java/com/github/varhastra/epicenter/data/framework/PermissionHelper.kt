package com.github.varhastra.epicenter.data.framework

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.varhastra.epicenter.App

class PermissionHelper(val context: Context = App.instance) {

    private val requests: MutableMap<Int, PermissionRequest> = mutableMapOf()

    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun isExplanationNeeded(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    fun isExplanationNeeded(fragment: Fragment, permission: String): Boolean {
        return fragment.shouldShowRequestPermissionRationale(permission)
    }

    fun requestPermission(activity: Activity, permissionRequest: PermissionRequest) {
        requests[permissionRequest.requestCode] = permissionRequest
        ActivityCompat.requestPermissions(activity, permissionRequest.permissions, permissionRequest.requestCode)
    }

    fun requestPermission(fragment: Fragment, permissionRequest: PermissionRequest) {
        requests[permissionRequest.requestCode] = permissionRequest
        fragment.requestPermissions(permissionRequest.permissions, permissionRequest.requestCode)
    }

    fun dispatchRequestResults(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        requests[requestCode]?.apply {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                this.onResultCallback?.invoke()
            } else {
                this.onFailureCallback?.invoke()
            }
        }
    }
}