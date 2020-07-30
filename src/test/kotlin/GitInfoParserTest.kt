package org.gradle.plugin.coveralls.jacoco

import org.gradle.internal.impldep.com.google.common.io.Files
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class GitInfoParserTest {
    private val testRepo = File("src/test/resources/testrepo")

    @Test
    fun `GitInfoParser parses git repo details`() {
        Files.move(File(testRepo, "git"), File(testRepo, ".git"))

        val actual = GitInfoParser.parse(testRepo)
        Files.move(File(testRepo, ".git"), File(testRepo, "git"))

        val expected = GitInfo(
                Head(
                        "4cd72eadcc34861139b338dd859344d419244e0b",
                        "John Doe", "test@example.com",
                        "John Doe", "test@example.com",
                        "test commit\n"
                ),
                "master",
                listOf(Remote("origin", "git@github.com:test/testrepo.git"))
        )
        assertEquals(expected, actual)
    }
}