package org.gradle.plugin.coveralls.jacoco

import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactView
import org.gradle.api.artifacts.ArtifactView.ViewConfiguration
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.VerificationType
import org.gradle.api.tasks.SourceSetContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class SourceReportParserTest {
    private val testReport = File("src/test/resources/testreports/jacocoTestReport.xml")
    private val testReportMissingLines = File("src/test/resources/testreports/jacocoTestReportMissingLines.xml")
    private val testKotlinStyleSourceDir = File("src/test/resources/testrepo/src/main/kotlin")
    private val testKotlinStyleSourceDirAdditional = File("src/test/resources/testrepo/src/anotherMain/kotlin")
    private val testJavaStyleSourceDir = File("src/test/resources/testrepo/javaStyleSrc/main/kotlin")

    @Test
    fun `SourceReportParser parses skips parsing if source directories empty`() {
        val project = mockk<Project> {
            every { rootDir } returns File("src/test/resources/testrepo")
            every { configurations.findByName(any()) } returns null
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
            every { rootDir } returns File("src/test/resources/testrepo")
            every { configurations.findByName(any()) } returns null
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
                listOf(null, null, null, null, 1, 1, 1, 1, null, 1, 1, 0, 0, 1, 1, null, 1, 1, 1)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `SourceReportParser parses simple android jacoco report with kotlin styled root package`() {
        val project = mockk<Project> {
            every { rootDir } returns File("src/test/resources/testrepo")
            every { configurations.findByName(any()) } returns null
            every { extensions.getByType(SourceSetContainer::class.java) } returns mockk {
                every { getByName("main").allJava.srcDirs } returns setOf(testKotlinStyleSourceDir)
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

    @Test
    fun `SourceReportParser parses simple jacoco report without android classes`() {
        val project = mockk<Project> {
            every { rootDir } returns File("src/test/resources/testrepo")
            every { configurations.findByName(any()) } returns null
            every { extensions.getByType(SourceSetContainer::class.java) } returns mockk {
                every { getByName("main").allJava.srcDirs } returns setOf(testKotlinStyleSourceDir)
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

    @Test
    fun `SourceReportParser parses simple jacoco report with kotlin styled root package`() {
        val project = mockk<Project> {
            every { rootDir } returns File("src/test/resources/testrepo")
            every { configurations.findByName(any()) } returns null
            every { extensions.getByType(SourceSetContainer::class.java) } returns mockk {
                every { getByName("main").allJava.srcDirs } returns setOf(testKotlinStyleSourceDir)
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

    @Test
    fun `SourceReportParser uses report source sets and parses jacoco report`() {
        val project = mockk<Project> {
            every { rootDir } returns File("src/test/resources/testrepo")
            every { configurations.findByName(any()) } returns null
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
                listOf(null, null, null, null, 1, 1, 1, 1, null, 1, 1, 0, 0, 1, 1, null, 1, 1, 1)
            ),
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

    @Test
    fun `SourceReportParser uses Gradle 7 jacocoAggregation provided source sets and parses jacoco report`() {
        val project = mockk<Project> {
            every { rootDir } returns File("src/test/resources/testrepo")
            every { configurations.findByName("allCodeCoverageReportSourceDirectories") } returns mockk {
                every { files } returns emptySet()
                every { incoming } returns mockk {
                    val artifactViewConfigAction = slot<Action<ViewConfiguration>>()
                    every {
                        artifactView(capture(artifactViewConfigAction))
                    } answers {
                        artifactViewConfigAction.captured.execute(mockk {
                            every { componentFilter(any()) } returns this
                            every { lenient(true) } returns this
                        })
                        mockk {
                            every { files } returns mockk {
                                every { files } returns setOf( testKotlinStyleSourceDir,
                                    testKotlinStyleSourceDirAdditional)
                            }
                        }
                    }
                }
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
            ),
            SourceReport(
                "src/anotherMain/kotlin/Lib.kt",
                "8b5c1c773cf81996efc19a08f0ac3648",
                listOf(null, null, null, null, 1, 1, 1, null, null, null, null, null, null)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `SourceReportParser uses Gradle 8+ jacocoAggregation provided source sets and parses jacoco report`() {
        val project = mockk<Project> {
            every { rootDir } returns File("src/test/resources/testrepo")
            val bundling = mockk<Bundling>()
            val category = mockk<Category>()
            val verificationType = mockk<VerificationType>()
            every { objects } returns mockk {
                every { named(Bundling::class.java, Bundling.EXTERNAL) } returns bundling
                every { named(Category::class.java, Category.VERIFICATION) } returns category
                every { named(VerificationType::class.java, VerificationType.MAIN_SOURCES) } returns verificationType
            }
            every { configurations.findByName("allCodeCoverageReportSourceDirectories") } returns null
            every { configurations.findByName("aggregateCodeCoverageReportResults") } returns mockk {
                every { files } returns emptySet()
                every { incoming } returns mockk {
                    val artifactViewConfigAction = slot<Action<ViewConfiguration>>()
                    every {
                        artifactView(capture(artifactViewConfigAction))
                    } answers {
                        artifactViewConfigAction.captured.execute(mockk ArtifactViewConfig@ {
                            every { withVariantReselection() } returns this
                            every { componentFilter(any()) } returns this
                            val attributeContainerAction = slot<Action<AttributeContainer>>()
                            every {
                                attributes(capture(attributeContainerAction))
                            } answers {
                                attributeContainerAction.captured.execute(mockk {
                                    every { attribute(Bundling.BUNDLING_ATTRIBUTE, bundling) } returns this
                                    every { attribute(Category.CATEGORY_ATTRIBUTE, category) } returns this
                                    every {
                                        attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, verificationType)
                                    } returns this
                                })
                                this@ArtifactViewConfig
                            }
                        })
                        mockk {
                            every { files } returns mockk {
                                every { files } returns setOf( testKotlinStyleSourceDir,
                                    testKotlinStyleSourceDirAdditional)
                            }
                        }
                    }
                }
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
            ),
            SourceReport(
                "src/anotherMain/kotlin/Lib.kt",
                "8b5c1c773cf81996efc19a08f0ac3648",
                listOf(null, null, null, null, 1, 1, 1, null, null, null, null, null, null)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `SourceReportParser ignores lines in report that are missing in source`() {
        val project = mockk<Project> {
            every { rootDir } returns File("src/test/resources/testrepo")
            every { configurations.findByName(any()) } returns null
            every { extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns mockk {
                every { reportPath } returns testReportMissingLines.path
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
                listOf(null, null, null, null, 1, 1, 1, 1, null, 1, 1, 0, 0, 1, 1, null, 1, 1, 1)
            ),
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
