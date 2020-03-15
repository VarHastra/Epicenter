package me.alex.pet.apps.epicenter.presentation.common

class Event<T>(private val data: T) {
    var isConsumed = false
        private set

    fun consume(f: (T) -> Unit) {
        if (isConsumed) throw IllegalStateException("This event has already been consumed.")
        f(data)
        isConsumed = true
    }

    fun consume(): T {
        if (isConsumed) throw IllegalStateException("This event has already been consumed.")
        isConsumed = true
        return data
    }
}