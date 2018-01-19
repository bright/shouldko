package pl.miensol.shouldko.internal

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import pl.miensol.shouldko.shouldEqual

internal class CallingPackageStackTraceElementFinderTest {
    private fun finder(matcher: (String) -> Boolean) =
            CallingPackageStackTraceElementFinder(matcher)

    @Test
    fun `can find first frame outside of should ko`() {
        val firstFrameOutsideOfShouldKo = StackTraceElement("org.example.ClassTest", "can_do_something", null, -1)
        val stackTrace = listOf(
                StackTraceElement("java.lang.Enum", "toString", null, -1),
                StackTraceElement("java.lang.Enum", "valueOf", null, -1),
                StackTraceElement("pl.miensol.shouldko.ShouldKt", "should", null, -1),
                firstFrameOutsideOfShouldKo,
                StackTraceElement("org.junit.Runtime", "run", null, -1)
        )

        val frame = finder({ true })(stackTrace)

        frame.shouldEqual(firstFrameOutsideOfShouldKo)
    }

    @Test
    fun `can find first frame outside of should ko and outside of custom matcher`() {
        val firstFrameOutsideOfShouldKo = StackTraceElement("org.example.ClassTest", "can_do_something", null, -1)
        val stackTrace = listOf(
                StackTraceElement("java.lang.Enum", "toString", null, -1),
                StackTraceElement("java.lang.Enum", "valueOf", null, -1),
                StackTraceElement("pl.miensol.shouldko.ShouldKt", "should", null, -1),
                StackTraceElement("org.example.ClassTestHelpers", "shouldBeBig", null, -1),
                firstFrameOutsideOfShouldKo,
                StackTraceElement("org.junit.Runtime", "run", null, -1)
        )

        val isNotCustomTestHelper: (String) -> Boolean = { !it.endsWith("ClassTestHelpers") }

        val frame = finder(isNotCustomTestHelper)(stackTrace)

        frame.shouldEqual(firstFrameOutsideOfShouldKo)
    }

    @Test
    fun `cannot find frame without shouldko in stack trace`() {
        val stackTrace = listOf(
                StackTraceElement("java.lang.Enum", "toString", null, -1),
                StackTraceElement("java.lang.Enum", "valueOf", null, -1),
                StackTraceElement("org.example.ClassTestHelpers", "shouldBeBig", null, -1)
        )

        val frame = finder({true})(stackTrace)

        frame.shouldEqual(null)
    }
}