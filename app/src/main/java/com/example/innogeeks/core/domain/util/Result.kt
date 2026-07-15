package com.example.innogeeks.core.domain.util

interface Error

sealed interface Result<out D , out E: Error>{
    data class Success<out D>(val data : D) : Result<D , Nothing>
    data class Error<out E : com.example.innogeeks.core.domain.util.Error>(
        val error : E
    ) : Result<Nothing , E>
}

typealias EmptyResult<E> = Result<Unit,E>

inline fun <T , E: Error ,R> Result<T,E>.map(map : (T) -> R) : Result<R,E>{
    return when(this){
        is Result.Error -> Result.Error(error = this.error)
        is Result.Success -> Result.Success(data = map(this.data))
    }
}

fun <T , E: Error> Result<T ,E >.asEmptyResult() : EmptyResult<E>{
    return this.map{ Unit }
}

inline fun <T ,E :Error> Result<T,E>.onSuccess(action : (T) -> Unit) : Result<T , E>{
    return when(this){
        is Result.Error ->this

        is Result.Success ->{
            action(data)
            this
        }
    }
}

inline fun<T,E :Error> Result<T,E>.onFailure(action: (E) -> Unit) : Result<T,E>{
    return when(this){
        is Result.Success -> this
        is Result.Error ->{
            action(error)
            this
        }
    }
}