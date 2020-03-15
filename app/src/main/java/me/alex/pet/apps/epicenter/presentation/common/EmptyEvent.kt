package me.alex.pet.apps.epicenter.presentation.common

class EmptyEvent {
    var isConsumed = false
        private set

    fun consume(f: () -> Unit) {
        if (isConsumed) throw IllegalStateException("This event has already been consumed.")
        f()
        isConsumed = true
    }

    fun consume() {
        if (isConsumed) throw IllegalStateException("This event has already been consumed.")
        isConsumed = true
    }
}