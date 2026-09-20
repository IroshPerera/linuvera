package io.github.iroshperera.linuvera;

import io.github.iroshperera.linuvera.system.EnvironmentDetectionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnvironmentDetectionServiceTest {

    private final EnvironmentDetectionService service =
            new EnvironmentDetectionService();

    @Test
    void shouldReturnAllSupportedTools() {

        var tools = service.detectTools();

        assertEquals(7, tools.size());

        List<String> expectedNames = List.of(
                "Java",
                "Maven",
                "Git",
                "Node.js",
                "Docker",
                "PostgreSQL",
                "Redis"
        );

        assertEquals(
                expectedNames,
                tools.stream()
                        .map(EnvironmentDetectionService.DetectedTool::name)
                        .toList()
        );
    }

    @Test
    void everyToolShouldHaveCompleteResultData() {

        var tools = service.detectTools();

        for (var tool : tools) {
            assertFalse(tool.name().isBlank());
            assertFalse(tool.command().isBlank());
            assertNotNull(tool.version());
        }
    }
}
