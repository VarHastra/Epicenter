package com.github.varhastra.epicenter.domain.state.placeeditor

class PlaceEditorState(
        val placeId: Int,
        val area: Area?
) {

    fun copy(placeId: Int = this.placeId, area: Area? = this.area) = PlaceEditorState(placeId, area)
}