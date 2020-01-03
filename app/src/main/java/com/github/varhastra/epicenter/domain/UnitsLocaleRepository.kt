package com.github.varhastra.epicenter.domain

import com.github.varhastra.epicenter.utils.UnitsLocale

interface UnitsLocaleRepository {

    fun getPreferredUnitsLocale(): UnitsLocale
}