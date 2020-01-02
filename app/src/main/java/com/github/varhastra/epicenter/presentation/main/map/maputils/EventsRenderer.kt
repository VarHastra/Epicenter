package com.github.varhastra.epicenter.presentation.main.map.maputils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.presentation.main.map.AlertLevel
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
        clusterManager: ClusterManager<EventClusterItem>
) : DefaultClusterRenderer<EventClusterItem>(context, map, clusterManager) {

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


    override fun onBeforeClusterItemRendered(item: EventClusterItem, markerOptions: MarkerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(getMarkerResource(item.alertLevel)))
        markerOptions.zIndex(item.alertLevel.id.toFloat())
        markerOptions.alpha(item.alpha)
        markerOptions.anchor(0.5f, 0.5f)
    }

    override fun onBeforeClusterRendered(cluster: Cluster<EventClusterItem>, markerOptions: MarkerOptions) {
        val maxAlertLevel = cluster.items.maxBy { it.alertLevel.id }?.alertLevel
        val bucket = getBucket(cluster)

        var descriptor: BitmapDescriptor? = icons[bucket to maxAlertLevel]
        if (descriptor == null) {
            val color = getColor(maxAlertLevel)
            coloredCircleBackground.paint.color = color
            outline.paint.color = color
            descriptor = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(getClusterText(bucket)))
            icons[bucket to maxAlertLevel] = descriptor
        }

        markerOptions.icon(descriptor)
        markerOptions.zIndex(12.0f)
    }

    @ColorInt
    private fun getColor(alertLevel: AlertLevel?) = ContextCompat.getColor(context, getColorResource(alertLevel))

    @ColorRes
    private fun getColorResource(alertLevel: AlertLevel?): Int {
        return when (alertLevel) {
            AlertLevel.ALERT_0 -> R.color.colorAlert0
            AlertLevel.ALERT_2 -> R.color.colorAlert2
            AlertLevel.ALERT_4 -> R.color.colorAlert4
            AlertLevel.ALERT_6 -> R.color.colorAlert6
            AlertLevel.ALERT_8 -> R.color.colorAlert8
            else -> R.color.colorAlert0
        }
    }

    @DrawableRes
    private fun getMarkerResource(alertLevel: AlertLevel?): Int {
        return when (alertLevel) {
            AlertLevel.ALERT_0 -> R.drawable.marker_0
            AlertLevel.ALERT_2 -> R.drawable.marker_2
            AlertLevel.ALERT_4 -> R.drawable.marker_4
            AlertLevel.ALERT_6 -> R.drawable.marker_6
            AlertLevel.ALERT_8 -> R.drawable.marker_8
            else -> R.drawable.marker_0
        }
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