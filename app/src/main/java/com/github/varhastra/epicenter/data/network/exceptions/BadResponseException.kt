package com.github.varhastra.epicenter.data.network.exceptions

class BadResponseException(
        message: String = "Bad response.",
        cause: Throwable? = null
) : ServiceProviderException(message, cause)