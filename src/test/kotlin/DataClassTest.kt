package org.gradle.plugin.coveralls.jacoco

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// only required since kotlin data classes do not show up on coverage unless get/set operations are performed
internal class DataClassTest {
    @Test
    fun `data class Head`() {
        val head = Head("1", "2", "3", "4", "5", "6")
        assertEquals("1", head.id)
        assertEquals("2", head.author_name)
        assertEquals("3", head.author_email)
        assertEquals("4", head.committer_name)
        assertEquals("5", head.committer_email)
        assertEquals("6", head.message)
    }

    @Test
    fun `data class Remote`() {
        val remote = Remote("1", "2")
        assertEquals("1", remote.name)
        assertEquals("2", remote.url)
    }

    @Test
    fun `data class GitInfo`() {
        val head = mockk<Head>()
        val remote = mockk<Remote>()

        val gitInfo = GitInfo(head, "branch", listOf(remote))
        assertEquals(head, gitInfo.head)
        assertEquals("branch", gitInfo.branch)
        assertEquals(listOf(remote), gitInfo.remotes)
    }

    @Test
    fun `data class ServiceInfo`() {
        val svcInfo = ServiceInfo("1", "job number", "2", "3", "4")
        assertEquals("1", svcInfo.name)
        assertEquals("job number", svcInfo.number)
        assertEquals("2", svcInfo.jobId)
        assertEquals("3", svcInfo.pr)
        assertEquals("4", svcInfo.branch)
    }

    @Test
    fun `data class SourceReport`() {
        val cov = arrayListOf(1, null)
        val srcReport = SourceReport("1", "2", cov)
        assertEquals("1", srcReport.name)
        assertEquals("2", srcReport.source_digest)
        assertEquals(cov, srcReport.coverage)
    }

    @Test
    fun `data class Key`() {
        val key = Key("1", "2")
        assertEquals("1", key.pkg)
        assertEquals("2", key.file)
    }

    @Test
    fun `data class Request`() {
        val gitInfo = mockk<GitInfo>()
        val sourceFiles = emptyList<SourceReport>()
        val req = Request("1", "2", "job number", "3", "4", gitInfo, sourceFiles)
        assertEquals("1", req.repo_token)
        assertEquals("2", req.service_name)
        assertEquals("job number", req.service_number)
        assertEquals("3", req.service_job_id)
        assertEquals("4", req.service_pull_request)
        assertEquals(gitInfo, req.git)
        assertEquals(sourceFiles, req.source_files)
    }
}
