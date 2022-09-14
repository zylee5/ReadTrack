package com.example.readtrack.types

data class Book(
    var name: String = "",
    // TODO: Possibly a data class
    var authorName: String = "",
    var genre: String = "",
    // Start and end date

    var rating: Int = -1,
    var selectedStatusPosition: Int = -1
) {
    // Use the selected status position from the UI to get the status
    fun getSelectedStatus() = Status.states.getOrNull(selectedStatusPosition)
}
