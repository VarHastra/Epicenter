package com.github.varhastra.epicenter

interface BaseView<T> {

    fun attachPresenter(presenter: T)
}