package dev.erdragh.astralbot.handlers

import dev.erdragh.astralbot.LOGGER
import dev.erdragh.astralbot.baseDirectory
import dev.erdragh.astralbot.config.AstralBotTextConfig
import java.io.File
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

/**
 * Handles everything related to the FAQs apart from responding to
 * the faq command.
 * @author Erdragh
 */
object FAQHandler {
    private val faqDirectory = File(baseDirectory, "faq")
    private val availableFAQIDs = HashSet<String>()
    private var watcher: FileWatcher? = null

    /**
     * Starts the FAQ Handler. This will start a [FileWatcher] to
     * update the available FAQ ids without having to restart the
     * entire server. This is very useful in case of large modpacks
     * where server startup can take minutes.
     */
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
                LOGGER.debug("Event Handling: {}", it.kind())
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

    /**
     * Produces the contents for the requested FAQ by reading it
     * from disk.
     * @param id the ID of the faq that will be read
     * @return the contents of the faq with the specified [id] or
     * a message notifying the user that there is no such FAQ
     */
    fun getFAQForId(id: String): String {
        if (!faqDirectory.exists() || !faqDirectory.isDirectory) {
            LOGGER.error("FAQ directory not specified as directory: ${faqDirectory.absolutePath}")
            return AstralBotTextConfig.FAQ_ERROR.get()
        }
        val faqFiles = faqDirectory.listFiles { file -> file.name == "$id.md" }
        val faqFile = if (faqFiles?.isNotEmpty() == true) faqFiles[0] else null
        return faqFile?.readText(Charsets.UTF_8) ?: AstralBotTextConfig.FAQ_NO_REGISTERED.get().replace("{{id}}", id)
    }

    /**
     * Filters the available FAQ ids based on the given [slug].
     * The filtering is done case-insensitive.
     * Used in the autocomplete implementation of the faq command.
     * @return a List of all available FAQ ids that start with the given [slug]
     */
    fun suggestFAQIds(slug: String): List<String> {
        return availableFAQIDs.filter { it.startsWith(slug, true) }
    }

    /**
     * Stops the FAQ Handler, which means stopping the associated
     * [FileWatcher]
     */
    fun stop() {
        LOGGER.info("Shutting down FileSystem Watcher")
        watcher?.stopWatching()
    }
}

