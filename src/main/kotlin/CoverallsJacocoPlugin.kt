package org.gradle.plugin.coveralls.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

open class CoverallsJacocoPluginExtension {
    var reportPath = "build/reports/jacoco/test/jacocoTestReport.xml"
    var apiEndpoint = "https://coveralls.io/api/v1/jobs"
    var reportSourceSets: Iterable<File> = emptySet()
    var dryRun: Boolean = false
    var coverallsRequest: File? = null
}

class CoverallsJacocoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("coverallsJacoco", CoverallsJacocoPluginExtension::class.java)

        project.task("coverallsJacoco") {
            it.group = "verification"
            it.description = "Reports coverage to coveralls"
            it.doLast {
                val envGetter = { v: String -> System.getenv(v)?.ifBlank { null } }
                CoverallsReporter(envGetter).report(project)
            }
        }
    }
}

class Main {

}
