package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.utils.UnitsLocale

interface UnitsLocaleRepository {

    fun getPreferredUnitsLocale(): UnitsLocale
}