package dev.erdragh.astralbot.handlers

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.baseDirectory
import java.io.File
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

object FAQHandler {
    private val faqDirectory = File(baseDirectory, "faq")
    private val availableFAQIDs = HashSet<String>()
    private var watcher: FileWatcher? = null

    fun start() {
        LOGGER.info("FAQHandler loading")
        if (!faqDirectory.exists() && !faqDirectory.mkdir()) {
            LOGGER.error("Couldn't create FAQ directory")
        }
        if (!faqDirectory.exists() || !faqDirectory.isDirectory) {
            LOGGER.error("FAQ directory not specified as directory: ${faqDirectory.absolutePath}")
        } else {
            val findMarkdownRegex = Regex(".+\\.md$")
            val faqFiles =
                faqDirectory.listFiles { file -> !file.isDirectory }?.filter { it.name.matches(findMarkdownRegex) }
                    ?.map { it.nameWithoutExtension }
            faqFiles?.forEach(availableFAQIDs::add)

            watcher = FileWatcher(faqDirectory.toPath()) {
                LOGGER.info("Event Handling: {}", it.kind())
                val fileName = it.context().nameWithoutExtension
                val extension = it.context().extension
                if (extension == "md") when (it.kind()) {
                    StandardWatchEventKinds.ENTRY_CREATE -> {
                        availableFAQIDs.add(fileName)
                    }

                    StandardWatchEventKinds.ENTRY_DELETE -> {
                        availableFAQIDs.remove(fileName)
                    }
                }
            }
            watcher?.startWatching()

            LOGGER.info("FAQHandler loaded")
        }
    }

    fun getFAQForId(id: String): String {
        if (!faqDirectory.exists() || !faqDirectory.isDirectory) {
            LOGGER.error("FAQ directory not specified as directory: ${faqDirectory.absolutePath}")
            return "Bot Error (Contact Bot Operator)"
        }
        val faqFiles = faqDirectory.listFiles { file -> file.name == "$id.md" }
        val faqFile = if (faqFiles?.isNotEmpty() == true) faqFiles[0] else null
        return faqFile?.readText(Charsets.UTF_8) ?: "No FAQ registered for id: `$id`"
    }

    fun suggestFAQIds(slug: String): List<String> {
        return availableFAQIDs.filter { it.startsWith(slug, true) }
    }

    fun stop() {
        LOGGER.info("Shutting down FileSystem Watcher")
        watcher?.stopWatching()
    }
}

