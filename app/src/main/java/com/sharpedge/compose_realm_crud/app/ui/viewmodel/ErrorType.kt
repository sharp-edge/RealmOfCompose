package com.sharpedge.compose_realm_crud.app.ui.viewmodel

sealed class ErrorType {
    object None : ErrorType()
    data class TextFieldError(val message: String) : ErrorType()
    data class LazyListError(val message: String) : ErrorType()
}
