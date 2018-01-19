package pl.miensol.shouldko.internal

internal fun packageName(className: String): String? {
    val lastDotIndex = className.lastIndexOf('.')
    return if (lastDotIndex != -1) {
        className.substring(0, lastDotIndex)
    } else null
}