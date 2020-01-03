package com.github.varhastra.epicenter.presentation

interface BaseView<T> {

    fun attachPresenter(presenter: T)
}