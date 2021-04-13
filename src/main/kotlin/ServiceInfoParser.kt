package org.gradle.plugin.coveralls.jacoco

data class ServiceInfo(
    val name: String,
    val repoName: String? = null,
    val number: String? = null,
    val jobId: String? = null,
    val jobNumber: String? = null,
    val pr: String? = null,
    val branch: String? = null,
    val buildUrl: String? = null
)

class ServiceInfoParser(val envGetter: EnvGetter) {
    private val isJenkins = envGetter("JENKINS_URL") != null
    private val isTravis = envGetter("TRAVIS") == "true"
    private val isCircleCI = envGetter("CIRCLECI") == "true"
    private val isCodeship = envGetter("CI_NAME") == "codeship"
    private val isGithubActions = envGetter("GITHUB_ACTIONS") != null
    private val isGithubActionsToken = envGetter("GITHUB_TOKEN") != null
    private val isBuildkite = envGetter("BUILDKITE") == "true"
    private val isGitlab = envGetter("GITLAB_CI") != null
    private val isBitrise = envGetter("BITRISE_IO") == "true"

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
                    number = envGetter("TRAVIS_BUILD_NUMBER"),
                    jobId = envGetter("TRAVIS_JOB_ID"),
                    pr = envGetter("TRAVIS_PULL_REQUEST"),
                    branch = envGetter("TRAVIS_BRANCH")
            )
            isCircleCI -> ServiceInfo(
                    name = "circleci",
                    number = envGetter("CIRCLE_WORKFLOW_ID"),
                    jobNumber = envGetter("CIRCLE_BUILD_NUM"),
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
                    branch = envGetter("BUILDKITE_BRANCH"),
                    buildUrl = envGetter("BUILDKITE_BUILD_URL")
            )
            isGitlab -> ServiceInfo(
                    name = "gitlab-ci",
                    number = envGetter("CI_PIPELINE_ID"),
                    jobId = envGetter("CI_JOB_ID"),
                    pr = envGetter("CI_MERGE_REQUEST_IID"),
                    branch = envGetter("CI_COMMIT_BRANCH"),
                    buildUrl = envGetter("CI_PIPELINE_URL")
            )
            isBitrise -> ServiceInfo(
                name = "bitrise",
                number = envGetter("BITRISE_BUILD_NUMBER"),
                pr = envGetter("BITRISE_PULL_REQUEST"),
                branch = envGetter("BITRISE_GIT_BRANCH"),
                buildUrl = envGetter("BITRISE_BUILD_URL")
            )
            else -> ServiceInfo(
                    name = envGetter("CI_NAME") ?: "other",
                    number = envGetter("CI_BUILD_NUMBER"),
                    pr = envGetter("CI_PULL_REQUEST"),
                    branch = envGetter("CI_BRANCH"),
                    buildUrl = envGetter("CI_BUILD_URL")
            )
        }
    }
}
