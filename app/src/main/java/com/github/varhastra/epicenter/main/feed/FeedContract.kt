package com.github.varhastra.epicenter.main.feed

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView

interface FeedContract {

    interface View : BaseView<Presenter> {

    }

    interface Presenter: BasePresenter {

    }
}