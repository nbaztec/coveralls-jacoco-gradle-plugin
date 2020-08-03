package org.gradle.plugin.coveralls.jacoco

import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class SourceReportParserTest {
    private val testReport = File("src/test/resources/testreports/jacocoTestReport.xml")
    private val testKotlinStyleSourceDir = File("src/test/resources/testrepo/src/main/kotlin")
    private val testJavaStyleSourceDir = File("src/test/resources/testrepo/javaStyleSrc/main/kotlin")

    @Test
    fun `SourceReportParser parses skips parsing if source directories empty`() {
        val project = mockk<Project>()

        val sourceSetContainer = mockk<SourceSetContainer>()
        every { sourceSetContainer.getByName("main").allJava.srcDirs } returns emptySet()

        val pluginExtension = mockk<CoverallsJacocoPluginExtension>()
        every { pluginExtension.reportSourceSets } returns emptySet()

        every { project.projectDir } returns File("src/test/resources/testrepo")
        every { project.extensions.getByType(SourceSetContainer::class.java) } returns sourceSetContainer
        every { project.extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns pluginExtension

        val actual = SourceReportParser.parse(project)
        val expected = emptyList<SourceReport>()
        assertEquals(expected, actual)
    }

    @Test
    fun `SourceReportParser parses simple jacoco report with java styled package`() {
        val project = mockk<Project>()

        val sourceSetContainer = mockk<SourceSetContainer>()
        every { sourceSetContainer.getByName("main").allJava.srcDirs } returns setOf(testJavaStyleSourceDir)

        val pluginExtension = mockk<CoverallsJacocoPluginExtension>()
        every { pluginExtension.reportPath } returns testReport.path
        every { pluginExtension.rootPackage } returns null
        every { pluginExtension.reportSourceSets } returns emptySet()

        every { project.projectDir } returns File("src/test/resources/testrepo")
        every { project.extensions.getByType(SourceSetContainer::class.java) } returns sourceSetContainer
        every { project.extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns pluginExtension

        val actual = SourceReportParser.parse(project)
        val expected = listOf(
                SourceReport(
                        "javaStyleSrc/main/kotlin/foo/bar/baz/Main.kt",
                        "36083cd4c2ac736f9210fd3ed23504b5",
                        listOf(null, null, null, null, 1, 1, 1, 1, null, 1, 1, 0, 0, 1, 1, null, 1, 1, 1))
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `SourceReportParser parses simple jacoco report with kotlin styled rootPackage`() {
        val project = mockk<Project>()

        val sourceSetContainer = mockk<SourceSetContainer>()
        every { sourceSetContainer.getByName("main").allJava.srcDirs } returns setOf(testKotlinStyleSourceDir)

        val pluginExtension = mockk<CoverallsJacocoPluginExtension>()
        every { pluginExtension.reportPath } returns testReport.path
        every { pluginExtension.rootPackage } returns "foo.bar.baz"
        every { pluginExtension.reportSourceSets } returns emptySet()

        every { project.projectDir } returns File("src/test/resources/testrepo")
        every { project.extensions.getByType(SourceSetContainer::class.java) } returns sourceSetContainer
        every { project.extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns pluginExtension

        val actual = SourceReportParser.parse(project)
        val expected = listOf(
                SourceReport(
                        "src/main/kotlin/Main.kt",
                        "36083cd4c2ac736f9210fd3ed23504b5",
                        listOf(null, null, null, null, 1, 1, 1, 1, null, 1, 1, 0, 0, 1, 1, null, 1, 1, 1)),
                SourceReport(
                        "src/main/kotlin/internal/Util.kt",
                        "805ee340f4d661be591b4eb42f6164d2",
                        listOf(null, null, null, null, 1, 1, 1, null, null)
                )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `SourceReportParser uses additional source sets and parses jacoco report`() {
        val project = mockk<Project>()

        val mainSourceSet = mockk<SourceSet>()
        every { mainSourceSet.allJava.srcDirs } returns setOf(testKotlinStyleSourceDir)

        val sourceSetContainer = mockk<SourceSetContainer>()
        every { sourceSetContainer.getByName("main") } returns mainSourceSet

        val additionalSourceDirectorySet = mockk<SourceDirectorySet>()
        every { additionalSourceDirectorySet.srcDirs } returns setOf(File("src/test/resources/testrepo/src/anotherMain/kotlin"))
        val additionalSourceSet = mockk<SourceSet>()
        every { additionalSourceSet.allJava } returns additionalSourceDirectorySet

        val pluginExtension = mockk<CoverallsJacocoPluginExtension>()
        every { pluginExtension.reportPath } returns testReport.path
        every { pluginExtension.rootPackage } returns "foo.bar.baz"
        every { pluginExtension.reportSourceSets } returns listOf(mainSourceSet, additionalSourceSet)

        every { project.projectDir } returns File("src/test/resources/testrepo")
        every { project.extensions.getByType(SourceSetContainer::class.java) } returns sourceSetContainer
        every { project.extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns pluginExtension

        val actual = SourceReportParser.parse(project)
        val expected = listOf(
                SourceReport(
                        "src/main/kotlin/Main.kt",
                        "36083cd4c2ac736f9210fd3ed23504b5",
                        listOf(null, null, null, null, 1, 1, 1, 1, null, 1, 1, 0, 0, 1, 1, null, 1, 1, 1)),
                SourceReport(
                        "src/main/kotlin/internal/Util.kt",
                        "805ee340f4d661be591b4eb42f6164d2",
                        listOf(null, null, null, null, 1, 1, 1, null, null)
                ),
                SourceReport(
                        "src/anotherMain/kotlin/lib/Lib.kt",
                        "8b5c1c773cf81996efc19a08f0ac3648",
                        listOf(null, null, null, null, 1, 1, 1, null, null, null, null, null, null)
                )
        )
        assertEquals(expected, actual)
    }
}