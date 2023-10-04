package com.sharpedge.compose_realm_crud.app.utils

fun String?.isNullOrBlankOrEmpty(): Boolean {
    return this.isNullOrBlank() || this.isEmpty()
}