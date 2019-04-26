package com.github.varhastra.epicenter.domain.interactors

interface InteractorCallback<R> {
    fun onResult(result: R)

    fun onFailure(t: Throwable?)
}