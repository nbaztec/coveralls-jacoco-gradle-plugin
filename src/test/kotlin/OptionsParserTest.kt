package org.gradle.plugin.coveralls.jacoco

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class OptionsParserTest {
    @Test
    fun `OptionsParser throws exception when repo token not set`() {
        val envGetter = createEnvGetter(mapOf())

        assertThrows<IllegalStateException> { OptionsParser(envGetter).parse() }.also {
            assertEquals("COVERALLS_REPO_TOKEN not set", it.message)
        }
    }

    @Test
    fun `OptionsParser ignores missing parameter`() {
        val envGetter = createEnvGetter(
            mapOf(
                "COVERALLS_REPO_TOKEN" to "foo",
            )
        )

        val actual = OptionsParser(envGetter).parse()
        val expected = Options(
            repoToken = "foo",
            parallel = null,
            flagName = null,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `OptionsParser parses falsy parallel parameter`() {
        val envGetter = createEnvGetter(
            mapOf(
                "COVERALLS_REPO_TOKEN" to "foo",
                "COVERALLS_PARALLEL" to "false",
            )
        )

        val actual = OptionsParser(envGetter).parse()
        val expected = Options(
            repoToken = "foo",
            parallel = false,
            flagName = null,
        )
        assertEquals(expected, actual)
    }


    @Test
    fun `OptionsParser parses coveralls parameter`() {
        val envGetter = createEnvGetter(
            mapOf(
                "COVERALLS_REPO_TOKEN" to "foo",
                "COVERALLS_PARALLEL" to "true",
                "COVERALLS_FLAG_NAME" to "flag_name",
            )
        )

        val actual = OptionsParser(envGetter).parse()
        val expected = Options(
            repoToken = "foo",
            parallel = true,
            flagName = "flag_name",
        )
        assertEquals(expected, actual)
    }

    private fun createEnvGetter(entries: Map<String, String>): EnvGetter {
        return { k: String -> entries[k] }
    }
}
