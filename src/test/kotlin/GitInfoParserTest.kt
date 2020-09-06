package org.gradle.plugin.coveralls.jacoco

import org.gradle.internal.impldep.com.google.common.io.Files
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File

internal class GitInfoParserTest {
    private val testRepo = File("src/test/resources/testrepo")

    @Test
    fun `GitInfoParser parses git repo details`() {
        // it's non trivial to commit .git directories, hence we rename directory `git` to `.git`
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

    @Test
    fun `GitInfoParser returns null on invalid git repo`() {
        // repo is invalid since we haven't renamed the directory `git` to `.git`
        val actual = GitInfoParser.parse(testRepo)
        assertNull(actual)
    }
}
