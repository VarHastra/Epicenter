package com.github.varhastra.epicenter.ui.main.search

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView

interface SearchContract {

    interface View : BaseView<Presenter> {

    }

    interface Presenter: BasePresenter {

    }
}