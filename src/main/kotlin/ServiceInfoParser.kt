package org.gradle.plugin.coveralls.jacoco

data class ServiceInfo(val name: String, val repoName: String? = null, val number: String? = null, val jobId: String? = null, val pr: String? = null, val branch: String? = null)

class ServiceInfoParser(val envGetter: EnvGetter) {
    private val isJenkins = envGetter("JENKINS_URL") != null
    private val isTravis = envGetter("TRAVIS") == "true"
    private val isCircleCI = envGetter("CIRCLECI") == "true"
    private val isCodeship = envGetter("CI_NAME") == "codeship"
    private val isGithubActions = envGetter("GITHUB_ACTIONS") != null
    private val isGithubActionsToken = envGetter("GITHUB_TOKEN") != null
    private val isBuildkite = envGetter("BUILDKITE") == "true"

    fun parse(): ServiceInfo {
        return when {
            isJenkins -> ServiceInfo(
                    name = "jenkins",
                    jobId = envGetter("BUILD_NUMBER"),
                    pr = envGetter("ghprbPullId"),
                    branch = envGetter("GIT_BRANCH")
            )
            isTravis -> ServiceInfo(
                    name = envGetter("CI_NAME") ?: "travis-pro",
                    jobId = envGetter("TRAVIS_JOB_ID"),
                    pr = envGetter("TRAVIS_PULL_REQUEST"),
                    branch = envGetter("TRAVIS_BRANCH")
            )
            isCircleCI -> ServiceInfo(
                    name = "circleci",
                    jobId = envGetter("CIRCLE_BUILD_NUM"),
                    pr = envGetter("CIRCLE_PULL_REQUEST")?.substringAfterLast("/"),
                    branch = envGetter("CIRCLE_BRANCH")
            )
            isCodeship -> ServiceInfo(
                    name = "codeship",
                    jobId = envGetter("CI_BUILD_NUMBER"),
                    pr = envGetter("CI_PR_NUMBER"),
                    branch = envGetter("CI_BRANCH")
            )
            isGithubActions && isGithubActionsToken -> ServiceInfo(
                    name = "github",
                    repoName = envGetter("GITHUB_REPOSITORY"),
                    jobId = envGetter("BUILD_NUMBER"),
                    pr = envGetter("GITHUB_REF")?.let { ref ->
                        "refs/pull/(\\d+)/merge".toRegex().find(ref)?.let {
                            it.groupValues[1]
                        }
                    },
                    branch = envGetter("CI_BRANCH")
            )
            isGithubActions -> ServiceInfo(
                    name = "github-actions",
                    jobId = envGetter("BUILD_NUMBER"),
                    pr = envGetter("GITHUB_REF")?.let { ref ->
                        "refs/pull/(\\d+)/merge".toRegex().find(ref)?.let {
                            it.groupValues[1]
                        }
                    },
                    branch = envGetter("CI_BRANCH")
            )
            isBuildkite -> ServiceInfo(
                    name = "buildkite",
                    number = envGetter("BUILDKITE_BUILD_NUMBER"),
                    jobId = envGetter("BUILDKITE_BUILD_ID"),
                    pr = if (envGetter("BUILDKITE_PULL_REQUEST") == "false")  null else envGetter("BUILDKITE_PULL_REQUEST"),
                    branch = envGetter("BUILDKITE_BRANCH")
            )
            else -> ServiceInfo("other")
        }
    }
}
