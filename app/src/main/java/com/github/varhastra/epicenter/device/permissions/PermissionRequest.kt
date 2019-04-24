package com.github.varhastra.epicenter.device.permissions

class PermissionRequest private constructor(
    val requestCode: Int,
    val permissions: Array<out String>,
    val onResultCallback: (() -> Unit)?,
    val onFailureCallback: (() -> Unit)?
) {

    class Builder() {
        private var requestCode: Int = 0
        private var permissions: Array<out String> = arrayOf()
        private var onResultCallback: (() -> Unit)? = null
        private var onFailureCallback: (() -> Unit)? = null

        fun withRequestCode(requestCode: Int): Builder {
            this.requestCode = requestCode
            return this
        }

        fun withPermissions(vararg permissions: String): Builder {
            this.permissions = permissions
            return this
        }

        fun onResult(callback: () -> Unit): Builder {
            onResultCallback = callback
            return this
        }

        fun onFailure(callback: () -> Unit): Builder {
            onFailureCallback = callback
            return this
        }

        fun build() = PermissionRequest(requestCode, permissions, onResultCallback, onFailureCallback)
    }
}