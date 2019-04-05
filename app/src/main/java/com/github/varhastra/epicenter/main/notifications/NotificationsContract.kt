package com.github.varhastra.epicenter.main.notifications

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView

interface NotificationsContract {

    interface View : BaseView<Presenter> {

    }

    interface Presenter: BasePresenter {

    }
}