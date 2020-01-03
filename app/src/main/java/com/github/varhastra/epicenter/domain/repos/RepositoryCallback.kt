package com.github.varhastra.epicenter.domain.repos

interface RepositoryCallback<in T> {
    fun onResult(result: T)

    fun onFailure(t: Throwable?)
}