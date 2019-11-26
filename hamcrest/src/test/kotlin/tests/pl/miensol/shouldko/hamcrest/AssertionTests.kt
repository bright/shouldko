package tests.pl.miensol.shouldko.hamcrest

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pl.miensol.shouldko.*
import java.math.BigDecimal

class AssertionTests {
    @Test
    fun `single variable equal`() {
        val myVariable = "some value"

        val error = Assertions.assertThrows(AssertionError::class.java, {
            myVariable.should(equalTo("some other value"))
        })

        assertThat(error.message, containsString("myVariable"))
    }

    @Test
    fun `single variable not equal`() {
        val myVariable = "some value"

        val error = Assertions.assertThrows(AssertionError::class.java, {
            myVariable.shouldNot(equalTo(myVariable))
        })

        assertThat(error.message, containsString("myVariable"))
    }

    @Test
    fun `method invocation equal`() {
        fun myFunction() = "some value"

        val error = Assertions.assertThrows(AssertionError::class.java) {
            myFunction().should(equalTo("some other value"))
        }

        assertThat(error.message, containsString("myFunction()"))
    }

    @Test
    fun `expression equal`() {
        val a = 1

        val error = Assertions.assertThrows(AssertionError::class.java) {
            (a + 2).shouldEqual(4)
        }

        assertThat(error.message, containsString("a + 2"))
    }

    @Test
    fun `lambda expression not equal to`() {
        val a = { 1 + 1 }

        val error = Assertions.assertThrows(AssertionError::class.java) {
            (a() + 1).shouldNotEqual(3)
        }

        assertThat(error.message, containsString("a() + 1"))
    }

    @Test
    fun `inline functions equal to`() {
        val error = Assertions.assertThrows(AssertionError::class.java) {
            sum(1, 2).shouldEqual(4)
        }

        assertThat(error.message, containsString("sum(1, 2)"))
    }

    @Test
    fun `collection contain item`() {
        val error = Assertions.assertThrows(AssertionError::class.java) {
            listOf(1, 2, 3).shouldContain(4L)
        }

        assertThat(error.message, containsString("listOf(1, 2, 3)"))
    }

    @Test
    fun `collection not contain item`() {
        val error = Assertions.assertThrows(AssertionError::class.java) {
            listOf(1, 2, 3).shouldNotContain(3)
        }

        assertThat(error.message, containsString("listOf(1, 2, 3)"))
    }

    @Test
    fun `string contains substring`() {
        val error = Assertions.assertThrows(AssertionError::class.java) {
            ("ala" + "ma kota").shouldContain("makota")
        }

        assertThat(error.message, containsString("""("ala" + "ma kota")"""))
    }

    @Test
    fun `string does not contain substring`() {
        val error = Assertions.assertThrows(AssertionError::class.java) {
            ("ala" + "ma kota").shouldNotContain("ma")
        }

        assertThat(error.message, containsString("""("ala" + "ma kota")"""))
    }

    @Test
    fun `comparable equal`() {
        val error = Assertions.assertThrows(AssertionError::class.java) {
            BigDecimal("9").shouldEqualCompared(BigDecimal.TEN)
        }

        assertThat(error.message, containsString("""BigDecimal("9")"""))
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun sum(a: Int, b: Int) = a + b
}