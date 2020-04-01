package me.alex.pet.apps.epicenter.presentation.placeeditor.namepicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_name_picker.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.longSnackbar
import me.alex.pet.apps.epicenter.common.extensions.observe
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NamePickerActivity : AppCompatActivity() {

    private val model: NamePickerModel by viewModel {
        val lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
        val lng = intent.getDoubleExtra(EXTRA_LNG, 0.0)
        parametersOf(lat, lng)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_picker)

        observeModel()
    }

    private fun observeModel() = with(model) {
        name.observe(this@NamePickerActivity, ::renderName)
        transientErrorEvent.observe(this@NamePickerActivity) { event ->
            event.consume { msgResId -> renderTransientError(msgResId) }
        }
        navigateBackEvent.observe(this@NamePickerActivity) { event ->
            event.consume { placeName -> navigateBackWithResult(placeName) }
        }
    }

    override fun onStart() {
        super.onStart()

        saveFab.setOnClickListener { model.onSaveAndExit() }
        nameEditText.doAfterTextChanged { editable ->
            model.onChangeName(editable.toString())
        }
    }

    private fun renderName(name: String) {
        if (nameEditText.text.toString() == name) {
            return
        }
        nameEditText.apply {
            setText(name)
            setSelection(nameEditText.length())
        }
    }

    private fun renderTransientError(msgResId: Int) {
        nameEditText.longSnackbar(msgResId)
    }

    private fun navigateBackWithResult(placeName: String) {
        val data = Intent().apply {
            putExtra(RESULT_NAME, placeName)
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {
        const val RESULT_NAME = "RESULT_NAME"

        fun start(sourceActivity: Activity, location: LatLng, requestCode: Int = -1) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(sourceActivity).toBundle()
            val intent = Intent(sourceActivity, NamePickerActivity::class.java).apply {
                putExtra(EXTRA_LAT, location.latitude)
                putExtra(EXTRA_LNG, location.longitude)
            }
            sourceActivity.startActivityForResult(intent, requestCode, options)
        }
    }
}


private const val EXTRA_LAT = "EXTRA_LAT"
private const val EXTRA_LNG = "EXTRA_LNG"
