package org.gradle.plugin.coveralls.jacoco

data class ServiceInfo(val name: String, val jobId: String? = null, val pr: String? = null, val branch: String? = null)

class ServiceInfoParser {
    companion object {
        private val isJenkins = System.getenv("JENKINS_URL") != null
        private val isTravis = System.getenv("TRAVIS") == "true"
        private val isCircleCI = System.getenv("CIRCLECI") == "true"
        private val isCodeship = System.getenv("CI_NAME") == "codeship"
        private val isGithubActions = System.getenv("GITHUB_ACTIONS") != null

        private fun envOrNull(v: String): String? = System.getenv(v)?.ifBlank { null }

        fun parse(): ServiceInfo {
            return when {
                isJenkins -> ServiceInfo(
                        "jenkins",
                        envOrNull("BUILD_NUMBER"),
                        envOrNull("ghprbPullId"),
                        envOrNull("GIT_BRANCH")
                )
                isTravis -> ServiceInfo(
                        envOrNull("CI_NAME") ?: "travis-pro",
                        envOrNull("TRAVIS_JOB_ID"),
                        envOrNull("TRAVIS_PULL_REQUEST"),
                        envOrNull("TRAVIS_BRANCH")
                )
                isCircleCI -> {
                    ServiceInfo(
                            "circleci",
                            envOrNull("CIRCLE_BUILD_NUM"),
                            envOrNull("CIRCLE_PULL_REQUEST")?.substringAfterLast("/"),
                            envOrNull("CIRCLE_BRANCH")
                    )
                }
                isCodeship -> ServiceInfo(
                        "codeship",
                        envOrNull("CI_BUILD_NUMBER"),
                        envOrNull("CI_PR_NUMBER"),
                        envOrNull("CI_BRANCH")
                )
                isGithubActions -> ServiceInfo(
                        "github-actions",
                        envOrNull("BUILD_NUMBER"),
                        envOrNull("GITHUB_REF")?.let { ref ->
                            "refs/pull/(\\d+)/merge".toRegex().find(ref)?.let {
                                it.groupValues[1]
                            }
                        },
                        envOrNull("CI_BRANCH")
                )
                else -> ServiceInfo("other")
            }
        }
    }
}

