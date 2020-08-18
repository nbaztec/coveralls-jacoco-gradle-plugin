package org.gradle.plugin.coveralls.jacoco

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ServiceInfoParserTest {
    @Test
    fun `ServiceInfoParser parses jenkins env on pr`() {
        val envGetter = createEnvGetter(mapOf(
                "JENKINS_URL" to "https://jenkins.url",
                "BUILD_NUMBER" to "1",
                "ghprbPullId" to "123",
                "GIT_BRANCH" to "foobar"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("jenkins", "1", "123", "foobar")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses jenkins env on master`() {
        val envGetter = createEnvGetter(mapOf(
                "JENKINS_URL" to "https://jenkins.url",
                "BUILD_NUMBER" to "1",
                "GIT_BRANCH" to "master"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("jenkins", "1", null, "master")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses travis basic env on pr`() {
        val envGetter = createEnvGetter(mapOf(
                "TRAVIS" to "true",
                "CI_NAME" to "travis-ci",
                "TRAVIS_JOB_ID" to "1",
                "TRAVIS_PULL_REQUEST" to "123",
                "TRAVIS_BRANCH" to "foobar"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("travis-ci", "1", "123", "foobar")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses travis pro env on pr`() {
        val envGetter = createEnvGetter(mapOf(
                "TRAVIS" to "true",
                "TRAVIS_JOB_ID" to "1",
                "TRAVIS_PULL_REQUEST" to "123",
                "TRAVIS_BRANCH" to "foobar"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("travis-pro", "1", "123", "foobar")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses travis pro env on master`() {
        val envGetter = createEnvGetter(mapOf(
                "TRAVIS" to "true",
                "TRAVIS_JOB_ID" to "1",
                "TRAVIS_BRANCH" to "master"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("travis-pro", "1", null, "master")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses circleci env on pr`() {
        val envGetter = createEnvGetter(mapOf(
                "CIRCLECI" to "true",
                "CIRCLE_BUILD_NUM" to "1",
                "CIRCLE_PULL_REQUEST" to "https://github.com/username/repo/pull/123",
                "CIRCLE_BRANCH" to "foobar"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("circleci", "1", "123", "foobar")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses circleci env on master`() {
        val envGetter = createEnvGetter(mapOf(
                "CIRCLECI" to "true",
                "CIRCLE_BUILD_NUM" to "1",
                "CIRCLE_BRANCH" to "master"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("circleci", "1", null, "master")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses codeship env on pr`() {
        val envGetter = createEnvGetter(mapOf(
                "CI_NAME" to "codeship",
                "CI_BUILD_NUMBER" to "1",
                "CI_PR_NUMBER" to "123",
                "CI_BRANCH" to "foobar"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("codeship", "1", "123", "foobar")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses codeship env on master`() {
        val envGetter = createEnvGetter(mapOf(
                "CI_NAME" to "codeship",
                "CI_BUILD_NUMBER" to "1",
                "CI_BRANCH" to "master"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("codeship", "1", null, "master")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses github actions env on pr`() {
        val envGetter = createEnvGetter(mapOf(
                "GITHUB_ACTIONS" to "true",
                "BUILD_NUMBER" to "1",
                "GITHUB_REF" to "refs/pull/123/merge",
                "CI_BRANCH" to "foobar"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("github-actions", "1", "123", "foobar")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses github actions env on master`() {
        val envGetter = createEnvGetter(mapOf(
                "GITHUB_ACTIONS" to "true",
                "BUILD_NUMBER" to "1",
                "CI_BRANCH" to "master"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("github-actions", "1", null, "master")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses unidentifiable ci as other`() {
        val envGetter = createEnvGetter(emptyMap())

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("other")
        assertEquals(expected, actual)
    }

    private fun createEnvGetter(entries: Map<String, String>): EnvGetter {
        return { k: String -> entries[k] }
    }
}
