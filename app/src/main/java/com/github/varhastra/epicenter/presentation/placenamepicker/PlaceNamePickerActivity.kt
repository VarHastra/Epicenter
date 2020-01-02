package com.github.varhastra.epicenter.presentation.placenamepicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.device.LocationProvider
import com.github.varhastra.epicenter.domain.model.Coordinates
import kotlinx.android.synthetic.main.activity_place_name_picker.*
import org.jetbrains.anko.design.longSnackbar

class PlaceNamePickerActivity : AppCompatActivity(), PlaceNamePickerContract.View {

    lateinit var presenter: PlaceNamePickerContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_name_picker)

        saveFab.setOnClickListener { presenter.saveAndExit() }

        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                presenter.setPlaceName(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }
        })

        val presenter = PlaceNamePickerPresenter(this, LocationProvider())
        intent?.apply {
            val lat = getDoubleExtra(EXTRA_LAT, 0.0)
            val lng = getDoubleExtra(EXTRA_LNG, 0.0)
            presenter.initialize(Coordinates(lat, lng))
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
        nameEditText.setText(suggestedName)
        nameEditText.setSelection(nameEditText.length())
    }

    override fun showErrorEmptyName() {
        nameEditText.longSnackbar(R.string.place_name_picker_error_empty_name)
    }

    override fun navigateBackWithResult(placeName: String) {
        val data = Intent()
        data.putExtra(RESULT_NAME, placeName)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {
        const val EXTRA_LAT = "EXTRA_LAT"
        const val EXTRA_LNG = "EXTRA_LNG"
        const val RESULT_NAME = "RESULT_NAME"
    }
}
