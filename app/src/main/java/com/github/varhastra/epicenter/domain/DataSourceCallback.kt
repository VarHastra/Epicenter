package com.github.varhastra.epicenter.domain

interface DataSourceCallback<in T> {
    fun onResult(result: T)

    fun onFailure(t: Throwable?)
}