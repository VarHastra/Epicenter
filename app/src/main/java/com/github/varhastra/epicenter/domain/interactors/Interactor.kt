package com.github.varhastra.epicenter.domain.interactors

interface Interactor<T, R> {

    fun execute(arg: T, callback: InteractorCallback<R>)
}