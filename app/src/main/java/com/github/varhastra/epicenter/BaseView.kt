package com.github.varhastra.epicenter

interface BaseView<T> {

    fun setPresenter(presenter: T)
}