package com.github.varhastra.epicenter.ui.main.map

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView

interface MapContract {

    interface View : BaseView<Presenter> {

    }

    interface Presenter: BasePresenter {

    }
}