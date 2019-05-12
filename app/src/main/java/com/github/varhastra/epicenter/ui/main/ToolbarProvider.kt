package com.github.varhastra.epicenter.ui.main

import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.utils.UnitsLocale

interface ToolbarProvider {

    fun setTitleText(text: String)

    fun setDropdownText(text: String)

    fun showDropdown(show: Boolean)

    fun attachListener(listener: (Place) -> Unit)

    fun attachOnEditListener(listener: () -> Unit)

    fun setDropdownData(places: List<Place>, unitsLocale: UnitsLocale)
}