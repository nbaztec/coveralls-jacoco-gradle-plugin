package org.gradle.plugin.coveralls.jacoco.androidTest

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.plugin.coveralls.jacoco.CoverallsJacocoPluginExtension
import org.gradle.plugin.coveralls.jacoco.SourceReport
import org.gradle.plugin.coveralls.jacoco.SourceReportParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class SourceReportParserTest {
    private val testReport = File("src/test/resources/testreports/jacocoTestReport.xml")
    private val testKotlinStyleSourceDir = File("src/test/resources/testrepo/src/main/kotlin")

    @Test
    fun `SourceReportParser parses simple jacoco report with android classes`() {
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
                listOf(null, null, null, null, 1, 1, 1, 1, null, 1, 1, 0, 0, 1, 1, null, 1, 1, 1)
            ),
            SourceReport(
                "src/main/kotlin/internal/Util.kt",
                "805ee340f4d661be591b4eb42f6164d2",
                listOf(null, null, null, null, 1, 1, 1, null, null)
            )
        )
        assertEquals(expected, actual)
    }
}
