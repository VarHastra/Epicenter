package com.github.varhastra.epicenter.data.network.exceptions

class BadResponseCodeException(
        val responseCode: Int,
        message: String = "Bad response code: $responseCode.",
        cause: Throwable? = null
) : ServiceProviderException(message, cause)