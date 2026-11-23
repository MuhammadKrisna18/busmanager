package com.example.busmanager.domain.route.model

data class TransitStop(
    val terminal: Terminal,
    val stopOrder: Int,
    val isOrigin: Boolean = false,
    val isDestination: Boolean = false
) {
    val isTransit: Boolean get() = !isOrigin && !isDestination
}
