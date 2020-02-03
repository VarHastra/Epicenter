package com.github.varhastra.epicenter.presentation.main.map

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.ViewGroup
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.extensions.getColorCompat
import com.github.varhastra.epicenter.presentation.common.AlertLevel
import com.github.varhastra.epicenter.presentation.common.EventMarker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.google.maps.android.ui.SquareTextView


class EventsRenderer(
        val context: Context,
        map: GoogleMap,
        clusterManager: ClusterManager<EventMarker>
) : DefaultClusterRenderer<EventMarker>(context, map, clusterManager) {

    // Unfortunately some members of DefaultClusterRenderer are private, including mIconGenerator, mDensity, mColoredCircleBackground.
    // We want to render our own cluster icons.
    // It is possible to create our own full implementation of DefaultClusterRenderer, however it
    // internally relies on some private functions in other classes. So the quickest and the most
    // straightforward solution here is to just create our own IconGenerator, and other things that copy
    // the behavior of their counterparts in the DefaultClusterRenderer.
    private val density = context.resources.displayMetrics.density

    private lateinit var coloredCircleBackground: ShapeDrawable
    private lateinit var outline: ShapeDrawable

    private val iconGenerator = IconGenerator(context)

    /**
     * Cluster icons cache. It distinguishes icons by their bucket and alert level.
     */
    private val icons = mutableMapOf<Pair<Int, AlertLevel?>, BitmapDescriptor>()


    init {
        iconGenerator.setContentView(makeSquareTextView(context))
        iconGenerator.setTextAppearance(R.style.ClusterIconTextAppearance)
        iconGenerator.setBackground(makeClusterBackground())
    }


    override fun onBeforeClusterItemRendered(item: EventMarker, markerOptions: MarkerOptions) {
        val icon = BitmapDescriptorFactory.fromResource(item.alertLevel.markerResId)
        markerOptions.apply {
            icon(icon)
            zIndex(item.zIndex)
            alpha(item.alpha)
            anchor(0.5f, 0.5f)
        }
    }

    override fun onBeforeClusterRendered(cluster: Cluster<EventMarker>, markerOptions: MarkerOptions) {
        val maxAlertLevel = cluster.items.map { it.alertLevel }.maxBy { it.value }
                ?: AlertLevel.LEVEL_0
        val bucket = getBucket(cluster)

        var descriptor: BitmapDescriptor? = icons[bucket to maxAlertLevel]
        if (descriptor == null) {
            val color = context.getColorCompat(maxAlertLevel.colorResId)
            coloredCircleBackground.paint.color = color
            outline.paint.color = color
            descriptor = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(getClusterText(bucket)))
            icons[bucket to maxAlertLevel] = descriptor
        }

        markerOptions.icon(descriptor)
        markerOptions.zIndex(12.0f)
    }

    private fun makeSquareTextView(context: Context): SquareTextView {
        val squareTextView = SquareTextView(context)
        val layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        squareTextView.layoutParams = layoutParams
        squareTextView.id = com.google.maps.android.R.id.amu_text
        val twelveDpi = (12 * density).toInt()
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi)
        return squareTextView
    }

    private fun makeClusterBackground(): LayerDrawable {
        coloredCircleBackground = ShapeDrawable(OvalShape())
        outline = ShapeDrawable(OvalShape())
        outline.alpha = 128
        val background = LayerDrawable(arrayOf<Drawable>(outline, coloredCircleBackground))
        val strokeWidth = (density * 3).toInt()
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth)
        return background
    }
}