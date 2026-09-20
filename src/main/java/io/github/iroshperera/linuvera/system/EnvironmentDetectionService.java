package io.github.iroshperera.linuvera.system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class EnvironmentDetectionService {

    private static final List<ToolDefinition> TOOLS = List.of(
            new ToolDefinition(
                    "Java",
                    List.of("java", "--version")
            ),
            new ToolDefinition(
                    "Maven",
                    List.of("mvn", "--version")
            ),
            new ToolDefinition(
                    "Git",
                    List.of("git", "--version")
            ),
            new ToolDefinition(
                    "Node.js",
                    List.of("node", "--version")
            ),
            new ToolDefinition(
                    "Docker",
                    List.of("docker", "--version")
            ),
            new ToolDefinition(
                    "PostgreSQL",
                    List.of("psql", "--version")
            ),
            new ToolDefinition(
                    "Redis",
                    List.of("redis-server", "--version")
            )
    );

    public List<DetectedTool> detectTools() {
        return TOOLS.stream()
                .map(this::detectTool)
                .toList();
    }

    private DetectedTool detectTool(
            ToolDefinition tool
    ) {
        try {
            Process process = new ProcessBuilder(
                    tool.command()
            )
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(
                    3,
                    TimeUnit.SECONDS
            );

            if (!finished) {
                process.destroyForcibly();

                return new DetectedTool(
                        tool.name(),
                        commandText(tool),
                        "Timeout",
                        false
                );
            }

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            if (process.exitValue() != 0) {
                return new DetectedTool(
                        tool.name(),
                        commandText(tool),
                        "Not detected",
                        false
                );
            }

            String version = output.lines()
                    .map(this::stripAnsiCodes)
                    .filter(line -> !line.isBlank())
                    .findFirst()
                    .orElse("Detected")
                    .trim();

            version = formatVersion(tool.name(), version);

            return new DetectedTool(
                    tool.name(),
                    commandText(tool),
                    version,
                    true
            );

        } catch (IOException exception) {
            return new DetectedTool(
                    tool.name(),
                    commandText(tool),
                    "Not installed",
                    false
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            return new DetectedTool(
                    tool.name(),
                    commandText(tool),
                    "Interrupted",
                    false
            );
        }
    }

    private String formatVersion(
            String toolName,
            String version
    ) {
        if ("Java".equals(toolName)
                && version.startsWith("openjdk ")) {

            String[] parts = version.split("\\s+");

            if (parts.length > 1) {
                return "OpenJDK " + parts[1];
            }
        }

        if ("Docker".equals(toolName)
                && version.startsWith("Docker version ")) {

            int versionStart = "Docker version ".length();

            int versionEnd = version.indexOf(
                    ',',
                    versionStart
            );

            String dockerVersion = versionEnd == -1
                    ? version.substring(versionStart)
                    : version.substring(
                    versionStart,
                    versionEnd
            );

            return "Docker " + dockerVersion;
        }

        if ("PostgreSQL".equals(toolName)
                && version.startsWith("psql (PostgreSQL) ")) {

            int versionStart = "psql (PostgreSQL) ".length();

            int versionEnd = version.indexOf(
                    ' ',
                    versionStart
            );

            String postgresqlVersion = versionEnd == -1
                    ? version.substring(versionStart)
                    : version.substring(
                    versionStart,
                    versionEnd
            );

            return "PostgreSQL " + postgresqlVersion;
        }

        if ("Redis".equals(toolName)
                && version.startsWith("Redis server v=")) {

            int versionStart = "Redis server v=".length();

            int versionEnd = version.indexOf(
                    ' ',
                    versionStart
            );

            String redisVersion = versionEnd == -1
                    ? version.substring(versionStart)
                    : version.substring(
                    versionStart,
                    versionEnd
            );

            return "Redis " + redisVersion;
        }

        return version;
    }

    private String stripAnsiCodes(String value) {
        return value
                .replaceAll("\u001B\\[[;\\d]*m", "")
                .trim();
    }

    private String commandText(
            ToolDefinition tool
    ) {
        return String.join(
                " ",
                tool.command()
        );
    }

    private record ToolDefinition(
            String name,
            List<String> command
    ) {
    }

    public record DetectedTool(
            String name,
            String command,
            String version,
            boolean detected
    ) {
    }
}
