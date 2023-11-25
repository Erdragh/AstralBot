package dev.erdragh.erdbot

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.nameWithoutExtension

object FAQHandler {
    private val faqDirectory = File("faq")
    private val availableFAQIDs = ArrayList<String>()
    private var watcherThread: Thread? = null

    init {
        LOGGER.info("FAQHandler loading")
        if (!faqDirectory.exists() || !faqDirectory.isDirectory) {
            LOGGER.error("FAQ directory not specified as directory: ${faqDirectory.absolutePath}")
        } else {
            val findMarkdownRegex = Regex(".+\\.md$")
            val faqFiles = faqDirectory.listFiles { it -> !it.isDirectory }?.filter { it.name.matches(findMarkdownRegex) }?.map { it.nameWithoutExtension }
            faqFiles?.forEach(availableFAQIDs::add)
            watcherThread = (Thread {
                val watcher = FileSystems.getDefault().newWatchService()

                val faqDirectoryPath = faqDirectory.toPath()
                faqDirectoryPath.register(
                    watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE
                )

                try {
                    var key: WatchKey = watcher.take()
                    while (!Thread.currentThread().isInterrupted) {
                        for (event in key.pollEvents()) {
                            val ctx = event.context()
                            val path = event.context() as Path
                            LOGGER.debug("Handling File Event: {} for {}", event.kind(), ctx)
                            if (!path.isDirectory() && path.extension == "md") {
                                when (event.kind()) {
                                    StandardWatchEventKinds.ENTRY_CREATE -> {
                                        this.availableFAQIDs.add(path.nameWithoutExtension)
                                    }

                                    StandardWatchEventKinds.ENTRY_DELETE -> {
                                        this.availableFAQIDs.remove(path.nameWithoutExtension)
                                    }
                                }
                            }
                        }
                        key.reset()
                        key = watcher.take()
                    }
                } catch (_: InterruptedException) {
                    // Do Nothing. If this thread is interrupted it means it should just stop
                }
                println("Thread done")
            })
            watcherThread?.start()
        }
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

    fun suggestFAQIds(slug: String): List<String> {
        return availableFAQIDs.filter { it.startsWith(slug, true) }
    }

    fun shutdownWatcher() {
        watcherThread?.interrupt()
    }
}

