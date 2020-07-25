package org.gradle.plugin.coveralls.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

open class CoverallsJacocoPluginExtension {
    var rootPackage:String? = null
    var reportPath = "build/reports/jacoco/test/jacocoTestReport.xml"
}

class CoverallsJacocoPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("coverallsJacoco", CoverallsJacocoPluginExtension::class.java)

        project.task("coverallsJacoco") {
            it.doLast {
                SourceReportsParser.parse(project).forEach(::println)
            }
        }
    }
}