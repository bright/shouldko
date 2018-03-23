package pl.miensol.shouldko.internal

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

private val globalFileLinesCache = ConcurrentHashMap<Path, List<String>>()

internal class DefaultErrorEnhancer(
        private val fileTree: FileTree = FileTree.currentWorkingDir,
        private val fileLinesCache: MutableMap<Path, List<String>> = globalFileLinesCache,
        private val assertionStackTraceElementFinder: AssertionStackTraceElementFinder = CallingPackageStackTraceElementFinder()) : ErrorEnhancer {


    override fun <T : Throwable> enhance(exception: T, factory: (msg: String) -> T): T {
        val originalStackTrace = exception.stackTrace
        val assertionFrame = assertionStackTraceElementFinder(originalStackTrace.toList())
        val assertionValueSource = assertionFrame?.fileName?.let { findAssertionContext(assertionFrame, fileTree) }
        return if (assertionValueSource != null) {
            val newMessage = assertionValueSource.source + " " + exception.message
            factory(newMessage).apply {
                this.stackTrace = originalStackTrace
            }
        } else exception
    }

    private fun findAssertionContext(assertionFrame: StackTraceElement, fileTree: FileTree): AssertionContext? {
        return try {
            val fileLeafPath = pathForFrame(assertionFrame)
            val sourceLine = fileLeafPath?.let { findSourceLine(fileTree, fileLeafPath, assertionFrame) }

            if (sourceLine.isNullOrEmpty()) {
                null
            } else {
                val shouldIndex = sourceLine!!.indexOf(".should")
                val sourceLineNoShould = if (shouldIndex == -1) sourceLine else sourceLine.substring(0, shouldIndex)
                AssertionContext(fileLeafPath, sourceLineNoShould, IntRange(assertionFrame.lineNumber, assertionFrame.lineNumber + 1))
            }
        } catch (e: NoClassDefFoundError) {//Android does not have Path
            null
        }
    }


    private fun findSourceLine(fileTree: FileTree, fileLeafPath: Path, assertionFrame: StackTraceElement): String? {
        return fileTree.findByLeafPath(fileLeafPath)
                .asSequence()
                .map { it.toAbsolutePath() }
                .mapNotNull {
                    val lines = fileLinesCache.computeIfAbsent(it) { Files.readAllLines(it) }
                    lines.getOrNull(assertionFrame.lineNumber - 1)
                }
                .firstOrNull()
                ?.trim()
    }

    private fun pathForFrame(assertionFrame: StackTraceElement): Path? {
        val assertionFileName = assertionFrame.fileName

        return if (!assertionFileName.isNullOrEmpty()) {
            val packageNameParts = packageName(assertionFrame.className)
                    ?.split('.')
                    ?.filterNot { it.isEmpty() }

            return if (packageNameParts == null || packageNameParts.isEmpty()) {
                Paths.get(assertionFileName)
            } else {
                val firstPathComponent = packageNameParts.first()
                val remainingPathComponents = packageNameParts.drop(1) + listOf(assertionFileName)
                Paths.get(firstPathComponent, *remainingPathComponents.toTypedArray())
            }
        } else null
    }


}