package io.github.iroshperera.linuvera.system;

import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.List;

public final class StorageMetricsService {

    private final FileSystem fileSystem;

    public StorageMetricsService() {
        SystemInfo systemInfo =
                new SystemInfo();

        OperatingSystem operatingSystem =
                systemInfo.getOperatingSystem();

        this.fileSystem =
                operatingSystem.getFileSystem();
    }

    public List<StorageInfo> collectStorage() {
        return fileSystem
                .getFileStores()
                .stream()
                .filter(
                        fileStore ->
                                fileStore.getTotalSpace() > 0
                )
                .map(this::toStorageInfo)
                .toList();
    }

    private StorageInfo toStorageInfo(
            OSFileStore fileStore
    ) {
        long totalBytes =
                fileStore.getTotalSpace();

        long freeBytes =
                Math.max(
                        0,
                        fileStore.getUsableSpace()
                );

        long usedBytes =
                Math.max(
                        0,
                        totalBytes - freeBytes
                );

        double usagePercent = 0;

        if (totalBytes > 0) {
            usagePercent =
                    usedBytes * 100.0
                            / totalBytes;
        }

        return new StorageInfo(
                safeText(
                        fileStore.getName(),
                        "Unknown"
                ),
                safeText(
                        fileStore.getMount(),
                        "Unknown"
                ),
                safeText(
                        fileStore.getType(),
                        "Unknown"
                ),
                totalBytes,
                usedBytes,
                freeBytes,
                roundToOneDecimal(
                        usagePercent
                )
        );
    }

    private String safeText(
            String value,
            String fallback
    ) {
        if (value == null
                || value.isBlank()) {
            return fallback;
        }

        return value;
    }

    private double roundToOneDecimal(
            double value
    ) {
        return Math.round(value * 10.0)
                / 10.0;
    }

    public record StorageInfo(
            String name,
            String mountPoint,
            String fileSystemType,
            long totalBytes,
            long usedBytes,
            long freeBytes,
            double usagePercent
    ) {
    }
}
