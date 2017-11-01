package pl.miensol.shouldko

import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import pl.miensol.shouldko.internal.prependTopStackFrameSourceLine

fun <T> T.shouldBe(matcher: Matcher<T>): T = apply {
    prependTopStackFrameSourceLine {
        MatcherAssert.assertThat(this, matcher)
    }
}

fun <T> T.shouldNotBe(matcher: Matcher<T>): T = shouldBe(Matchers.not(matcher))