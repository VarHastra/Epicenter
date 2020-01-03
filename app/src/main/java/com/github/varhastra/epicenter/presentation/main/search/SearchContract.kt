package com.github.varhastra.epicenter.presentation.main.search

import com.github.varhastra.epicenter.presentation.BasePresenter
import com.github.varhastra.epicenter.presentation.BaseView

interface SearchContract {

    interface View : BaseView<Presenter> {

    }

    interface Presenter : BasePresenter {

    }
}