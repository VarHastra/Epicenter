package com.github.varhastra.epicenter.domain.interactors

interface FunctionalInteractor<T, R> {

    var onResult: ((R) -> Unit)?
    var onFailure: ((Throwable?) -> Unit)?

    fun execute(arg: T)
}