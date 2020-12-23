package org.gradle.plugin.coveralls.jacoco

data class Options(
    val repoToken: String,
    val parallel: Boolean?,
    val flagName: String?
)

class OptionsParser(val envGetter: EnvGetter) {
    fun parse(): Options {
        val repoToken = envGetter("COVERALLS_REPO_TOKEN") ?: envGetter("GITHUB_TOKEN")
        check(repoToken != null && repoToken.isNotBlank()) { "COVERALLS_REPO_TOKEN not set" }

        val parallel = envGetter("COVERALLS_PARALLEL")?.toBoolean()
        val flagName = envGetter("COVERALLS_FLAG_NAME")

        return Options(repoToken, parallel, flagName)
    }
}
