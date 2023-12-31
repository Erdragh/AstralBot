package dev.erdragh.astralbot.handlers

import dev.erdragh.astralbot.LOGGER
import kotlinx.coroutines.*
import java.nio.file.*

/**
 * Abstraction around Java's [WatchService] API that makes it easy to
 * set up a handler for file changes in a non-blocking manner.
 *
 * @param directoryPath the [Path] which will be watched for file changes
 * @param handler the function that gets called when an Event gets triggered
 * @author Erdragh
 */
class FileWatcher(private val directoryPath: Path, private val handler: (event: WatchEvent<Path>) -> Unit) {
    // These two references are needed to be able to stop
    // the activities running in parallel to the main thread
    private var job: Job? = null
    private var watchService: WatchService? = null

    /**
     * Starts the file system watcher in parallel using Kotlin's coroutines
     * in the [GlobalScope] using the [Dispatchers.IO] dispatcher.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun startWatching() {
        job = GlobalScope.launch(Dispatchers.IO) {
            LOGGER.info("FileSystem Watcher starting on: {}", directoryPath)
            watchService = FileSystems.getDefault().newWatchService()
            directoryPath.register(
                watchService!!,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            )

            try {
                while (isActive) {
                    val key = watchService?.take() ?: break

                    for (event in key.pollEvents()) {
                        // Send the event to the channel
                        @Suppress("UNCHECKED_CAST")
                        handler(event as WatchEvent<Path>)
                    }

                    key.reset()
                }
            } catch (_: ClosedWatchServiceException) {
                // Do nothing, this exception means we should just stop
            }
            LOGGER.info("FileSystem Watcher stopping for: {}", directoryPath)
        }
    }

    /**
     * Stops the file system watcher by closing the [watchService]
     * and cancelling the [job] running in parallel
     */
    fun stopWatching() {
        watchService?.close()
        job?.cancel()
    }
}
