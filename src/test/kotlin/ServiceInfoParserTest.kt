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
                "TRAVIS_BUILD_NUMBER" to "1",
                "TRAVIS_JOB_ID" to "2",
                "TRAVIS_PULL_REQUEST" to "123",
                "TRAVIS_BRANCH" to "foobar"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo(name = "travis-ci", number = "1", jobId = "2", pr = "123", branch = "foobar")
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
                "CIRCLE_WORKFLOW_ID" to "1",
                "CIRCLE_BUILD_NUM" to "2",
                "CIRCLE_PULL_REQUEST" to "https://github.com/username/repo/pull/123",
                "CIRCLE_BRANCH" to "foobar"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo(name = "circleci", number = "1", jobNumber = "2", pr = "123", branch = "foobar")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses circleci env on master`() {
        val envGetter = createEnvGetter(mapOf(
                "CIRCLECI" to "true",
                "CIRCLE_WORKFLOW_ID" to "1",
                "CIRCLE_BUILD_NUM" to "2",
                "CIRCLE_BRANCH" to "master"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo(name = "circleci", number = "1", jobNumber = "2", pr = null, branch = "master")
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
        val expected = ServiceInfo(name = "buildkite", number = "123", jobId = "58b195c0-94aa-43ba-ae43-00b93c29a8b7", pr = "11", branch = "foobar", buildUrl = "https://buildkite.com/your-org/your-repo/builds/123")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses gitlab env`() {
        val envGetter = createEnvGetter(mapOf(
                "GITLAB_CI" to "true",
                "CI_PIPELINE_ID" to "12341234",
                "CI_PIPELINE_URL" to "https://gitlab.com/your-group/your-repo/pipelines/123",
                "CI_JOB_ID" to "43214321",
                "CI_COMMIT_BRANCH" to "foobar",
                "CI_MERGE_REQUEST_IID" to "11"
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo(name = "gitlab-ci", number = "12341234", jobId = "43214321", pr = "11", branch = "foobar", buildUrl = "https://gitlab.com/your-group/your-repo/pipelines/123")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses bitrise env`() {
        val envGetter = createEnvGetter(mapOf(
            "BITRISE_IO" to "true",
            "BITRISE_BUILD_NUMBER" to "123123",
            "BITRISE_PULL_REQUEST" to "11",
            "BITRISE_GIT_BRANCH" to "foobar",
            "BITRISE_BUILD_URL" to "https://app.bitrise.io/build/d75abbebxfc9ca4e"
        ))
        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo(
            name = "bitrise",
            number = "123123",
            pr = "11",
            branch = "foobar",
            buildUrl = "https://app.bitrise.io/build/d75abbebxfc9ca4e"
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses unidentifiable ci as other when no env is set`() {
        val envGetter = createEnvGetter(emptyMap())

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo("other")
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses unidentifiable ci with default env on master`() {
        val envGetter = createEnvGetter(mapOf(
            "CI_NAME" to "teamcity",
            "CI_BUILD_NUMBER" to "123123",
            "CI_BUILD_URL" to "https://localhost:8111/viewLog.html?buildId=123123",
            "CI_BRANCH" to "master",
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo(
            name = "teamcity",
            number = "123123",
            buildUrl = "https://localhost:8111/viewLog.html?buildId=123123",
            branch = "master",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `ServiceInfoParser parses unidentifiable ci with default env on pr`() {
        val envGetter = createEnvGetter(mapOf(
            "CI_NAME" to "teamcity",
            "CI_BUILD_NUMBER" to "123123",
            "CI_BUILD_URL" to "https://localhost:8111/viewLog.html?buildId=123123",
            "CI_BRANCH" to "foobar",
            "CI_PULL_REQUEST" to "11",
        ))

        val actual = ServiceInfoParser(envGetter).parse()
        val expected = ServiceInfo(
            name = "teamcity",
            number = "123123",
            buildUrl = "https://localhost:8111/viewLog.html?buildId=123123",
            branch = "foobar",
            pr = "11"
        )
        assertEquals(expected, actual)
    }

    private fun createEnvGetter(entries: Map<String, String>): EnvGetter {
        return { k: String -> entries[k] }
    }
}
