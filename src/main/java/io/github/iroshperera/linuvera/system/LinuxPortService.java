package io.github.iroshperera.linuvera.system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LinuxPortService {

    private static final Pattern PROCESS_PATTERN =
            Pattern.compile(
                    "users:\\(\\(\"([^\"]+)\",pid=(\\d+)"
            );

    public List<PortInfo> collectPorts() {

        try {
            Process process = new ProcessBuilder(
                    "ss",
                    "-H",
                    "-tulpn"
            )
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(
                    3,
                    TimeUnit.SECONDS
            );

            if (!finished) {
                process.destroyForcibly();

                throw new IllegalStateException(
                        "Port scan timed out."
                );
            }

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            if (process.exitValue() != 0) {
                throw new IllegalStateException(
                        "Unable to read Linux ports."
                );
            }

            List<PortInfo> ports = new ArrayList<>();

            for (String line : output.lines().toList()) {
                PortInfo portInfo = parseLine(line);

                if (portInfo != null) {
                    ports.add(portInfo);
                }
            }

            ports.sort(
                    Comparator.comparingInt(PortInfo::port)
            );

            return ports;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "The ss command is not available.",
                    exception
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Port scan was interrupted.",
                    exception
            );
        }
    }

    private PortInfo parseLine(String line) {

        if (line == null || line.isBlank()) {
            return null;
        }

        String[] columns = line.trim().split(
                "\\s+",
                7
        );

        if (columns.length < 6) {
            return null;
        }

        String protocol = columns[0];
        String state = columns[1];
        String localEndpoint = columns[4];

        Endpoint endpoint =
                parseEndpoint(localEndpoint);

        if (endpoint == null) {
            return null;
        }

        String processName = "Unknown";
        long processId = 0;

        if (columns.length >= 7) {
            Matcher matcher =
                    PROCESS_PATTERN.matcher(columns[6]);

            if (matcher.find()) {
                processName = matcher.group(1);
                processId = Long.parseLong(
                        matcher.group(2)
                );
            }
        }

        return new PortInfo(
                protocol,
                state,
                endpoint.address(),
                endpoint.port(),
                processName,
                processId
        );
    }

    private Endpoint parseEndpoint(
            String endpoint
    ) {
        int separatorIndex =
                endpoint.lastIndexOf(':');

        if (separatorIndex < 0) {
            return null;
        }

        String address = endpoint.substring(
                0,
                separatorIndex
        );

        String portText = endpoint.substring(
                separatorIndex + 1
        );

        try {
            int port = Integer.parseInt(portText);

            if (address.startsWith("[")
                    && address.endsWith("]")) {
                address = address.substring(
                        1,
                        address.length() - 1
                );
            }

            return new Endpoint(
                    address,
                    port
            );

        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record Endpoint(
            String address,
            int port
    ) {
    }

    public record PortInfo(
            String protocol,
            String state,
            String localAddress,
            int port,
            String processName,
            long processId
    ) {
    }
}
