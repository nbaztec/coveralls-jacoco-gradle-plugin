package org.gradle.plugin.coveralls.jacoco

import com.google.gson.Gson
import org.apache.http.client.config.RequestConfig
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
    val repo_name: String?,
    val service_number: String?,
    val service_job_id: String?,
    val service_job_number: String?,
    val service_pull_request: String?,
    val service_branch: String?,
    val service_build_url: String?,
    val git: GitInfo?,
    val parallel: Boolean?,
    val flag_name: String?,
    val source_files: List<SourceReport>,
)

private fun Request.json() = Gson().toJson(this)

typealias EnvGetter = (String) -> String?
typealias SourceFilesSanitiser = (List<SourceReport>) -> Void

class CoverallsReporter(val envGetter: EnvGetter, val sourceFilesSanitiser: SourceFilesSanitiser? = null) {
    private val logger: Logger by lazy { LogManager.getLogger(CoverallsReporter::class.java) }
    private val defaultHttpTimeoutMs = 10 * 1000

    fun report(project: Project) {
        val pluginExtension = project.extensions.getByType(CoverallsJacocoPluginExtension::class.java)

        logger.info("retrieving git info")
        val gitInfo = GitInfoParser.parse(project.projectDir)

        logger.info("parsing source files")
        val sourceFiles = SourceReportParser.parse(project)
        if (sourceFilesSanitiser != null) {
            sourceFilesSanitiser.invoke(sourceFiles)
        }

        if (sourceFiles.count() == 0) {
            logger.info("source file set empty, skipping")
            return
        }

        logger.info("retrieving ci service info")
        val serviceInfo = ServiceInfoParser(envGetter).parse()
        val options = OptionsParser(envGetter).parse()

        val req = Request(
            repo_token = options.repoToken,
            repo_name = serviceInfo.repoName,
            service_name = serviceInfo.name,
            service_number = serviceInfo.number,
            service_job_id = serviceInfo.jobId,
            service_job_number = serviceInfo.jobNumber,
            service_pull_request = serviceInfo.pr,
            service_branch = serviceInfo.branch,
            service_build_url = serviceInfo.buildUrl,
            git = gitInfo,
            parallel = options.parallel,
            flag_name = options.flagName,
            source_files = sourceFiles,
        )

        send(pluginExtension.apiEndpoint, req)
    }

    private fun send(endpoint: String, req: Request) {
        val httpClient = HttpClients.createDefault()
        val httpPost = HttpPost(endpoint).apply {
            config = RequestConfig
                .custom()
                .setConnectTimeout(defaultHttpTimeoutMs)
                .setSocketTimeout(defaultHttpTimeoutMs)
                .setConnectionRequestTimeout(defaultHttpTimeoutMs)
                .build()

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
            throw Exception(
                "coveralls returned HTTP ${res.statusLine.statusCode}: ${
                    EntityUtils.toString(res.entity).trim()
                }"
            )
        }
        logger.info("OK")
    }
}
