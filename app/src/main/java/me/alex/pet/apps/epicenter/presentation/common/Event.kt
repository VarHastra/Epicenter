package me.alex.pet.apps.epicenter.presentation.common

class Event<T>(private val data: T) {
    var isConsumed = false
        private set

    fun consume(f: (T) -> Unit) {
        if (isConsumed) return
        f(data)
        isConsumed = true
    }
}