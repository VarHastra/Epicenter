package me.alex.pet.apps.epicenter.presentation

interface BaseView<T> {

    fun attachPresenter(presenter: T)
}