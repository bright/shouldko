package pl.miensol.shouldko.internal

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

private const val PackageName = "pl.miensol.shouldko"

private val globalFileLinesCache = ConcurrentHashMap<Path, List<String>>()

internal class DefaultErrorEnhancer(
        private val fileTree: FileTree = FileTree.currentWorkingDir,
        private val classLoader: ClassLoader = fileTree.javaClass.classLoader,
        private val fileLinesCache: MutableMap<Path, List<String>> = globalFileLinesCache) : ErrorEnhancer {


    override fun <T : Throwable> enhance(exception: T, factory: (msg: String) -> T): T {
        return tryEnhanceError(exception, fileTree, classLoader, factory)
    }

    private fun <T : Throwable> tryEnhanceError(originalError: T,
                                                fileTree: FileTree = FileTree.currentWorkingDir,
                                                classLoader: ClassLoader = fileTree.javaClass.classLoader,
                                                newError: (msg: String) -> T): T {
        val originalStackTrace = originalError.stackTrace

        val assertionFrame = findCallingAssertionStackElement(originalStackTrace)

        return when {
            assertionFrame == null -> originalError
            assertionFrame.fileName.isNullOrEmpty() -> originalError
            else -> {
                val assertionValueSource = findAssertionContext(assertionFrame, classLoader, fileTree)
                return if (assertionValueSource != null) {
                    val newMessage = assertionValueSource.source + " " + originalError.message
                    newError(newMessage).apply {
                        stackTrace = originalStackTrace
                    }
                } else originalError
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

    private fun findCallingAssertionStackElement(stackTrace: Array<out StackTraceElement>?): StackTraceElement? {
        var reachedShouldKo = false
        stackTrace?.forEach { stackTraceElement ->
            reachedShouldKo = reachedShouldKo || stackTraceElement.className.startsWith(PackageName)
            if (reachedShouldKo && !stackTraceElement.className.startsWith(PackageName)) {
                return stackTraceElement
            }
        }
        return null
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