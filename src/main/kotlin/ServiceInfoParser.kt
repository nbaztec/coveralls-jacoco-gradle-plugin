package org.gradle.plugin.coveralls.jacoco

data class ServiceInfo(val name: String, val jobId: String? = null, val pr: String? = null, val branch: String? = null)

class ServiceInfoParser(val envGetter: EnvGetter) {
    private val isJenkins = envGetter("JENKINS_URL") != null
    private val isTravis = envGetter("TRAVIS") == "true"
    private val isCircleCI = envGetter("CIRCLECI") == "true"
    private val isCodeship = envGetter("CI_NAME") == "codeship"
    private val isGithubActions = envGetter("GITHUB_ACTIONS") != null

    fun parse(): ServiceInfo {
        return when {
            isJenkins -> ServiceInfo(
                    "jenkins",
                    envGetter("BUILD_NUMBER"),
                    envGetter("ghprbPullId"),
                    envGetter("GIT_BRANCH")
            )
            isTravis -> ServiceInfo(
                    envGetter("CI_NAME") ?: "travis-pro",
                    envGetter("TRAVIS_JOB_ID"),
                    envGetter("TRAVIS_PULL_REQUEST"),
                    envGetter("TRAVIS_BRANCH")
            )
            isCircleCI -> {
                ServiceInfo(
                        "circleci",
                        envGetter("CIRCLE_BUILD_NUM"),
                        envGetter("CIRCLE_PULL_REQUEST")?.substringAfterLast("/"),
                        envGetter("CIRCLE_BRANCH")
                )
            }
            isCodeship -> ServiceInfo(
                    "codeship",
                    envGetter("CI_BUILD_NUMBER"),
                    envGetter("CI_PR_NUMBER"),
                    envGetter("CI_BRANCH")
            )
            isGithubActions -> ServiceInfo(
                    "github-actions",
                    envGetter("BUILD_NUMBER"),
                    envGetter("GITHUB_REF")?.let { ref ->
                        "refs/pull/(\\d+)/merge".toRegex().find(ref)?.let {
                            it.groupValues[1]
                        }
                    },
                    envGetter("CI_BRANCH")
            )
            else -> ServiceInfo("other")
        }
    }
}

