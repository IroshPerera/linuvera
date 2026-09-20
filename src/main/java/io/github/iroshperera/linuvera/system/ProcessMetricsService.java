package io.github.iroshperera.linuvera.system;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import java.util.Comparator;

import java.util.List;

public final class ProcessMetricsService {

    private static final int MAX_PROCESS_COUNT = 100;

    private final OperatingSystem operatingSystem;

    public ProcessMetricsService() {
        SystemInfo systemInfo =
                new SystemInfo();

        this.operatingSystem =
                systemInfo.getOperatingSystem();
    }

    public List<ProcessInfo> collectProcesses() {
        return operatingSystem
                .getProcesses(
                        process -> true,
                        Comparator.comparingDouble(
                                OSProcess::getProcessCpuLoadCumulative
                        ).reversed(),
                        MAX_PROCESS_COUNT
                )
                .stream()
                .map(this::toProcessInfo)
                .toList();
    }

    private ProcessInfo toProcessInfo(
            OSProcess process
    ) {
        double cpuUsagePercent =
                Math.max(
                        0,
                        process.getProcessCpuLoadCumulative()
                                * 100.0
                );

        String processName =
                safeText(
                        process.getName(),
                        "Unknown"
                );

        String user =
                safeText(
                        process.getUser(),
                        "Unknown"
                );

        String state =
                process.getState() == null
                        ? "Unknown"
                        : process.getState().name();

        return new ProcessInfo(
                process.getProcessID(),
                processName,
                roundToOneDecimal(
                        cpuUsagePercent
                ),
                process.getResidentMemory(),
                user,
                state
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

    public record ProcessInfo(
            int processId,
            String name,
            double cpuUsagePercent,
            long memoryBytes,
            String user,
            String state
    ) {
    }
}
