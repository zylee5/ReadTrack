package com.example.readtrack.types

data class Status(val state: String) {
    override fun toString(): String = state

    companion object {
        @JvmStatic // To create a static get method for this property
                   // otherwise direct referencing: Status.states -> compiler complains
        val states = listOf(
            Status("Read"),
            Status("Reading"),
            Status("Want to read")
        )
    }
}
