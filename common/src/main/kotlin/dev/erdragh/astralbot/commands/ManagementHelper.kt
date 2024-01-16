package dev.erdragh.astralbot.commands

import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

object ManagementHelper {
    private val systemBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

    fun getCpuUsage() = Pair(systemBean.processCpuLoad, systemBean.cpuLoad)
}