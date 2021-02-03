package org.gradle.plugin.coveralls.jacoco

import java.io.File
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.revwalk.RevWalk

data class Head(
    val id: String,
    val author_name: String,
    val author_email: String,
    val committer_name: String,
    val committer_email: String,
    val message: String
)

data class Remote(
    val name: String,
    val url: String
)

data class GitInfo(
    val head: Head,
    val branch: String,
    val remotes: Collection<Remote>
)

object GitInfoParser {
    private val logger: Logger by lazy { LogManager.getLogger(GitInfoParser::class.java) }

    fun parse(directory: File): GitInfo? {
        try {
            val repo = RepositoryBuilder().findGitDir(directory).build()
            val head = repo.let {
                val rev = repo.resolve("HEAD")

                val commit = RevWalk(repo).parseCommit(rev)

                println("commit message: ${commit.fullMessage.trim()}")
                println(rev.name)
                println(commit.name)
                println(commit.shortMessage)

                Head(
                        rev.name,
                        commit.authorIdent.name,
                        commit.authorIdent.emailAddress,
                        commit.committerIdent.name,
                        commit.committerIdent.emailAddress,
                        commit.fullMessage.trim(),
                )
            }

            val remotes = repo.let {
                val config = repo.config
                config.getSubsections("remote").map {
                    Remote(it, config.getString("remote", it, "url"))
                }
            }

            return GitInfo(head, repo.branch, remotes)
        } catch (e: Exception) {
            logger.info("unable to read git info: ${e.message}")
            return null
        }
    }
}
