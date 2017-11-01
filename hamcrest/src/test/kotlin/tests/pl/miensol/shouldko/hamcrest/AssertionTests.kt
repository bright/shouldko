package tests.pl.miensol.shouldko.hamcrest

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pl.miensol.shouldko.shouldBe
import pl.miensol.shouldko.shouldNotBe

class AssertionTests {
    @Test
    fun `single variable equals`() {
        val myVariable = "some value"

        val error = Assertions.assertThrows(AssertionError::class.java, {
            myVariable.shouldBe(equalTo("some other value"))
        })

        assertThat(error.message, containsString("myVariable"))
    }

    @Test
    fun `single variable not equals`() {
        val myVariable = "some value"

        val error = Assertions.assertThrows(AssertionError::class.java, {
            myVariable.shouldNotBe(equalTo(myVariable))
        })

        assertThat(error.message, containsString("myVariable"))
    }

    @Test
    fun `method invocation equals`() {
        fun myFunction() = "some value"

        val error = Assertions.assertThrows(AssertionError::class.java) {
            myFunction().shouldBe(equalTo("some other value"))
        }

        assertThat(error.message, containsString("myFunction()"))
    }
}