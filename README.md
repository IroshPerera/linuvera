# Linuvera

Linuvera is an open-source Linux developer toolkit built with JavaFX.

It helps Linux developers monitor and inspect their local development environment through a simple desktop application.

## Features

- System overview dashboard
- Live CPU and memory monitoring
- Disk usage monitoring
- System uptime information
- System health charts
- Running process monitoring
- Process search
- Mounted filesystem information
- Developer tool detection
- Linux service monitoring
- Active TCP and UDP port inspection
- Recent system logs viewer
- Log search and log-level filtering
- Automatic data refresh
- Manual page refresh

## Tech Stack

- Java 21
- JavaFX 21
- Maven
- OSHI
- JUnit 5
- Linux system commands

## Requirements

- Ubuntu or Debian-based Linux distribution
- Java 21 or later
- Maven 3.8 or later
- `journalctl` for system log viewing

## Run the Application

```bash
mvn clean javafx:run
```

## Run Tests

```bash
mvn clean test
```

## Project Structure

```text
src/main/java
├── ui
├── system
└── ApplicationMetadata.java
```

## Project Status

Linuvera is currently under active development.

The current version focuses on read-only Linux system monitoring and developer environment inspection.

## Open Source

Contributions, suggestions, and bug reports are welcome.

## License

This project will be released under an open-source license.