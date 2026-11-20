package dev.erdragh.changelog

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.create
import java.io.File
import javax.inject.Inject

private data class ChangelogContent(
    val version: String,
    val changes: String
)

private val versionPattern = Regex("^##\\s*(.*)$")
private fun extractChangelogContent(changelogFile: File, project: Project): ChangelogContent {
    if (!changelogFile.exists()) throw GradleException("Changelog file not found at: ${changelogFile.absolutePath}")

    val lines = changelogFile.readLines()
    var version: String? = null
    var versionStart: Int? = null
    var versionEnd: Int? = null

    // Find the first version header (## ...)
    for ((i, line) in lines.withIndex()) {
        val matched = versionPattern.find(line)
        if (matched != null) {
            if (versionStart != null) {
                // Found the start of the next section, so this is the end of the current one
                versionEnd = i
                break
            }
            version = matched.groupValues[1].trim()
            versionStart = i
        }
    }

    if (version == null || versionStart == null) {
        throw GradleException("Could not find a Version (## ...) in ${changelogFile.name} during configuration.")
    }
    // If no subsequent version is found, the changes run until the end of the file
    versionEnd = versionEnd ?: (lines.size)

    // Slice the lines from the version header (inclusive) up to the next header (exclusive)
    val changes = lines.slice(versionStart until versionEnd).joinToString(separator = "\n")

    project.logger.debug("Extracted version {} and changes ({} lines) from {}", version, versionEnd - versionStart, changelogFile.name)

    return ChangelogContent(version, changes)
}

abstract class ChangelogExtension @Inject constructor(objects: ObjectFactory, project: Project) {
    val changelogPath = objects.property(File::class.java).convention(project.file("CHANGELOG.md"))
    private val contentProvider = changelogPath.map { extractChangelogContent(it, project) }
    val changelogVersion = contentProvider.map(ChangelogContent::version)
    val changelogChanges = contentProvider.map(ChangelogContent::changes)
}

class ChangelogExtractorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create<ChangelogExtension>("changelog", target.objects, target)
    }
}