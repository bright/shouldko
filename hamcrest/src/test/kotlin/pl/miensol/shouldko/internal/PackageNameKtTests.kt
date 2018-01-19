package pl.miensol.shouldko.internal

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import pl.miensol.shouldko.shouldEqual

internal class PackageNameKtTests {
    @Test
    fun `package of class without package is null`() {
        val className = Class.forName("PackageLessClass").name
        packageName(className).shouldEqual(null)
    }

    @Test
    fun `package name of a non nested class`() {
        packageName(javaClass.name).shouldEqual(javaClass.`package`.name)
    }

    @Test
    fun `package class of a nested class`() {
        packageName(Nested::class.java.name).shouldEqual(Nested::class.java.`package`.name)
    }

    class Nested
}