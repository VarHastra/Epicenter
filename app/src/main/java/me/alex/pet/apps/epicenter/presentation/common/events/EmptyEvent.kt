package me.alex.pet.apps.epicenter.presentation.common.events

class EmptyEvent {
    var isConsumed = false
        private set

    fun consume(f: () -> Unit) {
        if (isConsumed) return
        f()
        isConsumed = true
    }
}