package io.github.iroshperera.linuvera.system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class LinuxServiceStatusService {

    private static final List<ServiceDefinition> SERVICES = List.of(
            new ServiceDefinition(
                    "Nginx",
                    "nginx.service"
            ),
            new ServiceDefinition(
                    "PostgreSQL",
                    "postgresql@16-main.service"
            ),
            new ServiceDefinition(
                    "Docker",
                    "docker.service"
            ),
            new ServiceDefinition(
                    "Redis",
                    "redis-server.service"
            )
    );

    public List<ServiceStatus> collectStatuses() {
        return SERVICES.stream()
                .map(this::readStatus)
                .toList();
    }

    private ServiceStatus readStatus(
            ServiceDefinition service
    ) {
        try {
            Process process = new ProcessBuilder(
                    "systemctl",
                    "is-active",
                    service.unitName()
            )
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(
                    2,
                    TimeUnit.SECONDS
            );

            if (!finished) {
                process.destroyForcibly();

                return new ServiceStatus(
                        service.displayName(),
                        service.unitName(),
                        "Unavailable",
                        false
                );
            }

            String rawStatus = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim();

            boolean running =
                    process.exitValue() == 0
                            && "active".equals(rawStatus);

            return new ServiceStatus(
                    service.displayName(),
                    service.unitName(),
                    toDisplayStatus(rawStatus),
                    running
            );

        } catch (IOException exception) {
            return new ServiceStatus(
                    service.displayName(),
                    service.unitName(),
                    "Unavailable",
                    false
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            return new ServiceStatus(
                    service.displayName(),
                    service.unitName(),
                    "Interrupted",
                    false
            );
        }
    }

    private String toDisplayStatus(
            String rawStatus
    ) {
        return switch (rawStatus) {
            case "active" ->
                    "Running";

            case "inactive" ->
                    "Stopped";

            case "failed" ->
                    "Failed";

            case "unknown", "not-found", "" ->
                    "Not detected";

            default ->
                    rawStatus;
        };
    }

    private record ServiceDefinition(
            String displayName,
            String unitName
    ) {
    }

    public record ServiceStatus(
            String displayName,
            String unitName,
            String status,
            boolean running
    ) {
    }
}
