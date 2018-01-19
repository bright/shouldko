package pl.miensol.shouldko.internal

fun addSourceLineToAssertionError(job: () -> Unit) {
    try {
        job()
    } catch (originalError: AssertionError) {
        throw DefaultErrorEnhancer().enhance(originalError) { AssertionError(it) }
    }
}