package pl.miensol.shouldko.internal


class CallingPackageStackTraceElementFinder(private val callingPackageMatcher: (String) -> Boolean = { true }) : AssertionStackTraceElementFinder {
    override fun invoke(stackTrace: List<StackTraceElement>): StackTraceElement? {
        return stackTrace.asSequence()
                .dropWhile { !isFromShouldKo(it) }
                // we reached should ko
                .dropWhile { isFromShouldKo(it) }
                // we are outside of should ko
                .dropWhile { !callingPackageMatcher(it.className) }
                // we are outside of client matcher helpers
                .firstOrNull()
    }

    private fun isFromShouldKo(stackTraceElement: StackTraceElement) =
            stackTraceElement.className.startsWith(PackageName)

    companion object {
        private const val PackageName = "pl.miensol.shouldko"
    }
}