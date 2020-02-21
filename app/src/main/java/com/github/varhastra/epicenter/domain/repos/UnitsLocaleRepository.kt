package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.presentation.common.UnitsLocale

interface UnitsLocaleRepository {

    val preferredUnits: UnitsLocale
}