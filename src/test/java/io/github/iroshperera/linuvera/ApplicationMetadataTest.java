package io.github.iroshperera.linuvera;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationMetadataTest {

    @Test
    void applicationNameShouldBeLinuvera() {
        String applicationName =
                ApplicationMetadata.getApplicationName();

        assertNotNull(applicationName);
        assertEquals("Linuvera", applicationName);
    }

    @Test
    void applicationVersionShouldBeDevelopmentVersion() {
        String applicationVersion =
                ApplicationMetadata.getApplicationVersion();

        assertNotNull(applicationVersion);
        assertEquals("0.1.2", applicationVersion);
    }
}
