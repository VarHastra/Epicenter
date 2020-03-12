package me.alex.pet.apps.epicenter.common

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.execute
import com.chibatching.kotpref.pref.AbstractPref
import kotlin.reflect.KProperty

class DoublePref(
        val default: Double,
        override val key: String?,
        private val commitByDefault: Boolean
) : AbstractPref<Double>() {

    override fun getFromPreference(property: KProperty<*>, preference: SharedPreferences): Double {
        return preference.getDouble(preferenceKey, default)
    }

    @SuppressLint("CommitPrefEdits")
    override fun setToPreference(property: KProperty<*>, value: Double, preference: SharedPreferences) {
        preference.edit().putDouble(preferenceKey, value).execute(commitByDefault)
    }

    override fun setToEditor(property: KProperty<*>, value: Double, editor: SharedPreferences.Editor) {
        editor.putDouble(preferenceKey, value)
    }
}


fun KotprefModel.doublePref(
        default: Double = 0.0,
        key: String? = null,
        commitByDefault: Boolean = commitAllPropertiesByDefault
): AbstractPref<Double> = DoublePref(default, key, commitByDefault)

fun KotprefModel.doublePref(
        default: Double = 0.0,
        key: Int,
        commitByDefault: Boolean = commitAllPropertiesByDefault
): AbstractPref<Double> = doublePref(default, context.getString(key), commitByDefault)


fun SharedPreferences.Editor.putDouble(key: String, value: Double): SharedPreferences.Editor {
    putLong(key, value.toRawBits())
    return this
}

fun SharedPreferences.getDouble(key: String, defaultValue: Double): Double {
    return if (!contains(key)) {
        defaultValue
    } else {
        Double.fromBits(getLong(key, 0))
    }
}