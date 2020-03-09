package com.github.varhastra.epicenter.presentation.placenamepicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.widget.doAfterTextChanged
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.extensions.longSnackbar
import com.github.varhastra.epicenter.data.LocationProvider
import com.github.varhastra.epicenter.domain.interactors.LoadLocationNameInteractor
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_place_name_picker.*

class PlaceNamePickerActivity : AppCompatActivity(), PlaceNamePickerContract.View {

    private lateinit var presenter: PlaceNamePickerContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_name_picker)

        setUpViews()

        val presenter = PlaceNamePickerPresenter(this, LoadLocationNameInteractor(LocationProvider()))

        val lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
        val lng = intent.getDoubleExtra(EXTRA_LNG, 0.0)
        presenter.initialize(lat, lng)
    }

    private fun setUpViews() {
        saveFab.setOnClickListener { presenter.saveAndExit() }

        nameEditText.doAfterTextChanged { editable ->
            presenter.setPlaceName(editable.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    override fun attachPresenter(presenter: PlaceNamePickerContract.Presenter) {
        this.presenter = presenter
    }

    override fun isActive() = !(isFinishing || isDestroyed)

    override fun showSuggestedName(suggestedName: String) {
        nameEditText.apply {
            setText(suggestedName)
            setSelection(nameEditText.length())
        }
    }

    override fun showErrorEmptyName() {
        nameEditText.longSnackbar(R.string.place_name_picker_error_empty_name)
    }

    override fun navigateBackWithResult(placeName: String) {
        val data = Intent().apply {
            putExtra(RESULT_NAME, placeName)
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {
        private const val EXTRA_LAT = "EXTRA_LAT"
        private const val EXTRA_LNG = "EXTRA_LNG"
        const val RESULT_NAME = "RESULT_NAME"

        fun start(sourceActivity: Activity, location: LatLng, requestCode: Int = -1) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(sourceActivity).toBundle()
            val intent = Intent(sourceActivity, PlaceNamePickerActivity::class.java).apply {
                putExtra(EXTRA_LAT, location.latitude)
                putExtra(EXTRA_LNG, location.longitude)
            }
            sourceActivity.startActivityForResult(intent, requestCode, options)
        }
    }
}
