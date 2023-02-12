# ReadTrack
## Motivation
This Android application is designed to track personal reading activities, displaying books that have been read, are being read, or are intended to be read. It provides statistical data such as the number of books and pages read in the last year, the number of books of each genre read, etc.

## Overview
The application allows users to insert book records through a search via **Google Books API** or manual input of all essential book information. Once added to the bookshelf, books can be searched via title or author and can be modified or deleted at any time. The bookshelf can also be sorted according to user preferences.

Apart from its practical usage, the application is also created to implement standard Android development practices using Kotlin and MVVM architecture. Key Android Jetpack libraries and their components such as **Paging 3, Flow, Room, RecyclerView, Two-way Data Binding, View Binding, LiveData, and Navigation components (navigation graph, NavHost, actions, etc.)** are utilized. HTTP clients and converters, **Retrofit** and **Moshi**, and key Kotlin concepts, such as **coroutines**, are also incorporated.

## Key Technical Features
- [x] Custom book record creation in Room database
  - [x] Image insertion via Intents handling (photo capture or local storage selection)
  - [x] Add-button activation/deactivation via MediatorLiveData and BaseObservable instances
- [x] Display of all book records from Room database in RecyclerView
  - [x] Load images in ImageView via Glide 
- [x] Book record modification in Room database through RecyclerView item clicks
- [x] Book record deletion from Room database through RecyclerView item swipes
  - [x] Deletion undo through Snackbar actions
- [x] RecyclerView item sorting through Comparator classes in data class model
  - [x] Retained sorting options through SharedPreferences
- [x] RecyclerView item filtering through SearchView queries
- [x] Google Books API integration (Retrofit, Moshi, Paging 3) for queried book record retrieval

## Future Updates (Coming Soon)
- Allow users to select a record returned from the Google Books API, add status information in a separate view, and persist the record in the Room database
- Display reading statistics in home page, including
  - For each status (read, reading, want to read), book/page counts with/without genre breakdown for all-time/specified time frames, answering questions such as:
    - Total number of books read
    - Total number of books being read
    - Total number of pages read
    - Number of books read in 2019
    - Number of pages read in 2019
    - Total number of fantasy books read
    - Number of fantasy books read in 2019
    - Total number of pages of fantasy books read
... etc.  
  - Most frequently read genre/period/language
- Enhance UI with Jetpack Compose
 
## Note
If you wish to compile the program in Android Studio, add your Google API key in `local.properties` as **apiKey=*yourApiKey***.
