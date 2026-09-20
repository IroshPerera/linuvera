package io.github.iroshperera.linuvera.system;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

public final class SystemMetricsService {

    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final CentralProcessor processor;
    private final GlobalMemory memory;
    private final OperatingSystem operatingSystem;
    private final FileSystem fileSystem;

    private long[] previousCpuTicks;

    public SystemMetricsService() {
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.processor = hardware.getProcessor();
        this.memory = hardware.getMemory();
        this.operatingSystem = systemInfo.getOperatingSystem();
        this.fileSystem = operatingSystem.getFileSystem();

        this.previousCpuTicks =
                processor.getSystemCpuLoadTicks();
    }

    public SystemMetrics collectMetrics() {

        long[] currentCpuTicks =
                processor.getSystemCpuLoadTicks();

        double cpuUsage =
                processor.getSystemCpuLoadBetweenTicks(
                        previousCpuTicks
                ) * 100.0;

        previousCpuTicks = currentCpuTicks;

        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;

        DiskMetrics diskMetrics =
                collectRootDiskMetrics();

        long uptimeSeconds =
                operatingSystem.getSystemUptime();

        String osFamily =
                operatingSystem.getFamily();

        String osVersion =
                operatingSystem
                        .getVersionInfo()
                        .getVersion();

        return new SystemMetrics(
                roundToOneDecimal(cpuUsage),
                usedMemory,
                totalMemory,
                osFamily,
                osVersion,
                diskMetrics.usagePercent(),
                diskMetrics.usedBytes(),
                diskMetrics.totalBytes(),
                uptimeSeconds
        );
    }

    private DiskMetrics collectRootDiskMetrics() {

        for (OSFileStore fileStore :
                fileSystem.getFileStores()) {

            if ("/".equals(fileStore.getMount())) {

                long totalBytes =
                        fileStore.getTotalSpace();

                long usableBytes =
                        fileStore.getUsableSpace();

                long usedBytes =
                        Math.max(
                                0,
                                totalBytes - usableBytes
                        );

                double usagePercent = 0;

                if (totalBytes > 0) {
                    usagePercent =
                            usedBytes * 100.0 / totalBytes;
                }

                return new DiskMetrics(
                        roundToOneDecimal(usagePercent),
                        usedBytes,
                        totalBytes
                );
            }
        }

        return new DiskMetrics(
                0,
                0,
                0
        );
    }

    private double roundToOneDecimal(
            double value
    ) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record DiskMetrics(
            double usagePercent,
            long usedBytes,
            long totalBytes
    ) {
    }

    public record SystemMetrics(
            double cpuUsagePercent,
            long usedMemoryBytes,
            long totalMemoryBytes,
            String osFamily,
            String osVersion,
            double diskUsagePercent,
            long usedDiskBytes,
            long totalDiskBytes,
            long uptimeSeconds
    ) {
    }
}
