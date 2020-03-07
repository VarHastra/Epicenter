package com.github.varhastra.epicenter.common.functionaltypes

sealed class Either<out D, out T> {

    data class Success<out D>(val data: D) : Either<D, Nothing>()

    data class Failure<out T>(val failureDetails: T) : Either<Nothing, T>()

    val isSuccess get() = this is Success<D>

    val isFailure get() = this is Failure<T>

    inline fun <R> fold(onSuccess: (D) -> R, onFailure: (T) -> R): R {
        return when (this) {
            is Success -> onSuccess(data)
            is Failure -> onFailure(failureDetails)
        }
    }

    inline fun <R> map(f: (D) -> R): Either<R, T> {
        return when (this) {
            is Success -> Success(f(data))
            is Failure -> this
        }
    }

    companion object {
        fun <D> success(data: D) = Success(data)

        fun <T> failure(t: T) = Failure(t)

        inline fun <D, T> of(data: D?, onNull: ((D?) -> T)): Either<D, T> {
            return if (data == null) {
                failure(onNull(data))
            } else {
                success(data)
            }
        }
    }
}

inline fun <D, T, R> Either<D, T>.flatMap(f: (D) -> Either<R, T>): Either<R, T> {
    return when (this) {
        is Either.Success -> f(data)
        is Either.Failure -> this
    }
}

inline fun <D, T> Either<D, T>.ifSuccess(f: (D) -> Unit): Either<D, T> {
    if (this is Either.Success) {
        f(data)
    }
    return this
}

inline fun <D, T> Either<D, T>.ifFailure(f: (T) -> Unit): Either<D, T> {
    if (this is Either.Failure) {
        f(failureDetails)
    }
    return this
}

fun <D, T> Either<D, T>.or(fallback: D) = when (this) {
    is Either.Success -> this
    is Either.Failure -> Either.Success(fallback)
}

fun <D, T> Either<D, T>.orNull() = when (this) {
    is Either.Success -> data
    is Either.Failure -> null
}