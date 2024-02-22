package dev.erdragh.astralbot.commands.discord

import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

/**
 * This exists in a separate file to prevent loading the
 * com.sun.management classes if they're not available
 *
 * @author Erdragh
 */
object ManagementHelper {
    private val systemBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

    /**
     * Accesses the system via certain com.sun.management classes
     * to receive CPU Usage information
     * @return Pair of process and system CPU Usage
     */
    fun getCpuUsage() = Pair(systemBean.processCpuLoad, systemBean.cpuLoad)
}