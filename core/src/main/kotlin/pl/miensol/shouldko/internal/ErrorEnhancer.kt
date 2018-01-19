package pl.miensol.shouldko.internal

internal interface ErrorEnhancer {
    fun <T : Throwable> enhance(exception: T, factory: (msg: String) -> T): T
}