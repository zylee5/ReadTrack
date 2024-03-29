package com.example.readtrack.types

//data class Status(val state: String) {
//    override fun toString(): String = state
//
//    companion object {
//        @JvmStatic // To create a static get method for this property
//                   // otherwise direct referencing: Status.states
//        val states = listOf(
//            Status("Read"),
//            Status("Reading"),
//            Status("Want to read")
//        )
//    }
//}

enum class Status(val string: String) {
    NONE("None"),
    READ("Read"),
    READING("Reading"),
    WANT_TO_READ("Want to read")
}
