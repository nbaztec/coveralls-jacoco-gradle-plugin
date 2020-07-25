package org.gradle.plugin.coveralls.jacoco

import org.dom4j.io.SAXReader
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import java.io.File

data class SourceReport(val name: String, val source: String, val coverage: Iterable<Int?>)

class SourceReportsParser {
    companion object {
        private fun read(reportPath: String, rootPackage: String?): Map<String, Map<Int, Int>> {
            val reader = SAXReader()
            val document = reader.read(File(reportPath))
            val root = document.rootElement

            val fullCoverage = mutableMapOf<String, MutableMap<Int, Int>>()
            root.elements("package").forEach { pkg ->
                val pkgName = pkg.attributeValue("name")
                val path = rootPackage?.let {
                    pkgName.replaceFirst("^$it".toRegex(), "")
                } ?: pkgName

                pkg.elements("sourcefile").forEach { sf ->
                    val sfName = sf.attributeValue("name")
                    val cov = fullCoverage.getOrDefault("$path/$sfName", mutableMapOf())

                    sf.elements("line").forEach { line ->
                        val lineIndex = line.attributeValue("nr").toInt() - 1

                        // jacoco doesn't count hits
                        cov[lineIndex] = if (line.attributeValue("ci").toInt() > 0) 1 else 0
                    }
                }
            }

            return fullCoverage.mapValues { (_, v) -> v.toMap() }.toMap()
        }

        fun parse(project: Project): Iterable<SourceReport> {
            val pluginExtension = project.extensions.getByName("coverallsJacoco") as CoverallsJacocoPluginExtension
            val kotlinExtension = project.extensions.getByName("kotlin") as KotlinProjectExtension

            val sourceDirs = kotlinExtension.sourceSets.getByName("main").kotlin.srcDirs.filterNotNull()

            return read(pluginExtension.reportPath, pluginExtension.rootPackage)
                    .mapNotNull { (filename, cov) ->
                        sourceDirs.find {
                            File(it, filename).exists()
                        }?.let { sourceFile ->
                            val lines = sourceFile.readLines()
                            val lineHits = arrayOfNulls<Int>(lines.size)

                            cov.forEach { (line, hits) -> lineHits[line] = hits }

                            val relPath = File(".").toURI().relativize(sourceFile.toURI()).toString()
                            SourceReport(relPath, lines.joinToString("\n"), lineHits.toList())
                        }
                    }
        }
    }
}