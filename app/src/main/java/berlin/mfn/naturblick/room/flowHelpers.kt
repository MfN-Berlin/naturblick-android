package berlin.mfn.naturblick.room

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

fun <T> Flow<List<T>>.single(): Flow<T> {
    return filter {
        it.isNotEmpty()
    }.map {
        it[0]
    }
}
