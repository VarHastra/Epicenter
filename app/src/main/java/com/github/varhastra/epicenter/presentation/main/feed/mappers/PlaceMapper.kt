package com.github.varhastra.epicenter.presentation.main.feed.mappers

import android.content.Context
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.presentation.common.UnitsFormatter
import com.github.varhastra.epicenter.presentation.common.UnitsLocale
import com.github.varhastra.epicenter.presentation.main.feed.PlaceViewBlock
import kotlin.math.roundToInt

class PlaceMapper(context: Context, unitsLocale: UnitsLocale) {

    private val unitsFormatter = UnitsFormatter(unitsLocale)

    private val nullRadiusStr = context.getString(R.string.feed_place_infinite)

    fun map(place: Place): PlaceViewBlock {
        val titleText = place.name

        val radiusText = if (place.radiusKm.isInfinite()) {
            nullRadiusStr
        } else {
            unitsFormatter.getLocalizedDistanceString(place.radiusKm.roundToInt())
        }

        val iconResId = when (place.id) {
            Place.CURRENT_LOCATION.id -> R.drawable.ic_place_near_me_24px
            Place.WORLD.id -> R.drawable.ic_place_world_24px
            else -> null
        }

        return PlaceViewBlock(
                place.id,
                titleText,
                radiusText,
                iconResId
        )
    }
}