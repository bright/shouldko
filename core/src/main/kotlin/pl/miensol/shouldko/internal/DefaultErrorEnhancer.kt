package pl.miensol.shouldko.internal

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

private val globalFileLinesCache = ConcurrentHashMap<Path, List<String>>()

internal class DefaultErrorEnhancer(
        private val fileTree: FileTree = FileTree.currentWorkingDir,
        private val classLoader: ClassLoader = fileTree.javaClass.classLoader,
        private val fileLinesCache: MutableMap<Path, List<String>> = globalFileLinesCache,
        private val assertionStackTraceElementFinder: AssertionStackTraceElementFinder = CallingPackageStackTraceElementFinder()) : ErrorEnhancer {


    override fun <T : Throwable> enhance(exception: T, factory: (msg: String) -> T): T {
        val originalStackTrace = exception.stackTrace
        val assertionFrame = assertionStackTraceElementFinder(originalStackTrace.toList())
        return when {
            assertionFrame == null -> exception
            assertionFrame.fileName.isNullOrEmpty() -> exception
            else -> {
                val assertionValueSource = findAssertionContext(assertionFrame, classLoader, fileTree)
                if (assertionValueSource != null) {
                    val newMessage = assertionValueSource.source + " " + exception.message
                    factory(newMessage).apply {
                        this.stackTrace = originalStackTrace
                    }
                } else exception
            }
        }
    }

    private fun findAssertionContext(assertionFrame: StackTraceElement, classLoader: ClassLoader, fileTree: FileTree): AssertionContext? {
        val fileLeafPath = leafPathForFrame(assertionFrame, classLoader)
        val sourceLine = findSourceLine(fileTree, fileLeafPath, assertionFrame)
        return if (sourceLine.isNullOrEmpty()) {
            null
        } else {
            val shouldIndex = sourceLine!!.indexOf(".should")
            val sourceLineNoShould = if (shouldIndex == -1) sourceLine else sourceLine.substring(0, shouldIndex)
            AssertionContext(fileLeafPath, sourceLineNoShould, IntRange(assertionFrame.lineNumber, assertionFrame.lineNumber + 1))
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

    private fun leafPathForFrame(assertionFrame: StackTraceElement, classLoader: ClassLoader): Path {
        val className = assertionFrame.className
        val packageNameParts = packageName(classLoader, className)
                .split('.')
                .filterNot { it.isEmpty() }

        return if (packageNameParts.isEmpty()) {
            Paths.get(assertionFrame.fileName)
        } else {
            val firstPathComponent = packageNameParts.first()
            val remainingPathComponents = packageNameParts.drop(1) + listOf(assertionFrame.fileName)
            Paths.get(firstPathComponent, *remainingPathComponents.toTypedArray())
        }
    }

    private fun packageName(classLoader: ClassLoader, className: String): String {
        return classLoader.loadClass(className).`package`.name
    }
}