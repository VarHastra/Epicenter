package me.alex.pet.apps.epicenter.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_settings.*
import me.alex.pet.apps.epicenter.R

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.apply {
            setTitle(R.string.app_settings)
            setNavigationIcon(R.drawable.ic_up)
        }
    }

    override fun onStart() {
        super.onStart()
        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
    }


    companion object {

        fun newInstance(): Fragment = SettingsFragment()
    }
}
