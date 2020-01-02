package com.github.varhastra.epicenter.presentation.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.github.varhastra.epicenter.R
import org.jetbrains.anko.find

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(find(R.id.tb_settings))

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.frame_content_settings, SettingsFragment())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }
}
