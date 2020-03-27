package me.alex.pet.apps.epicenter.presentation.main

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.alex.pet.apps.epicenter.R

class MainModel : ViewModel() {

    val selectedDestination: LiveData<TabDestination>
        get() = _selectedDestination
    private val _selectedDestination = MutableLiveData<TabDestination>()

    init {
        _selectedDestination.value = TabDestination.MAP
    }

    fun onChangeDestination(@IdRes id: Int) {
        if (_selectedDestination.value!!.id == id) {
            return
        }
        val destination = TabDestination.values().find { it.id == id }
                ?: throw IllegalArgumentException("Unknown destination id: $id.")
        _selectedDestination.value = destination
    }
}

enum class TabDestination(@IdRes val id: Int, val tag: String) {
    FEED(R.id.navigation_feed, "FEED"),
    MAP(R.id.navigation_map, "MAP")
}