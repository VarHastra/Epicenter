package com.github.varhastra.epicenter.data.network.exceptions

abstract class ServiceProviderException(
        message: String = "",
        cause: Throwable? = null
) : RuntimeException(message, cause)