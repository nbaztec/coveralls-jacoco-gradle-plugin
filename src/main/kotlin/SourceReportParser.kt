package org.gradle.plugin.coveralls.jacoco

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.dom4j.io.SAXReader
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest


data class SourceReport(val name: String, val source_digest: String, val coverage: List<Int?>)

data class Key(val pkg: String, val file: String)

object SourceReportParser {
    private val logger: Logger by lazy { LogManager.getLogger(CoverallsReporter::class.java) }

    private fun read(reportPath: String): Map<Key, Map<Int, Int>> {
        val reader = SAXReader()
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

        val document = reader.read(File(reportPath))
        val root = document.rootElement

        val fullCoverage = mutableMapOf<Key, MutableMap<Int, Int>>()
        root.elements("package").forEach { pkg ->
            val pkgName = pkg.attributeValue("name")

            pkg.elements("sourcefile").forEach { sf ->
                val sfName = sf.attributeValue("name")
                val key = Key(pkgName, sfName)

                if (fullCoverage[key] == null) {
                    fullCoverage[key] = mutableMapOf()
                }

                sf.elements("line").forEach { line ->
                    val lineIndex = line.attributeValue("nr").toInt() - 1

                    // jacoco doesn't count hits
                    fullCoverage.getValue(key)[lineIndex] = if (line.attributeValue("ci").toInt() > 0) 1 else 0
                }
            }
        }

        logger.info("parsed coverage at $reportPath")

        return fullCoverage.mapValues { (_, v) -> v.toMap() }.toMap()
    }

    private fun File.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(readBytes())).toString(16).padStart(32, '0')
    }

    fun parse(project: Project): List<SourceReport> {
        val pluginExtension = project.extensions.getByType(CoverallsJacocoPluginExtension::class.java)

        val sourceDirs = if (pluginExtension.reportSourceSets.count() == 0) {
            val androidExtension = project.extensions.findByType(BaseAppModuleExtension::class.java)
            androidExtension?.let {
                androidExtension.sourceSets.getByName("main").java.srcDirs.filterNotNull()
            } ?: project.extensions.getByType(SourceSetContainer::class.java)
                    .getByName("main").allJava.srcDirs.filterNotNull()

        } else {
            pluginExtension.reportSourceSets.toList()
        }

        if (sourceDirs.isEmpty()) {
            return emptyList()
        }

        val fileFinder = FileFinder(sourceDirs)

        logger.info("using source directories: $sourceDirs")
        return read(pluginExtension.reportPath)
                .mapNotNull { (key, cov) ->
                    fileFinder.find(File(key.pkg, key.file))?.let { f ->
                        logger.debug("found file: $f")

                        val lines = f.readLines()
                        val lineHits = arrayOfNulls<Int>(lines.size)

                        cov.forEach { (line, hits) ->
                            if (line < lines.size) {
                                lineHits[line] = hits
                            } else {
                                logger.debug("skipping invalid line $line, (total ${lines.size})")
                            }
                        }

                        val relPath = File(project.projectDir.absolutePath).toURI().relativize(f.toURI()).toString()
                        SourceReport(relPath, f.md5(), lineHits.toList())
                    }.also {
                        it
                                ?: logger.info("${key.file} could not be found in any of the source directories, skipping")
                    }
                }
    }
}
