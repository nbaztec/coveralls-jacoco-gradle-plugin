package org.gradle.plugin.coveralls.jacoco

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.testfixtures.ProjectBuilder
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
    fun `CoverallsJacocoPlugin runs on task execute`() {
        val sourceSetContainer = mockk<SourceSetContainer> {
            every { getByName("main").allJava.srcDirs } returns emptySet()
        }
        val project = ProjectBuilder.builder().withProjectDir(File(".")).build()
        project.extensions.add("sourceSets", sourceSetContainer)

        val plugin = CoverallsJacocoPlugin()
        plugin.apply(project)

        val task = project.tasks.getByName("coverallsJacoco")
        task.actions.forEach { it.execute(task) }
    }

    @Test
    fun `CoverallsJacocoPluginExtension has meaningful defaults`() {
        val extension = CoverallsJacocoPluginExtension()
        assertEquals(extension.reportPath, "build/reports/jacoco/test/jacocoTestReport.xml")
        assertEquals(extension.reportSourceSets, emptySet<SourceSet>())
        assertEquals(extension.apiEndpoint, "https://coveralls.io/api/v1/jobs")
    }
}
