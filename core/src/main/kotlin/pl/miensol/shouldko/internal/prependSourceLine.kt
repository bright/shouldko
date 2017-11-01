package pl.miensol.shouldko.internal

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

fun prependTopStackFrameSourceLine(job: () -> Unit) {
    try {
        job()
    } catch (originalError: AssertionError) {
        throw tryEnhanceError(originalError) { AssertionError(it) }
    }
}

private const val packageName = "pl.miensol.shouldko"

private val fileLinesCache = ConcurrentHashMap<Path, List<String>>()

private fun <T : Throwable> tryEnhanceError(originalError: T,
                                            fileTree: FileTree = FileTree.currentWorkingDir,
                                            classLoader: ClassLoader = fileTree.javaClass.classLoader,
                                            newError: (msg: String) -> T): T {
    val originalStackTrace = originalError.stackTrace

    val assertionFrame = findCallingStackFrame(originalStackTrace)

    return when {
        assertionFrame == null -> originalError
        assertionFrame.fileName.isNullOrEmpty() -> originalError
        else -> {
            val fileLeafPath = leafPathForFrame(assertionFrame, classLoader)
            val sourceLine = fineSourceLine(fileTree, fileLeafPath, assertionFrame)
            if (sourceLine.isNullOrEmpty()) {
                originalError
            } else {
                val shouldIndex = sourceLine!!.indexOf(".should")
                val sourceLineNoShould = if (shouldIndex == -1) sourceLine else sourceLine.substring(0, shouldIndex)
                val newMessage = sourceLineNoShould + " " + originalError.message
                newError(newMessage).apply {
                    stackTrace = originalStackTrace
                }
            }
        }
    }
}

private fun fineSourceLine(fileTree: FileTree, fileLeafPath: Path, assertionFrame: StackTraceElement): String? {
    return fileTree.fineByLeafPath(fileLeafPath)
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
    val packageNameParts = packageName(classLoader, className).split('.').filterNot { it.isNullOrEmpty() }
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

private fun findCallingStackFrame(stackTrace: Array<out StackTraceElement>?): StackTraceElement? {
    var reachedShouldKo = false
    stackTrace?.forEach { stackTraceElement ->
        reachedShouldKo = reachedShouldKo || stackTraceElement.className.startsWith(packageName)
        if (reachedShouldKo && !stackTraceElement.className.startsWith(packageName)) {
            return stackTraceElement
        }
    }
    return null
}