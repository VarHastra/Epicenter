package com.github.varhastra.epicenter.common.functionaltypes

sealed class Either<out D, out T : Throwable> {

    data class Success<out D>(val data: D) : Either<D, Nothing>()

    data class Failure<out T : Throwable>(val t: T) : Either<Nothing, T>()

    val isSuccess get() = this is Success<D>

    val isFailure get() = this is Failure<T>

    inline fun <R> fold(onSuccess: (D) -> R, onFailure: (T) -> R): R {
        return when (this) {
            is Success -> onSuccess(data)
            is Failure -> onFailure(t)
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

        fun <T : Throwable> failure(t: T) = Failure(t)

        inline fun <D> of(data: D?, onNull: ((D?) -> Throwable)): Either<D, Throwable> {
            return if (data == null) {
                failure(onNull(data))
            } else {
                success(data)
            }
        }
    }
}

inline fun <D, T : Throwable, R> Either<D, T>.flatMap(f: (D) -> Either<R, T>): Either<R, T> {
    return when (this) {
        is Either.Success -> f(data)
        is Either.Failure -> this
    }
}

inline fun <D, T : Throwable> Either<D, T>.ifSuccess(f: (D) -> Unit): Either<D, T> {
    if (this is Either.Success) {
        f(data)
    }
    return this
}

inline fun <D, T : Throwable> Either<D, T>.ifFailure(f: (T) -> Unit): Either<D, T> {
    if (this is Either.Failure) {
        f(t)
    }
    return this
}