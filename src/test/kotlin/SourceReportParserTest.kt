package org.gradle.plugin.coveralls.jacoco

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class SourceReportParserTest {
    private val testReport = File("src/test/resources/testreports/jacocoTestReport.xml")
    private val testKotlinStyleSourceDir = File("src/test/resources/testrepo/src/main/kotlin")
    private val testKotlinStyleSourceDirAdditional = File("src/test/resources/testrepo/src/anotherMain/kotlin")
    private val testJavaStyleSourceDir = File("src/test/resources/testrepo/javaStyleSrc/main/kotlin")

    @Test
    fun `SourceReportParser parses skips parsing if source directories empty`() {
        val project = mockk<Project> {
            every { projectDir } returns File("src/test/resources/testrepo")
            every { extensions.findByType(BaseAppModuleExtension::class.java) } returns null
            every { extensions.getByType(SourceSetContainer::class.java) } returns mockk {
                every { getByName("main").allJava.srcDirs } returns emptySet()
            }
            every { extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns mockk {
                every { reportSourceSets } returns emptySet()
            }
        }

        val actual = SourceReportParser.parse(project)
        val expected = emptyList<SourceReport>()
        assertEquals(expected, actual)
    }

    @Test
    fun `SourceReportParser parses simple jacoco report with java styled package`() {
        val project = mockk<Project> {
            every { projectDir } returns File("src/test/resources/testrepo")
            every { extensions.findByType(BaseAppModuleExtension::class.java) } returns null
            every { extensions.getByType(SourceSetContainer::class.java) } returns mockk {
                every { getByName("main").allJava.srcDirs } returns setOf(testJavaStyleSourceDir)
            }
            every { extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns mockk {
                every { reportPath } returns testReport.path
                every { reportSourceSets } returns emptySet()
            }
        }

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
    fun `SourceReportParser parses simple android jacoco report with kotlin styled root package`() {
        val project = mockk<Project> {
            every { projectDir } returns File("src/test/resources/testrepo")
            every { extensions.findByType(BaseAppModuleExtension::class.java) } returns mockk {
                every { sourceSets.getByName("main").java.srcDirs } returns setOf(testKotlinStyleSourceDir)
            }
            every { extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns mockk {
                every { reportPath } returns testReport.path
                every { reportSourceSets } returns emptySet()
            }
        }

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
    fun `SourceReportParser parses simple jacoco report with kotlin styled root package`() {
        val project = mockk<Project> {
            every { projectDir } returns File("src/test/resources/testrepo")
            every { extensions.getByType(SourceSetContainer::class.java) } returns mockk {
                every { getByName("main").allJava.srcDirs } returns setOf(testKotlinStyleSourceDir)
            }
            every { extensions.findByType(BaseAppModuleExtension::class.java) } returns null
            every { extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns mockk {
                every { reportPath } returns testReport.path
                every { reportSourceSets } returns emptySet()
            }
        }

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
    fun `SourceReportParser uses report source sets and parses jacoco report`() {
        val project = mockk<Project> {
            every { projectDir } returns File("src/test/resources/testrepo")
            every { extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns mockk {
                every { reportPath } returns testReport.path
                every { reportSourceSets } returns listOf(
                        testKotlinStyleSourceDir,
                        testKotlinStyleSourceDirAdditional
                )
            }
        }

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
                        "src/anotherMain/kotlin/Lib.kt",
                        "8b5c1c773cf81996efc19a08f0ac3648",
                        listOf(null, null, null, null, 1, 1, 1, null, null, null, null, null, null)
                )
        )
        assertEquals(expected, actual)
    }
}
