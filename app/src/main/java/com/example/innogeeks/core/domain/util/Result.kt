package com.example.innogeeks.core.domain.util

// Marker for any type that can be the failure side of a Result.
interface Error

// A typed two-outcome container, sealed so `when` over it is exhaustive, and covariant in both slots.
sealed interface Result<out D, out E : Error> {
    // Success carries data; its error slot is Nothing (Kotlin's "impossible" type,
    // subtype of everything) — a success has no error and never could.
    data class Success<out D>(val data: D) : Result<D, Nothing>

    // Error mirrors it: carries an error, data slot is Nothing.
    data class Error<out E : com.example.innogeeks.core.domain.util.Error>(
        val error: E
    ) : Result<Nothing, E>
}

// Nickname for the common shape "succeeds with no payload" (e.g. save/delete/validate).
typealias EmptyResult<E> = Result<Unit, E>

// Transforms the success value (T -> R); passes any error through untouched.
// inline => the lambda is compiled into the call site (no per-call lambda object).
inline fun <T, E : Error, R> Result<T, E>.mapData(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(data = map(this.data))
    }
}

// Mirror of mapData: transforms the ERROR value (E -> E2), passes success through.
// Used to convert one layer's error type into another (e.g. DataError -> SignUpError.Remote).
inline fun <T , E:Error , E2: Error> Result<T,E>.mapError(map: (E)->E2) : Result<T,E2>{
    return when(this){
        is Result.Error -> Result.Error(map(this.error))
        is Result.Success -> Result.Success(this.data)
    }
}

// Discards a success value, keeping only "worked or failed with E".
fun <T, E : Error> Result<T, E>.asEmptyResult(): EmptyResult<E> {
    return this.mapData { Unit }
}

// Side-effect on success only; returns `this` unchanged so calls can be chained.
inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

// Side-effect on failure only; also returns `this` for chaining
// (result.onSuccess { .. }.onFailure { .. }).
inline fun <T, E : Error> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Success -> this
        is Result.Error -> {
            action(error)
            this
        }
    }
}