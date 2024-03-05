package com.example.readtrack.services

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.readtrack.api_models.VolumeApiModel
import retrofit2.HttpException
import java.io.IOException

private const val STARTING_INDEX = 0

class VolumePagingSource(
    private val service: GoogleBookApiService,
    private val query: String,
): PagingSource<Int, VolumeApiModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VolumeApiModel> {
        val position = params.key ?: STARTING_INDEX

        // The index range of the current list
        val range = position.until(position + params.loadSize)

        return try {
            val response = service.getVolumesForQuery(query, range.first, params.loadSize)
            val volumes = response.items
            // Log.d("paging", "prevKey : ${(range.first - params.loadSize).coerceAtLeast(STARTING_INDEX)}, nextKey: ${range.last + 1}")

            volumes?.let { volumeList ->
                LoadResult.Page(
                    data = volumeList,
                    // Do not load items behind STARTING_INDEX
                    prevKey = if (position == STARTING_INDEX) null else (range.first - params.loadSize).coerceAtLeast(STARTING_INDEX),
                    nextKey = if (volumeList.isEmpty()) null else range.last + 1
                )
            } ?: LoadResult.Error(NoSuchElementException())

        } catch (ex: IOException) {
            LoadResult.Error(ex)
        } catch (ex: HttpException) {
            // Log.d("paging", "$ex")
            LoadResult.Error(ex)
        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, VolumeApiModel>): Int? {
        // Most recently accessed index
        val anchorPosition = state.anchorPosition ?: return null
        return (anchorPosition - (state.config.initialLoadSize / 2)).coerceAtLeast(STARTING_INDEX)
    }
}