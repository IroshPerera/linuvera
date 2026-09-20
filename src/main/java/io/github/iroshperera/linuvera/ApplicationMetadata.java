package io.github.iroshperera.linuvera;

public final class ApplicationMetadata {

    private static final String APPLICATION_NAME = "Linuvera";
    private static final String APPLICATION_VERSION = "0.1.0-SNAPSHOT";

    private ApplicationMetadata() {
        // Utility class cannot be instantiated.
    }

    public static String getApplicationName() {
        return APPLICATION_NAME;
    }

    public static String getApplicationVersion() {
        return APPLICATION_VERSION;
    }
}
