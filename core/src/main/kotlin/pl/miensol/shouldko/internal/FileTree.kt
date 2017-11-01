package pl.miensol.shouldko.internal

import java.io.File
import java.nio.file.Path

class FileTree(private val root: File) {
    private val files by lazy {
        root.walkTopDown()
                .filter { it.isFile }
                .map { it.toPath() }
                .toList()
    }

    fun fineByLeafPath(subPath: Path): List<Path> {
        return files.filter { it.endsWith(subPath) }
    }

    companion object {
        val currentWorkingDir by lazy { FileTree(File(System.getProperty("user.dir"))) }
    }
}