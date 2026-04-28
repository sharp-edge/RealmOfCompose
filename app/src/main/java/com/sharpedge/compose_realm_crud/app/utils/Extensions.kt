package com.sharpedge.compose_realm_crud.app.utils

fun String?.isNullOrBlankOrEmpty(): Boolean {
    return this.isNullOrBlank() || this.isEmpty()
}
// Checked: 2026-04-28
