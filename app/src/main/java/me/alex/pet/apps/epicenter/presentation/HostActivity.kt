package me.alex.pet.apps.epicenter.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.presentation.main.MainFragment

class HostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.hostContainer, MainFragment.newInstance(), "MAIN")
                    .commit()
        }
    }
}
