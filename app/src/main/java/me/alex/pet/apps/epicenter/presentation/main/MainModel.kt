package me.alex.pet.apps.epicenter.presentation.main

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.alex.pet.apps.epicenter.R

class MainModel : ViewModel() {

    val selectedDestination: LiveData<TabDestination>
        get() = _selectedDestination
    private val _selectedDestination = MutableLiveData<TabDestination>()

    fun onRestoreState(savedInstanceState: Bundle?) {
        _selectedDestination.value = if (savedInstanceState == null) {
            TabDestination.FEED
        } else {
            val destId = savedInstanceState.getInt(STATE_DESTINATION_ID, TabDestination.FEED.id)
            val destination = TabDestination.values().find { it.id == destId }
                    ?: throw IllegalArgumentException("Unknown destination id: $destId.")
            destination
        }
    }

    fun onChangeDestination(@IdRes id: Int) {
        if (_selectedDestination.value!!.id == id) {
            return
        }
        val destination = TabDestination.values().find { it.id == id }
                ?: throw IllegalArgumentException("Unknown destination id: $id.")
        _selectedDestination.value = destination
    }

    fun onSaveState(outState: Bundle) {
        outState.putInt(STATE_DESTINATION_ID, _selectedDestination.value!!.id)
    }
}

enum class TabDestination(@IdRes val id: Int, val tag: String) {
    FEED(R.id.navigation_feed, "FEED"),
    MAP(R.id.navigation_map, "MAP")
}

private const val STATE_DESTINATION_ID = "DESTINATION_ID"