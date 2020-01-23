package com.github.varhastra.epicenter.domain.model.filters

class AndFilter<T>(vararg filters: Filter<T>) : Filter<T> {

    val filters = filters.toList()

    override fun invoke(p1: T): Boolean {
        return if (filters.isEmpty()) {
            true
        } else {
            filters.map { it.invoke(p1) }.reduce { acc, value -> acc && value }
        }
    }
}