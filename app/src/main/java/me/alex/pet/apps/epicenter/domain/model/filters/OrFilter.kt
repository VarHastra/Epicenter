package me.alex.pet.apps.epicenter.domain.model.filters

class OrFilter<in T>(vararg filters: Filter<T>) : Filter<T> {

    val filters = filters.toList()

    override fun invoke(p1: T): Boolean {
        return if (filters.isEmpty()) {
            true
        } else {
            filters.map { it.invoke(p1) }.reduce { acc, value -> acc || value }
        }
    }
}