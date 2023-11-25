# ReadTrack
## Motivation
This Android application is designed to track personal reading activities, displaying books that have been read, are being read, or to be read. It should provide statistical data such as the number of books and pages read in the last year, the number of books of each genre read, etc.

## Overview
The application allows users to insert book records through a search via **Google Books API** or manual input of all essential book information. Once added to the bookshelf, books can be searched via title or author, and can be later modified or deleted. The bookshelf can also be sorted according to user preferences.

The application is developed to experiment with modern Android development practices using **Kotlin** and **MVVM** architecture. Key Android Jetpack libraries and their components such as **Navigation components (navigation graph, NavHost, actions, etc.), Two-way Data Binding, View Binding, LiveData, Flow, Room, RecyclerView, and Paging 3** are utilized. HTTP clients and converters, **Retrofit** and **Moshi**, and key Kotlin concepts, such as **coroutines**, are also incorporated.

## Note
To compile the program in Android Studio, add your Google API key in `local.properties` as **apiKey=*yourApiKey***.
