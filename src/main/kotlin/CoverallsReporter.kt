package org.gradle.plugin.coveralls.jacoco


import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.gradle.api.Project


data class Request(
        val repo_token: String,
        val service_name: String,
        val service_job_id: String?,
        val service_pull_request: String?,
        val git: GitInfo?,
        val source_files: List<SourceReport>
)

private fun Request.json() = Gson().toJson(this)

typealias EnvGetter = (String) -> String?

class CoverallsReporter(val envGetter: EnvGetter) {
    private val logger: Logger by lazy { LogManager.getLogger(CoverallsReporter::class.java) }

    fun report(project: Project) {
        logger.info("retrieving git info")
        val gitInfo = GitInfoParser.parse(project.projectDir)

        logger.info("parsing source files")
        val sourceFiles = SourceReportParser.parse(project)

        if (sourceFiles.count() == 0) {
            logger.info("source file set empty, skipping")
            return
        }

        logger.info("retrieving ci service info")
        val serviceInfo = ServiceInfoParser(envGetter).parse()

        val repoToken = System.getenv("COVERALLS_REPO_TOKEN")
        check(repoToken.isNotBlank()) { "COVERALLS_REPO_TOKEN not set" }

        val req = Request(
                repoToken,
                serviceInfo.name,
                serviceInfo.jobId,
                serviceInfo.pr,
                gitInfo,
                sourceFiles
        )
        val pluginExtension = project.extensions.getByType(CoverallsJacocoPluginExtension::class.java)

        send(pluginExtension.apiEndpoint, req)
    }

    private fun send(endpoint: String, req: Request) {
        val httpClient = HttpClients.createDefault()
        val httpPost = HttpPost(endpoint).apply {
            entity = MultipartEntityBuilder
                    .create()
                    .addBinaryBody(
                            "json_file",
                            req.json().toByteArray(Charsets.UTF_8),
                            ContentType.APPLICATION_JSON,
                            "json_file"
                    )
                    .build()
        }

        logger.info("sending payload to coveralls")
        logger.debug(req.json())

        val res = httpClient.execute(httpPost)
        if (res.statusLine.statusCode != 200) {
            throw Exception("coveralls returned HTTP ${res.statusLine.statusCode}: ${EntityUtils.toString(res.entity)}")
        }
        logger.info("OK")
    }
}