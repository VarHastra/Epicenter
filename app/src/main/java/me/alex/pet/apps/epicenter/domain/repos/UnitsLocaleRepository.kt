package me.alex.pet.apps.epicenter.domain.repos

import me.alex.pet.apps.epicenter.presentation.common.UnitsLocale

interface UnitsLocaleRepository {

    val preferredUnits: UnitsLocale
}