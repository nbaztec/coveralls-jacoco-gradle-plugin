package org.gradle.plugin.coveralls.jacoco

import com.sun.net.httpserver.HttpServer
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verifyAll
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.impldep.com.google.common.io.Files
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.PrintWriter
import java.net.InetSocketAddress

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CoverallsReporterTest {
    private val testReport = File("src/test/resources/testreports/jacocoTestReport.xml")
    private val testJavaStyleSourceDir = File("src/test/resources/testrepo/javaStyleSrc/main/kotlin")
    private val testRepo = File("src/test/resources/testrepo")

    @BeforeAll
    fun beforeAll() {
        Files.move(File(testRepo, "git"), File(testRepo, ".git"))
    }

    @AfterAll
    fun afterAll() {
        Files.move(File(testRepo, ".git"), File(testRepo, "git"))
    }

    @Test
    fun `CoverallsReporter skips sending report when empty`() {
        val envGetter = createEnvGetter(mapOf())

        val project = mockProject { sourceSetContainer, _ ->
            every { sourceSetContainer.getByName("main").allJava.srcDirs } returns emptySet()
        }

        val reporter = spyk(CoverallsReporter(envGetter), recordPrivateCalls = true)
        reporter.report(project)

        verifyAll(inverse = true) {
            reporter["send"](any<String>(), any<Request>())
        }
    }

    @Test
    fun `CoverallsReporter throws exception when repo token not set`() {
        val envGetter = createEnvGetter(mapOf())
        val project = mockProject()

        val reporter = CoverallsReporter(envGetter)
        assertThrows<IllegalStateException> { reporter.report(project) }.also {
            assertEquals("COVERALLS_REPO_TOKEN not set", it.message)
        }
    }

    @Test
    fun `CoverallsReporter throws exception on coveralls server error`() = runBlocking {
        val mockServer = HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
            createContext("/") { http ->
                http.sendResponseHeaders(404, 0)
                PrintWriter(http.responseBody).use { it.println("ERR") }
                http.close()
            }
        }
        GlobalScope.launch {
            mockServer.start()
        }

        val project = mockProject { _, pluginExtension ->
            every { pluginExtension.apiEndpoint } returns "http://${mockServer.address.hostName}:${mockServer.address.port}"
        }

        delay(50)
        val envGetter = createEnvGetter(mapOf("COVERALLS_REPO_TOKEN" to "test-token"))
        val reporter = CoverallsReporter(envGetter)
        try {
            assertThrows<Exception> { reporter.report(project) }.also {
                assertEquals("coveralls returned HTTP 404: ERR", it.message)
            }
        } finally {
            mockServer.stop(0)
        }

        Unit
    }

    @Test
    fun `CoverallsReporter parses info and sends report to coveralls server`() = runBlocking {
        var actual = ""
        val mockServer = HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
            createContext("/") { http ->
                actual = http.requestBody.reader().readText()
                        .trim()
                        .split("\r\n")
                        .drop(1).dropLast(1)
                        .joinToString("\n")

                http.sendResponseHeaders(200, 0)
                http.close()
            }
        }
        GlobalScope.launch {
            mockServer.start()
        }

        val envGetter = createEnvGetter(mapOf("COVERALLS_REPO_TOKEN" to "test-token"))
        val project = mockProject { _, pluginExtension ->
            every { pluginExtension.apiEndpoint } returns "http://${mockServer.address.hostName}:${mockServer.address.port}"
        }

        delay(50)
        val reporter = CoverallsReporter(envGetter)
        reporter.report(project)
        mockServer.stop(2)

        val expected = """Content-Disposition: form-data; name="json_file"; filename="json_file"
Content-Type: application/json; charset=UTF-8
Content-Transfer-Encoding: binary

{"repo_token":"test-token","service_name":"other","git":{"head":{"id":"4cd72eadcc34861139b338dd859344d419244e0b","author_name":"John Doe","author_email":"test@example.com","committer_name":"John Doe","committer_email":"test@example.com","message":"test commit\n"},"branch":"master","remotes":[{"name":"origin","url":"git@github.com:test/testrepo.git"}]},"source_files":[{"name":"javaStyleSrc/main/kotlin/foo/bar/baz/Main.kt","source_digest":"36083cd4c2ac736f9210fd3ed23504b5","coverage":[null,null,null,null,1,1,1,1,null,1,1,0,0,1,1,null,1,1,1]}]}
        """.trimIndent()

        assertEquals(expected, actual)
    }

    private fun createEnvGetter(entries: Map<String, String>): EnvGetter {
        return { k: String -> entries[k] }
    }

    private fun mockProject(optionsFunc: (SourceSetContainer, CoverallsJacocoPluginExtension) -> Unit = { _, _ -> Unit }): Project {
        val sourceDirectorySet = mockk<SourceDirectorySet>()
        every { sourceDirectorySet.srcDirs } returns setOf(testJavaStyleSourceDir)
        val sourceSetContainer = mockk<SourceSetContainer>()
        every { sourceSetContainer.getByName("main").allJava } returns sourceDirectorySet

        val pluginExtension = mockk<CoverallsJacocoPluginExtension>()
        every { pluginExtension.reportPath } returns testReport.path
        every { pluginExtension.rootPackage } returns null
        every { pluginExtension.reportPath } returns testReport.path
        every { pluginExtension.reportSourceSets } returns emptySet()

        val project = mockk<Project>()
        every { project.projectDir } returns testRepo
        every { project.extensions.getByType(SourceSetContainer::class.java) } returns sourceSetContainer
        every { project.extensions.getByType(CoverallsJacocoPluginExtension::class.java) } returns pluginExtension

        optionsFunc(sourceSetContainer, pluginExtension)

        return project
    }
}
