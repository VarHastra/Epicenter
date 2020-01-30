package com.github.varhastra.epicenter.presentation.main

import com.github.varhastra.epicenter.presentation.main.feed.PlaceViewBlock

interface ToolbarProvider {

    fun setTitleText(text: String)

    fun setDropdownText(text: String)

    fun showDropdown(show: Boolean)

    fun attachListener(listener: (PlaceViewBlock) -> Unit)

    fun attachOnEditListener(listener: () -> Unit)

    fun setDropdownData(places: List<PlaceViewBlock>)
}