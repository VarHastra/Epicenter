package com.github.varhastra.epicenter.domain.repos

import android.content.Context
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.presentation.common.UnitsLocale

interface UnitsLocaleRepository {

    fun getPreferredUnits(context: Context = App.instance): UnitsLocale
}