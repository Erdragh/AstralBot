package dev.erdragh.astralbot.handlers

import dev.erdragh.astralbot.LOGGER
import kotlinx.coroutines.*
import java.nio.file.*

class FileWatcher(private val directoryPath: Path, private val handler: (event: WatchEvent<Path>) -> Unit) {
    private var job: Job? = null
    private var watchService: WatchService? = null

    fun startWatching() {
        job = GlobalScope.launch(Dispatchers.IO) {
            watchService = FileSystems.getDefault().newWatchService()
            directoryPath.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            )

            try {
                while (isActive) {
                    LOGGER.info("Waiting for watchService WatchKey")
                    val key = watchService?.take() ?: break
                    LOGGER.info("Got watchService WatchKey")

                    for (event in key.pollEvents()) {
                        LOGGER.info("Event: {}", event.kind())
                        // Send the event to the channel
                        handler(event as WatchEvent<Path>)
                    }

                    key.reset()
                }
            } catch (_: ClosedWatchServiceException) {
                // Do nothing, this exception means we should just stop
            }

            LOGGER.info("WatchService ending")
        }
    }

    fun stopWatching() {
        watchService?.close()
        job?.cancel()
    }
}
