package me.alex.pet.apps.epicenter.presentation.main.map

import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.ViewGroup
import androidx.annotation.ColorRes
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.google.maps.android.ui.SquareTextView
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.getColorCompat
import me.alex.pet.apps.epicenter.presentation.common.AlertLevel
import me.alex.pet.apps.epicenter.presentation.common.EventMarker


class EventsRenderer(
        val context: Context,
        map: GoogleMap,
        clusterManager: ClusterManager<EventMarker>
) : DefaultClusterRenderer<EventMarker>(context, map, clusterManager) {

    private val clusterBgDrawable: ShapeDrawable = ShapeDrawable(OvalShape())

    private val clusterOutlineDrawable: ShapeDrawable = ShapeDrawable(OvalShape()).apply {
        alpha = 128
    }

    private val iconGenerator = IconGenerator(context).apply {
        setContentView(makeSquareTextView())
        setTextAppearance(R.style.ClusterIconTextAppearance)
        setBackground(makeClusterBackground())
    }

    private val icons = mutableMapOf<Pair<Int, AlertLevel>, BitmapDescriptor>()


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
        val alertLevel = findHighestAlertLevelIn(cluster)
        val bucket = getBucket(cluster)

        val descriptor = icons.getOrPut(bucket to alertLevel) {
            setClusterDrawablesColor(alertLevel.colorResId)
            BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(getClusterText(bucket)))
        }

        markerOptions.apply {
            icon(descriptor)
            zIndex(12.0f)
        }
    }

    private fun findHighestAlertLevelIn(cluster: Cluster<EventMarker>): AlertLevel {
        return cluster.items.map { it.alertLevel }.maxBy { it.value }
                ?: throw IllegalStateException("Empty cluster: $cluster.")
    }

    private fun setClusterDrawablesColor(@ColorRes resId: Int) {
        val color = context.getColorCompat(resId)
        clusterBgDrawable.paint.color = color
        clusterOutlineDrawable.paint.color = color
    }

    private fun makeSquareTextView(): SquareTextView {
        val density = context.resources.displayMetrics.density
        val params =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val twelveDpi = (12 * density).toInt()
        return SquareTextView(context).apply {
            layoutParams = params
            id = com.google.maps.android.R.id.amu_text
            setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi)
        }
    }

    private fun makeClusterBackground(): LayerDrawable {
        val density = context.resources.displayMetrics.density
        val strokeWidth = (density * 3).toInt()
        return LayerDrawable(arrayOf(clusterOutlineDrawable, clusterBgDrawable)).apply {
            setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth)
        }
    }
}