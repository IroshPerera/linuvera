# Contributing to Linuvera

Thank you for your interest in contributing to Linuvera.

## Development Setup

Requirements:

- Java 21 or later
- Maven 3.8 or later
- Ubuntu or Debian-based Linux distribution

Clone the repository:

```bash
git clone https://github.com/IroshPerera/linuvera.git
cd linuvera
```

Run tests:

```bash
mvn clean test
```

Run the application:

```bash
mvn clean javafx:run
```

## Contribution Workflow

1. Create a new branch from `main`.

   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes.
3. Add or update tests.
4. Run the test suite.

   ```bash
   mvn clean test
   ```

5. Commit your changes with a clear message.

   ```bash
   git commit -m "feat: describe your change"
   ```

6. Push your branch and open a Pull Request.

## Commit Message Examples

```text
feat: add process filtering
fix: handle missing system command
test: add storage metrics tests
docs: update installation guide
chore: update build configuration
```

## Pull Request Guidelines

- Keep changes focused and easy to review.
- Include tests for new behaviour.
- Do not commit generated files such as `target/`.
- Explain the purpose of the change in the Pull Request description.
- Make sure GitHub Actions checks pass.

## Code Style

- Use clear class and method names.
- Keep responsibilities separated between UI and system services.
- Prefer readable code over unnecessary complexity.
- Handle unavailable Linux commands safely.
- Keep user-facing errors understandable.

## Reporting Issues

When reporting an issue, include:

- Ubuntu or Linux distribution and version
- Java version
- Steps to reproduce the issue
- Expected behaviour
- Actual behaviour
- Relevant error messages or screenshots