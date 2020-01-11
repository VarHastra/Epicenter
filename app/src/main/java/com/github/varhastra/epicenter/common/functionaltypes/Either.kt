package com.github.varhastra.epicenter.common.functionaltypes

sealed class Either<out L, out R> {

    data class Success<out R>(val data: R) : Either<Nothing, R>()

    data class Failure<out L>(val t: L) : Either<L, Nothing>()

    val isSuccess get() = this is Success<R>

    val isFailure get() = this is Failure<L>

    fun <U> fold(onSuccess: (R) -> U, onFailure: (L) -> U): U {
        return when (this) {
            is Success -> onSuccess(data)
            is Failure -> onFailure(t)
        }
    }

    fun <T> map(f: (R) -> T): Either<L, T> {
        return when (this) {
            is Success -> Success(f(data))
            is Failure -> this
        }
    }
}

fun <L, R, T> Either<L, R>.flatMap(f: (R) -> Either<L, T>): Either<L, T> {
    return when (this) {
        is Either.Success -> f(data)
        is Either.Failure -> this
    }
}

fun <L, R, T> Either<L, R>.ifSuccess(f: (R) -> T): Either<L, R> {
    if (this is Either.Success) {
        f(data)
    }
    return this
}

fun <L, R, T> Either<L, R>.ifFailure(f: (L) -> T): Either<L, R> {
    if (this is Either.Failure) {
        f(t)
    }
    return this
}