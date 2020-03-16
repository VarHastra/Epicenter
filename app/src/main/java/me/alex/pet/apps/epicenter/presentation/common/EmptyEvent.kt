package me.alex.pet.apps.epicenter.presentation.common

class EmptyEvent {
    var isConsumed = false
        private set

    fun consume(f: () -> Unit) {
        if (isConsumed) return
        f()
        isConsumed = true
    }
}