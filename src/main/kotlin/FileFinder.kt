package org.gradle.plugin.coveralls.jacoco

import java.io.File
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

class FileFinder(private val dirs: Iterable<File>) {
    private val logger: Logger by lazy { LogManager.getLogger(FileFinder::class.java) }
    private val packageRegex = """package\s+(\S+)""".toRegex()

    private val rootPackages = dirs.associateWith { dir ->
        dir.listFiles()
                ?.firstOrNull { it.extension == "kt" }
                ?.let { f ->
                    f.bufferedReader().useLines { seq ->
                        seq.firstOrNull { packageRegex.matches(it) }
                                ?.let {
                                    packageRegex.find(it)!!.groupValues[1].replace(".", "/")
                                }
                    }
                }
    }.also {
        it.forEach { (dir, pkg) ->
            pkg?.let {
                logger.info("mapped root package ${pkg.replace("/", ".")} for directory '$dir'")
            }
        }
    }

    fun find(file: File): File? {
        dirs.any { dir ->
            val adjustedFile = rootPackages[dir]?.let {
                File(file.path.replace(Regex("^$it/"), ""))
            } ?: file

            val f = File(dir, adjustedFile.toString())
            f.exists().also { exists ->
                if (exists) {
                    return f
                }
            }
        }

        logger.info("could not find file '$file'")
        return null
    }
}
