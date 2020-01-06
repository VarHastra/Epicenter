package com.github.varhastra.epicenter.data.network.exceptions

class NoNetworkConnectionException(
        message: String = "No network connection.",
        cause: Throwable? = null
) : ServiceProviderException(message, cause)