package org.gradle.plugin.coveralls.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

open class CoverallsJacocoPluginExtension {
    var rootPackage: String? = null
    var reportPath = "build/reports/jacoco/test/jacocoTestReport.xml"
    var apiEndpoint = "https://coveralls.io/api/v1/jobs"
    var additionalSourceSets = emptyList<SourceSet>()
}

class CoverallsJacocoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("coverallsJacoco", CoverallsJacocoPluginExtension::class.java)

        project.task("coverallsJacoco") {
            it.doLast {
                val envGetter = {v: String -> System.getenv(v)?.ifBlank { null } }
                CoverallsReporter(envGetter).report(project)
            }
        }
    }
}