package com.github.varhastra.epicenter.domain.state.placeeditor

class PlaceEditorState(
        val placeId: Int,
        val order: Int,
        val area: Area?
) {

    fun copy(placeId: Int = this.placeId, order: Int = this.order, area: Area? = this.area) = PlaceEditorState(placeId, order, area)
}