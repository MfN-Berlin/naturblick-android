package berlin.mfn.naturblick.utils

object MediaPlayerService {
    fun timeFormat(time: Float): String {
        return String.format("%.1fs", time)
    }
}
