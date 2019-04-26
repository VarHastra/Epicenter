package com.github.varhastra.epicenter.main

import com.github.varhastra.epicenter.domain.model.Place

interface ToolbarProvider {

    fun setTitleText(text: String)

    fun setDropdownText(text: String)

    fun showDropdown(show: Boolean)

    fun attachListener(listener: (Place) -> Unit)

    fun attachOnEditListener(listener: () -> Unit)

    fun setDropdownData(places: List<Place>)
}