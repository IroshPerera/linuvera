package io.github.iroshperera.linuvera.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class MainView extends BorderPane {

    private final StackPane pageContainer = new StackPane();
    private Button activeButton;

    public MainView() {
        getStyleClass().add("app-shell");

        pageContainer.setAlignment(Pos.TOP_LEFT);
        pageContainer.getStyleClass().add("page-container");

        setLeft(createSidebar());
        setCenter(createMainArea());
    }

    private VBox createSidebar() {

        Label brand = new Label("Linuvera");
        brand.getStyleClass().add("brand-title");

        Label tagline = new Label("Linux developer toolkit");
        tagline.getStyleClass().add("brand-tagline");

        VBox brandSection = new VBox(
                4,
                brand,
                tagline
        );

        Label overviewLabel = new Label("OVERVIEW");
        overviewLabel.getStyleClass().add("navigation-section");

        VBox overviewNavigation = new VBox(
                6,
                createNavigationButton("Dashboard", true)
        );

        Label systemLabel = new Label("SYSTEM");
        systemLabel.getStyleClass().add("navigation-section");

        VBox systemNavigation = new VBox(
                6,
                createNavigationButton("System Health", false),
                createNavigationButton("Processes", false),
                createNavigationButton("Storage", false)
        );

        Label developerLabel = new Label("DEVELOPER");
        developerLabel.getStyleClass().add("navigation-section");

        VBox developerNavigation = new VBox(
                6,
                createNavigationButton("Environment", false),
                createNavigationButton("Services", false),
                createNavigationButton("Ports", false),
                createNavigationButton("Logs", false)
        );

        VBox navigation = new VBox(
                14,
                overviewLabel,
                overviewNavigation,
                systemLabel,
                systemNavigation,
                developerLabel,
                developerNavigation
        );

        VBox.setVgrow(navigation, Priority.ALWAYS);

        Label version = new Label("v0.1.0-SNAPSHOT");
        version.getStyleClass().add("sidebar-version");

        VBox sidebar = new VBox(
                30,
                brandSection,
                navigation,
                version
        );

        sidebar.setPadding(
                new Insets(28, 18, 22, 18)
        );

        sidebar.setPrefWidth(250);
        sidebar.getStyleClass().add("sidebar");

        return sidebar;
    }

    private Button createNavigationButton(
            String text,
            boolean active
    ) {
        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(
                new Insets(11, 14, 11, 14)
        );

        button.getStyleClass().add("navigation-button");

        if (active) {
            button.getStyleClass()
                    .add("navigation-button-active");

            activeButton = button;
        }

        button.setOnAction(event -> {

            if (activeButton != null) {
                activeButton.getStyleClass()
                        .remove("navigation-button-active");
            }

            button.getStyleClass()
                    .add("navigation-button-active");

            activeButton = button;

            showPage(text);
        });

        return button;
    }

    private BorderPane createMainArea() {

        BorderPane mainArea = new BorderPane();

        mainArea.setTop(createTopBar());

        pageContainer.getChildren()
                .add(createDashboardContent());

        mainArea.setCenter(pageContainer);
        mainArea.getStyleClass().add("main-area");

        return mainArea;
    }

    private HBox createTopBar() {

        Label pageTitle = new Label("System Overview");
        pageTitle.getStyleClass().add("page-title");

        Label pageSubtitle = new Label(
                "Monitor and manage your Linux development environment."
        );

        pageSubtitle.getStyleClass()
                .add("page-subtitle");

        VBox pageInformation = new VBox(
                5,
                pageTitle,
                pageSubtitle
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("refresh-button");

        HBox topBar = new HBox(
                20,
                pageInformation,
                spacer,
                refreshButton
        );

        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(
                new Insets(26, 34, 26, 34)
        );

        topBar.getStyleClass().add("top-bar");

        return topBar;
    }

    private void showPage(String pageName) {

        if ("Dashboard".equals(pageName)) {
            pageContainer.getChildren()
                    .setAll(createDashboardContent());

            return;
        }

        String description = switch (pageName) {
            case "System Health" ->
                    "View CPU, memory, disk, battery, and system health information.";

            case "Processes" ->
                    "Inspect running processes and resource usage.";

            case "Storage" ->
                    "Analyze disks, mount points, and available storage.";

            case "Environment" ->
                    "Check Java, Maven, Git, Docker, Node.js, and other tools.";

            case "Services" ->
                    "Monitor Linux services such as Nginx, PostgreSQL, and Docker.";

            case "Ports" ->
                    "Inspect active ports and the processes using them.";

            case "Logs" ->
                    "Read and filter system and application logs.";

            default ->
                    "This Linuvera module will be implemented soon.";
        };

        Label title = new Label(pageName);
        title.getStyleClass().add("page-title");

        Label message = new Label(description);
        message.getStyleClass().add("page-subtitle");

        VBox placeholder = new VBox(
                12,
                title,
                message
        );

        placeholder.setPadding(
                new Insets(48, 34, 34, 34)
        );

        placeholder.setAlignment(Pos.TOP_LEFT);
        placeholder.getStyleClass().add("dashboard-content");

        pageContainer.getChildren()
                .setAll(placeholder);
    }

    private ScrollPane createDashboardContent() {

        Label sectionTitle = new Label("System Metrics");
        sectionTitle.getStyleClass().add("section-title");

        GridPane metricGrid = createMetricGrid();

        Label servicesTitle = new Label("Service Health");
        servicesTitle.getStyleClass().add("section-title");

        HBox servicesCard = createServicesCard();

        VBox content = new VBox(
                18,
                sectionTitle,
                metricGrid,
                servicesTitle,
                servicesCard
        );

        content.setPadding(
                new Insets(0, 34, 34, 34)
        );

        content.getStyleClass().add("dashboard-content");

        ScrollPane scrollPane = new ScrollPane(content);

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.getStyleClass()
                .add("dashboard-scroll");

        return scrollPane;
    }

    private GridPane createMetricGrid() {

        GridPane grid = new GridPane();

        grid.setHgap(16);
        grid.setVgap(16);

        VBox cpuCard = createMetricCard(
                "CPU Usage",
                "32%",
                "Healthy",
                "metric-card-blue"
        );

        VBox memoryCard = createMetricCard(
                "Memory Usage",
                "61%",
                "Normal",
                "metric-card-purple"
        );

        VBox diskCard = createMetricCard(
                "Disk Usage",
                "48%",
                "Healthy",
                "metric-card-green"
        );

        VBox uptimeCard = createMetricCard(
                "System Uptime",
                "3d 7h",
                "Running",
                "metric-card-orange"
        );

        grid.add(cpuCard, 0, 0);
        grid.add(memoryCard, 1, 0);
        grid.add(diskCard, 2, 0);
        grid.add(uptimeCard, 3, 0);

        for (int index = 0; index < 4; index++) {
            ColumnConstraints constraints =
                    new ColumnConstraints();

            constraints.setHgrow(Priority.ALWAYS);
            constraints.setFillWidth(true);

            grid.getColumnConstraints()
                    .add(constraints);
        }

        return grid;
    }

    private VBox createMetricCard(
            String title,
            String value,
            String status,
            String colorClass
    ) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");

        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("metric-status");

        VBox card = new VBox(
                14,
                titleLabel,
                valueLabel,
                statusLabel
        );

        card.getStyleClass().addAll(
                "metric-card",
                colorClass
        );

        return card;
    }

    private HBox createServicesCard() {

        VBox serviceRows = new VBox(
                12,
                createServiceRow("Nginx", "Running", true),
                createServiceRow("PostgreSQL", "Running", true),
                createServiceRow("Docker", "Not detected", false),
                createServiceRow("Redis", "Not detected", false)
        );

        HBox card = new HBox(serviceRows);

        card.setPadding(
                new Insets(22)
        );

        card.getStyleClass().add("services-card");

        return card;
    }

    private HBox createServiceRow(
            String serviceName,
            String serviceStatus,
            boolean healthy
    ) {
        Label indicator = new Label();
        indicator.getStyleClass().add("status-indicator");

        if (healthy) {
            indicator.getStyleClass()
                    .add("status-indicator-good");
        } else {
            indicator.getStyleClass()
                    .add("status-indicator-muted");
        }

        Label name = new Label(serviceName);
        name.getStyleClass().add("service-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(serviceStatus);

        if (healthy) {
            status.getStyleClass()
                    .add("service-status-good");
        } else {
            status.getStyleClass()
                    .add("service-status-muted");
        }

        HBox row = new HBox(
                10,
                indicator,
                name,
                spacer,
                status
        );

        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }
}