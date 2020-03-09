package com.github.varhastra.epicenter.presentation.places

import android.content.Context
import androidx.annotation.DrawableRes
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.presentation.common.UnitsFormatter
import com.github.varhastra.epicenter.presentation.common.UnitsLocale

class PlaceViewBlock(
        val id: Int,
        val title: String,
        val caption: String,
        @DrawableRes val iconResId: Int,
        val isDraggable: Boolean,
        val isDeletable: Boolean
)

class Mapper(private val context: Context, unitsLocale: UnitsLocale) {

    private val unitsFormatter = UnitsFormatter(context, unitsLocale)

    fun map(place: Place): PlaceViewBlock {
        val titleText = place.name
        val captionText = if (place.isSpecial) {
            context.getString(R.string.places_radius_dash)
        } else {
            unitsFormatter.getLocalizedDistanceString(place.radiusKm)
        }

        val iconResId = when (place.id) {
            Place.CURRENT_LOCATION.id -> R.drawable.ic_place_near_me_24px
            Place.WORLD.id -> R.drawable.ic_place_world_24px
            else -> R.drawable.ic_drag_handle_24px
        }

        val isDeletable = !place.isSpecial
        val isDraggable = isDeletable

        return PlaceViewBlock(
                place.id,
                titleText,
                captionText,
                iconResId,
                isDeletable,
                isDraggable
        )
    }

    private val Place.isSpecial
        get() = id in setOf(Place.CURRENT_LOCATION.id, Place.WORLD.id)
}