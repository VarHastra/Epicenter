package me.alex.pet.apps.epicenter.presentation.placeeditor.namepicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_name_picker.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.longSnackbar
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.common.extensions.parentViewModel
import me.alex.pet.apps.epicenter.presentation.placeeditor.PlaceEditorModel

class NamePickerFragment : Fragment() {

    private val model: PlaceEditorModel by parentViewModel()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_name_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationIcon(R.drawable.ic_up)

        observeModel()
    }

    private fun observeModel() = with(model) {
        name.observe(viewLifecycleOwner, ::renderName)
        transientErrorEvent.observe(viewLifecycleOwner) { event ->
            event.consume { msgResId -> renderTransientError(msgResId) }
        }
    }

    override fun onStart() {
        super.onStart()

        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        saveFab.setOnClickListener {
            model.onSaveAndExit()
        }
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


    companion object {
        fun newInstance(): Fragment = NamePickerFragment()
    }
}