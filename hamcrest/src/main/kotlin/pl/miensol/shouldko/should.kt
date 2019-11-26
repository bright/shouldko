package pl.miensol.shouldko

import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import pl.miensol.shouldko.internal.addSourceLineToAssertionError

fun <T> T.should(matcher: Matcher<T>): T = apply {
    addSourceLineToAssertionError {
        MatcherAssert.assertThat(this, matcher)
    }
}

fun <T> T.shouldBe(matcher: Matcher<T>): T = should(matcher)

fun <T> T.shouldNot(matcher: Matcher<T>): T = should(Matchers.not(matcher))

fun <T> T.shouldNotBe(matcher: Matcher<T>): T = shouldNot(matcher)

fun <T> T.shouldEqual(expected: T): T = should(Matchers.equalTo(expected))

fun <T> T.shouldNotEqual(expected: T): T = shouldNot(Matchers.equalTo(expected))

@Suppress("UNCHECKED_CAST")
fun <T> Iterable<T>.shouldContain(item: T): Iterable<T> = should(Matchers.hasItem(item) as Matcher<Iterable<T>>)

@Suppress("UNCHECKED_CAST")
fun <T> Iterable<T>.shouldNotContain(item: T): Iterable<T> = shouldNot(Matchers.hasItem(item) as Matcher<Iterable<T>>)

fun String.shouldContain(substring: String): String = should(Matchers.containsString(substring))

fun String.shouldNotContain(substring: String): String = shouldNot(Matchers.containsString(substring))

fun <T : Comparable<T>> T.shouldEqualCompared(expected: T): T = should(Matchers.comparesEqualTo(expected))
