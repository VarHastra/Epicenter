package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.presentation.common.UnitsLocale

interface UnitsLocaleRepository {

//    fun getPreferredUnits(context: Context = App.instance): UnitsLocale

    val preferredUnits: UnitsLocale
}