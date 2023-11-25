package dev.erdragh.erdbot

import java.io.File
import java.nio.file.FileSystems

object FAQHandler {
    private val faqDirectory = File("faq")

    init {
        val watcher = FileSystems.getDefault().newWatchService()
    }

    fun getFAQForId(id: String): String {
        if (!faqDirectory.exists() || !faqDirectory.isDirectory) {
            LOGGER.error("FAQ directory not specified as directory: ${faqDirectory.absolutePath}")
            return "Bot Error (Contact Bot Operator)"
        }
        val faqFiles = faqDirectory.listFiles { it -> it.name == "$id.md" }
        val faqFile = if (faqFiles?.isNotEmpty() == true) faqFiles[0] else null
        return faqFile?.readText(Charsets.UTF_8) ?: "No FAQ registered for id: `$id`"
    }

    fun suggestFAQIds(): List<String>? {
        return if (!faqDirectory.exists() || !faqDirectory.isDirectory) {
            LOGGER.error("FAQ directory not specified as directory: ${faqDirectory.absolutePath}")
            null
        } else {
            faqDirectory.listFiles { it -> it.extension == "md" }?.map { it.nameWithoutExtension }
        }
    }
}

