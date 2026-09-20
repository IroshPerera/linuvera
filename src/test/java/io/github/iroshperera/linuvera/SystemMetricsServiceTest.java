package io.github.iroshperera.linuvera;

import io.github.iroshperera.linuvera.system.SystemMetricsService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemMetricsServiceTest {

    private final SystemMetricsService service =
            new SystemMetricsService();

    @Test
    void shouldCollectValidSystemMetrics() {

        var metrics = service.collectMetrics();

        assertTrue(metrics.cpuUsagePercent() >= 0);
        assertTrue(metrics.cpuUsagePercent() <= 100);

        assertTrue(metrics.usedMemoryBytes() >= 0);
        assertTrue(metrics.totalMemoryBytes() > 0);
        assertTrue(
                metrics.usedMemoryBytes()
                        <= metrics.totalMemoryBytes()
        );

        assertFalse(metrics.osFamily().isBlank());
        assertFalse(metrics.osVersion().isBlank());

        assertTrue(metrics.diskUsagePercent() >= 0);
        assertTrue(metrics.diskUsagePercent() <= 100);

        assertTrue(metrics.usedDiskBytes() >= 0);
        assertTrue(metrics.totalDiskBytes() > 0);

        assertTrue(
                metrics.usedDiskBytes()
                        <= metrics.totalDiskBytes()
        );

        assertTrue(metrics.uptimeSeconds() >= 0);
    }
}
