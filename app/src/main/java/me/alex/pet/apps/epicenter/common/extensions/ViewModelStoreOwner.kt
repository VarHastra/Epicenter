package me.alex.pet.apps.epicenter.common.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import org.koin.androidx.viewmodel.koin.getViewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.java.KoinJavaComponent.getKoin
import kotlin.reflect.KClass

fun <T : ViewModel> Fragment.parentViewModel(
        clazz: KClass<T>,
        qualifier: Qualifier? = null,
        parameters: ParametersDefinition? = null
): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) {
        val parent: ViewModelStoreOwner = parentFragment ?: throw IllegalStateException()
        getKoin().getViewModel(parent, clazz, qualifier, parameters)
    }
}

inline fun <reified T : ViewModel> Fragment.parentViewModel(
        qualifier: Qualifier? = null,
        noinline parameters: ParametersDefinition? = null
): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) {
        val parent: ViewModelStoreOwner = parentFragment ?: throw IllegalStateException()
        getKoin().getViewModel(parent, T::class, qualifier, parameters)
    }
}