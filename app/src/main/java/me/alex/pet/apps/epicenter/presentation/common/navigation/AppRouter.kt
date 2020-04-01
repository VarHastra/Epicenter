package me.alex.pet.apps.epicenter.presentation.common.navigation

class AppRouter : Router {

    private var navigator: Navigator? = null

    override fun navigateTo(destination: Destination) {
        val navigator = navigator
                ?: throw IllegalStateException("Router $this doesn't have a navigator attached to it.")
        navigator.navigateTo(destination)
    }

    override fun navigateBack() {
        val navigator = navigator
                ?: throw IllegalStateException("Router $this doesn't have a navigator attached to it.")
        navigator.navigateBack()
    }

    override fun attachNavigator(navigator: Navigator) {
        if (this.navigator != null) {
            throw IllegalStateException("Router $this already has a navigator ($navigator) attached to it.")
        }
        this.navigator = navigator
    }

    override fun detachNavigator() {
        if (this.navigator == null) {
            throw IllegalStateException("Router $this doesn't have a navigator attached to it.")
        }
        this.navigator = null
    }
}