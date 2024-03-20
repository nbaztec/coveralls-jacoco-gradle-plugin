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
        val svcInfo = ServiceInfo("name", "repo_name", "number", "jobId", "jobNumber", "pr", "branch", "buildNumber")
        assertEquals("name", svcInfo.name)
        assertEquals("repo_name", svcInfo.repoName)
        assertEquals("number", svcInfo.number)
        assertEquals("jobId", svcInfo.jobId)
        assertEquals("jobNumber", svcInfo.jobNumber)
        assertEquals("pr", svcInfo.pr)
        assertEquals("branch", svcInfo.branch)
        assertEquals("buildNumber", svcInfo.buildUrl)
    }

    @Test
    fun `data class SourceReport`() {
        val cov = arrayListOf(1, null)
        val branches = arrayListOf(1, 0, 0, 1)
        val srcReport = SourceReport("1", "2", cov, branches)
        assertEquals("1", srcReport.name)
        assertEquals("2", srcReport.source_digest)
        assertEquals(cov, srcReport.coverage)
        assertEquals(branches, srcReport.branches)
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
        val req = Request(
            "repo_token",
            "service_name",
            "repo_name",
            "service_number",
            "service_job_id",
            "service_job_number",
            "service_pull_request",
            "service_branch",
            "service_build_url",
            gitInfo,
            true,
            "flag_name",
            sourceFiles
        )
        assertEquals("repo_token", req.repo_token)
        assertEquals("service_name", req.service_name)
        assertEquals("repo_name", req.repo_name)
        assertEquals("service_number", req.service_number)
        assertEquals("service_job_id", req.service_job_id)
        assertEquals("service_job_number", req.service_job_number)
        assertEquals("service_pull_request", req.service_pull_request)
        assertEquals("service_branch", req.service_branch)
        assertEquals("service_build_url", req.service_build_url)
        assertEquals(true, req.parallel)
        assertEquals("flag_name", req.flag_name)
        assertEquals(gitInfo, req.git)
        assertEquals(sourceFiles, req.source_files)
    }
}
