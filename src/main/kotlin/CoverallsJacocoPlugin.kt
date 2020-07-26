package org.gradle.plugin.coveralls.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project

open class CoverallsJacocoPluginExtension {
    var rootPackage: String? = null
    var reportPath = "build/reports/jacoco/test/jacocoTestReport.xml"
    var apiEndpoint = "https://coveralls.io/api/v1/jobs"
}

class CoverallsJacocoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("coverallsJacoco", CoverallsJacocoPluginExtension::class.java)

        project.task("coverallsJacoco") {
            it.doLast {
                CoverallsReporter.report(project)
            }
        }
    }
}