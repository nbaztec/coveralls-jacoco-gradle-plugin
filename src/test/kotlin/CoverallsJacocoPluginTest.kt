package org.gradle.plugin.coveralls.jacoco

import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CoverallsJacocoPluginTest {
    @Test
    fun `CoverallsJacocoPlugin creates extension and task with correct name`() {
        val project = mockk<Project>(relaxed = true)

        val plugin = CoverallsJacocoPlugin()
        plugin.apply(project)

        verify {
            project.extensions.create("coverallsJacoco", any<Class<CoverallsJacocoPluginExtension>>())
            project.task("coverallsJacoco", any<Action<Task>>())
        }
    }

    @Test
    fun `CoverallsJacocoPluginExtension has meaningful defaults`() {
        val extension = CoverallsJacocoPluginExtension()
        assertEquals(extension.reportPath, "build/reports/jacoco/test/jacocoTestReport.xml")
        assertEquals(extension.rootPackage, null)
        assertEquals(extension.reportSourceSets, emptySet<SourceSet>())
        assertEquals(extension.apiEndpoint, "https://coveralls.io/api/v1/jobs")
    }
}
