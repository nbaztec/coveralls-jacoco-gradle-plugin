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
        val expected = ServiceInfo(name = "jenkins", jobId = "1", pr = "123", branch = "foobar")
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
        val expected = ServiceInfo(name = "jenkins", jobId = "1", pr = null, branch = "master")
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
        val expected = ServiceInfo(name = "travis-ci", jobId = "1", pr = "123", branch = "foobar")
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
        val expected = ServiceInfo(name = "travis-pro", jobId = "1", pr = "123", branch = "foobar")
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
        val expected = ServiceInfo(name = "travis-pro", jobId = "1", pr = null, branch = "master")
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
        val expected = ServiceInfo(name = "circleci", jobId = "1", pr = "123", branch = "foobar")
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
        val expected = ServiceInfo(name = "circleci", jobId = "1", pr = null, branch = "master")
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
        val expected = ServiceInfo(name = "codeship", jobId = "1", pr = "123", branch = "foobar")
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
        val expected = ServiceInfo(name = "codeship", jobId = "1",pr = null, branch = "master")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses github env on pr`() {
        val envGetter = createEnvGetter(mapOf(
                "GITHUB_ACTIONS" to "true",
                "GITHUB_REPOSITORY" to "foo/bar",
                "GITHUB_TOKEN" to "token",
                "BUILD_NUMBER" to "1",
                "GITHUB_REF" to "refs/pull/123/merge",
                "CI_BRANCH" to "foobar"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo(name = "github", repoName = "foo/bar", jobId = "1", pr = "123", branch = "foobar")
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
        val expected = ServiceInfo(name = "github-actions", jobId = "1", pr = "123", branch = "foobar")
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
        val expected = ServiceInfo(name = "github-actions", jobId = "1", pr = null, branch = "master")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses buildkite env`() {
        val envGetter = createEnvGetter(mapOf(
                "BUILDKITE" to "true",
                "BUILDKITE_BUILD_NUMBER" to "123",
                "BUILDKITE_BUILD_URL" to "https://buildkite.com/your-org/your-repo/builds/123",
                "BUILDKITE_BUILD_ID" to "58b195c0-94aa-43ba-ae43-00b93c29a8b7",
                "BUILDKITE_BRANCH" to "foobar",
                "BUILDKITE_COMMIT" to "231asdfadsf424",
                "BUILDKITE_PULL_REQUEST" to "11"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo(name = "buildkite", number = "123", jobId = "58b195c0-94aa-43ba-ae43-00b93c29a8b7", pr = "11", branch = "foobar")
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
