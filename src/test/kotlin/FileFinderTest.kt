package org.gradle.plugin.coveralls.jacoco

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

internal class FileFinderTest {
    private val kotlinSrc = File("src/test/resources/testfiles/kotlinSrc")
    private val javaStyleSrc = File("src/test/resources/testfiles/javaStyleSrc")

    @Test
    fun `FileFinder parses all source directories and maps to root package`() {
        val fileFinder = FileFinder(listOf(
                File(kotlinSrc, "simple"),
                File(kotlinSrc, "comment"),
                File(javaStyleSrc, "simple")
        ))

        val actual = fileFinder::class.memberProperties.find { it.name == "rootPackages" }!!.let {
            it.isAccessible = true
            it.getter.call(fileFinder)
        }

        val expected = mapOf(
                File("src/test/resources/testfiles/kotlinSrc/simple") to "com/test/bar",
                File("src/test/resources/testfiles/kotlinSrc/comment") to "com/test/baz",
                File("src/test/resources/testfiles/javaStyleSrc/simple") to null

        )

        assertEquals(expected, actual)
    }

    @Test
    fun `FileFinder finds file with kotlin and java style directory structure`() {
        val fileFinder = FileFinder(listOf(
                File(kotlinSrc, "simple"),
                File(kotlinSrc, "comment"),
                File(javaStyleSrc, "simple")
        ))

        assertEquals(
                File("src/test/resources/testfiles/javaStyleSrc/simple/com/test/foo/Main.kt"),
                fileFinder.find(File("com/test/foo/Main.kt"))
        )
        assertEquals(
                File("src/test/resources/testfiles/kotlinSrc/simple/Main.kt"),
                fileFinder.find(File("com/test/bar/Main.kt"))
        )
        assertEquals(
                File("src/test/resources/testfiles/kotlinSrc/comment/Main.kt"),
                fileFinder.find(File("com/test/baz/Main.kt"))
        )
    }

    @Test
    fun `FileFinder returns null for invalid files`() {
        val fileFinder = FileFinder(listOf(
                File(kotlinSrc, "simple"),
                File(kotlinSrc, "comment"),
                File(javaStyleSrc, "simple")
        ))

        assertNull(fileFinder.find(File("com/test/foo/invalid/Main.kt")))
        assertNull(fileFinder.find(File("com/test/bar/invalid/Main.kt")))
        assertNull(fileFinder.find(File("com/test/baz/invalid/Main.kt")))
    }
}
