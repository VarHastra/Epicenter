package com.github.varhastra.epicenter.data.network.exceptions

class UnknownException(
        message: String = "Unknown exception.",
        cause: Throwable? = null
) : ServiceProviderException(message, cause)