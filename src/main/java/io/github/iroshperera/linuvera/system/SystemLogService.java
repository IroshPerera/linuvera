package io.github.iroshperera.linuvera.system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SystemLogService {

    private static final List<String> COMMAND = List.of(
            "journalctl",
            "--no-pager",
            "--output=short-iso",
            "--lines=200"
    );

    public String collectRecentLogs() {

        try {
            Process process =
                    new ProcessBuilder(COMMAND)
                            .redirectErrorStream(true)
                            .start();

            boolean finished =
                    process.waitFor(
                            4,
                            TimeUnit.SECONDS
                    );

            if (!finished) {
                process.destroyForcibly();

                return "Log collection timed out.";
            }

            String output =
                    new String(
                            process.getInputStream()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    ).strip();

            if (process.exitValue() != 0) {
                return "Unable to read system logs.\n\n"
                        + output;
            }

            if (output.isBlank()) {
                return "No recent system log entries found.";
            }

            return output;

        } catch (IOException exception) {
            return "journalctl is not available on this system.";

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            return "Log collection was interrupted.";
        }
    }
}
