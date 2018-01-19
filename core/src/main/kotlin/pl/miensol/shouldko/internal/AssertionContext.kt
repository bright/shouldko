package pl.miensol.shouldko.internal

import java.nio.file.Path

internal data class AssertionContext(
    val sourceFilePath: Path,
    val source: String,
    val lines: IntRange
)