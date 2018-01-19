package pl.miensol.shouldko.internal


internal class CallingPackageStackTraceElementFinder(val callingPackageMatcher: (String) -> Boolean = { true }) : AssertionStackTraceElementFinder {
    override fun invoke(stackTrace: List<StackTraceElement>): StackTraceElement? {
        var reachedShouldKo = false
        stackTrace.forEach { stackTraceElement ->
            reachedShouldKo = reachedShouldKo || isFromShouldKo(stackTraceElement)
            if (reachedShouldKo && !isFromShouldKo(stackTraceElement) && callingPackageMatcher(stackTraceElement.className)) {
                return stackTraceElement
            }
        }
        return null
    }

    private fun isFromShouldKo(stackTraceElement: StackTraceElement) =
            stackTraceElement.className.startsWith(PackageName)

    companion object {
        private const val PackageName = "pl.miensol.shouldko"
    }
}