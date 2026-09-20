package io.github.iroshperera.linuvera.ui;

import io.github.iroshperera.linuvera.system.SystemMetricsService;
import io.github.iroshperera.linuvera.system.SystemMetricsService.SystemMetrics;
import io.github.iroshperera.linuvera.system.LinuxServiceStatusService;
import io.github.iroshperera.linuvera.system.LinuxPortService;
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
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import io.github.iroshperera.linuvera.system.EnvironmentDetectionService;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.List;

import java.util.Locale;

public final class MainView extends BorderPane {

    private final StackPane pageContainer = new StackPane();
    private final SystemMetricsService metricsService =
            new SystemMetricsService();

    private Button activeButton;

    private final Timeline metricsRefreshTimer = new Timeline(
            new KeyFrame(
                    Duration.seconds(3),
                    event -> refreshAutoData()
            )
    );

    private Label cpuValueLabel;
    private Label cpuStatusLabel;

    private Label memoryValueLabel;
    private Label memoryStatusLabel;

    private Label diskValueLabel;
    private Label diskStatusLabel;

    private Label uptimeValueLabel;
    private Label uptimeStatusLabel;

    private final LinuxServiceStatusService serviceStatusService =
            new LinuxServiceStatusService();

    private VBox serviceRows;

    private final LinuxPortService portService =
            new LinuxPortService();

    private String currentPage = "Dashboard";

    private Label pageTitleLabel;
    private Label pageSubtitleLabel;

    private TableView<LinuxPortService.PortInfo> portsTable;

    private Label totalEndpointsValueLabel;
    private Label tcpEndpointsValueLabel;
    private Label udpEndpointsValueLabel;
    private Label uniquePortsValueLabel;

    private final ObservableList<LinuxPortService.PortInfo> allPorts =
            FXCollections.observableArrayList();

    private FilteredList<LinuxPortService.PortInfo> filteredPorts;

    private TextField portSearchField;
    private ComboBox<String> protocolFilter;

    private final EnvironmentDetectionService
            environmentDetectionService =
            new EnvironmentDetectionService();

    private static final int CHART_HISTORY_LIMIT = 30;

    private final XYChart.Series<Number, Number>
            cpuChartSeries = new XYChart.Series<>();

    private final XYChart.Series<Number, Number>
            memoryChartSeries = new XYChart.Series<>();

    private final XYChart.Series<Number, Number>
            diskChartSeries = new XYChart.Series<>();

    private int chartSampleIndex = 0;

    private LineChart<Number, Number> cpuChart;
    private LineChart<Number, Number> memoryChart;
    private LineChart<Number, Number> diskChart;

    private Label healthCpuValueLabel;
    private Label healthCpuStatusLabel;

    private Label healthMemoryValueLabel;
    private Label healthMemoryStatusLabel;

    private Label healthDiskValueLabel;
    private Label healthDiskStatusLabel;

    private Label healthUptimeValueLabel;
    private Label healthUptimeStatusLabel;

    private Label healthOperatingSystemValueLabel;
    private Label healthOperatingSystemStatusLabel;

    public MainView() {
        getStyleClass().add("app-shell");

        pageContainer.setAlignment(Pos.TOP_LEFT);
        pageContainer.getStyleClass().add("page-container");

        setLeft(createSidebar());
        setCenter(createMainArea());

        refreshDashboardMetrics();

        metricsRefreshTimer.setCycleCount(
                Animation.INDEFINITE
        );

        metricsRefreshTimer.play();
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

        pageTitleLabel = new Label("System Overview");
        pageTitleLabel.getStyleClass().add("page-title");

        pageSubtitleLabel = new Label(
                "Monitor and manage your Linux development environment."
        );

        pageSubtitleLabel.getStyleClass()
                .add("page-subtitle");

        VBox pageInformation = new VBox(
                5,
                pageTitleLabel,
                pageSubtitleLabel
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("refresh-button");

        refreshButton.setOnAction(event ->
                refreshCurrentPage()
        );

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

    private void refreshCurrentPage() {

        switch (currentPage) {
            case "Dashboard" -> {
                refreshDashboardMetrics();
                refreshServiceRows();
            }

            case "Services" ->
                    refreshServiceRows();

            case "Ports" ->
                    refreshPortsData();

            case "Environment" ->
                    pageContainer.getChildren()
                            .setAll(createEnvironmentPage());

            default ->
                    showPage(currentPage);
        }
    }
    private void refreshAutoData() {

        switch (currentPage) {
            case "Dashboard" ->
                    refreshDashboardMetrics();

            case "Ports" ->
                    refreshPortsData();

            case "System Health" ->
                    refreshSystemHealthData();

            default -> {
                // No automatic refresh required.
            }
        }
    }



    private void updateTopBar(String pageName) {

        if (pageTitleLabel == null
                || pageSubtitleLabel == null) {
            return;
        }

        switch (pageName) {
            case "Dashboard" -> {
                pageTitleLabel.setText("System Overview");
                pageSubtitleLabel.setText(
                        "Monitor and manage your Linux development environment."
                );
            }

            case "Services" -> {
                pageTitleLabel.setText("Linux Services");
                pageSubtitleLabel.setText(
                        "Monitor systemd services running on this machine."
                );
            }

            case "Ports" -> {
                pageTitleLabel.setText("Listening Ports");
                pageSubtitleLabel.setText(
                        "Inspect active TCP and UDP ports."
                );
            }

            case "System Health" -> {
                pageTitleLabel.setText("System Health");
                pageSubtitleLabel.setText(
                        "Review the health of your Linux system."
                );
            }

            default -> {
                pageTitleLabel.setText(pageName);
                pageSubtitleLabel.setText(
                        "Linuvera Linux developer toolkit."
                );
            }
        }
    }

    private void showPage(String pageName) {

        currentPage = pageName;
        updateTopBar(pageName);

        if ("System Health".equals(pageName)) {
            pageContainer.getChildren()
                    .setAll(createSystemHealthPage());

            return;
        }

        if ("Environment".equals(pageName)) {
            pageContainer.getChildren()
                    .setAll(createEnvironmentPage());

            return;
        }

        if ("Services".equals(pageName)) {
            pageContainer.getChildren()
                    .setAll(createServicesPage());

            return;
        }

        if ("Ports".equals(pageName)) {
            pageContainer.getChildren()
                    .setAll(createPortsPage());

            return;
        }

        if ("Dashboard".equals(pageName)) {
            pageContainer.getChildren()
                    .setAll(createDashboardContent());

            refreshDashboardMetrics();

            return;
        }

        String description = switch (pageName) {
            case "System Health" -> "View CPU, memory, disk, battery, and system health information.";

            case "Processes" -> "Inspect running processes and resource usage.";

            case "Storage" -> "Analyze disks, mount points, and available storage.";

            case "Environment" -> "Check Java, Maven, Git, Docker, Node.js, and other tools.";

            case "Services" -> "Monitor Linux services such as Nginx, PostgreSQL, and Docker.";

            case "Ports" -> "Inspect active ports and the processes using them.";

            case "Logs" -> "Read and filter system and application logs.";

            default -> "This Linuvera module will be implemented soon.";
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

    private ScrollPane createSystemHealthPage() {

        SystemMetricsService.SystemMetrics metrics =
                metricsService.collectMetrics();

        Label sectionTitle =
                new Label("System Health Summary");

        sectionTitle.getStyleClass()
                .add("section-title");

        Label sectionSubtitle =
                new Label(
                        "Live health information from this Linux machine."
                );

        sectionSubtitle.getStyleClass()
                .add("page-subtitle");

        VBox heading = new VBox(
                6,
                sectionTitle,
                sectionSubtitle
        );

        double memoryUsagePercent = 0;

        if (metrics.totalMemoryBytes() > 0) {
            memoryUsagePercent =
                    metrics.usedMemoryBytes()
                            * 100.0
                            / metrics.totalMemoryBytes();
        }

        updateHealthChartSeries(metrics);

        GridPane metricGrid = new GridPane();

        metricGrid.setHgap(16);
        metricGrid.setVgap(16);

        metricGrid.add(
                createHealthMetricCard(
                        "CPU Usage",
                        String.format(
                                java.util.Locale.ROOT,
                                "%.1f%%",
                                metrics.cpuUsagePercent()
                        ),
                        metrics.cpuUsagePercent() < 80
                                ? "Healthy"
                                : "High usage",
                        "metric-card-blue"
                ),
                0,
                0
        );

        metricGrid.add(
                createHealthMetricCard(
                        "Memory Usage",
                        formatHealthBytes(
                                metrics.usedMemoryBytes()
                        ),
                        String.format(
                                java.util.Locale.ROOT,
                                "%.1f%% of %s",
                                memoryUsagePercent,
                                formatHealthBytes(
                                        metrics.totalMemoryBytes()
                                )
                        ),
                        "metric-card-purple"
                ),
                1,
                0
        );

        metricGrid.add(
                createHealthMetricCard(
                        "Disk Usage",
                        String.format(
                                java.util.Locale.ROOT,
                                "%.1f%%",
                                metrics.diskUsagePercent()
                        ),
                        formatHealthBytes(
                                metrics.usedDiskBytes()
                        )
                                + " used",
                        "metric-card-orange"
                ),
                2,
                0
        );

        metricGrid.add(
                createHealthMetricCard(
                        "System Uptime",
                        formatHealthUptime(
                                metrics.uptimeSeconds()
                        ),
                        "System running",
                        "metric-card-green"
                ),
                3,
                0
        );

        metricGrid.add(
                createHealthMetricCard(
                        "Operating System",
                        metrics.osFamily(),
                        metrics.osVersion(),
                        "metric-card-green"
                ),
                0,
                1
        );

        cpuChart =
                createMiniLineChart(cpuChartSeries);

        memoryChart =
                createMiniLineChart(memoryChartSeries);

        diskChart =
                createMiniLineChart(diskChartSeries);

        GridPane chartGrid = new GridPane();

        chartGrid.setHgap(16);
        chartGrid.setVgap(16);

        chartGrid.add(
                createHealthChartCard(
                        "CPU Usage Trend",
                        cpuChart,
                        "metric-card-blue"
                ),
                0,
                0
        );

        chartGrid.add(
                createHealthChartCard(
                        "Memory Usage Trend",
                        memoryChart,
                        "metric-card-purple"
                ),
                1,
                0
        );

        chartGrid.add(
                createHealthChartCard(
                        "Disk Usage Trend",
                        diskChart,
                        "metric-card-orange"
                ),
                2,
                0
        );

        VBox content = new VBox(
                24,
                heading,
                metricGrid,
                chartGrid
        );

        content.setPadding(
                new Insets(34)
        );

        content.getStyleClass()
                .add("dashboard-content");

        ScrollPane scrollPane =
                new ScrollPane(content);

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.getStyleClass()
                .add("dashboard-scroll");

        return scrollPane;
    }

    private VBox createHealthMetricCard(
            String title,
            String value,
            String status,
            String colorClass
    ) {
        Label titleLabel =
                new Label(title);

        titleLabel.getStyleClass()
                .add("metric-title");

        Label valueLabel =
                new Label(value);

        valueLabel.getStyleClass()
                .add("metric-value");

        valueLabel.setWrapText(true);

        Label statusLabel =
                new Label(status);

        statusLabel.getStyleClass()
                .add("metric-status");

        VBox card = new VBox(
                12,
                titleLabel,
                valueLabel,
                statusLabel
        );

        card.getStyleClass()
                .addAll(
                        "metric-card",
                        colorClass
                );

        switch (title) {
            case "CPU Usage" -> {
                healthCpuValueLabel = valueLabel;
                healthCpuStatusLabel = statusLabel;
            }

            case "Memory Usage" -> {
                healthMemoryValueLabel = valueLabel;
                healthMemoryStatusLabel = statusLabel;
            }

            case "Disk Usage" -> {
                healthDiskValueLabel = valueLabel;
                healthDiskStatusLabel = statusLabel;
            }

            case "System Uptime" -> {
                healthUptimeValueLabel = valueLabel;
                healthUptimeStatusLabel = statusLabel;
            }

            case "Operating System" -> {
                healthOperatingSystemValueLabel = valueLabel;
                healthOperatingSystemStatusLabel = statusLabel;
            }

            default -> {
            }
        }

        return card;
    }

    private String formatHealthBytes(
            long bytes
    ) {
        double gigabytes =
                bytes / (1024.0 * 1024.0 * 1024.0);

        return String.format(
                java.util.Locale.ROOT,
                "%.1f GB",
                gigabytes
        );
    }

    private String formatHealthUptime(
            long uptimeSeconds
    ) {
        long days =
                uptimeSeconds / 86_400;

        long hours =
                (uptimeSeconds % 86_400) / 3_600;

        long minutes =
                (uptimeSeconds % 3_600) / 60;

        return String.format(
                java.util.Locale.ROOT,
                "%dd %02dh %02dm",
                days,
                hours,
                minutes
        );
    }

    private ScrollPane createEnvironmentPage() {

        Label sectionTitle = new Label("Detected tools");
        sectionTitle.getStyleClass().add("section-title");

        Label description = new Label(
                "Check developer tools installed on this Linux machine."
        );
        description.getStyleClass().add("page-subtitle");

        VBox toolCards = new VBox(14);

        for (EnvironmentDetectionService.DetectedTool tool
                : environmentDetectionService.detectTools()) {

            toolCards.getChildren().add(
                    createToolCard(tool)
            );
        }

        VBox content = new VBox(
                12,
                sectionTitle,
                description,
                toolCards
        );

        content.setPadding(
                new Insets(34)
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

    private VBox createToolCard(
            EnvironmentDetectionService.DetectedTool tool
    ) {
        Label indicator = new Label();
        indicator.getStyleClass().add("status-indicator");

        if (tool.detected()) {
            indicator.getStyleClass()
                    .add("status-indicator-good");
        } else {
            indicator.getStyleClass()
                    .add("status-indicator-muted");
        }

        Label toolName = new Label(
                tool.name()
        );
        toolName.getStyleClass().add("service-name");

        Label command = new Label(
                tool.command()
        );
        command.getStyleClass().add("page-subtitle");

        VBox information = new VBox(
                5,
                toolName,
                command
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(
                tool.detected()
                        ? "Detected"
                        : "Not detected"
        );

        if (tool.detected()) {
            status.getStyleClass()
                    .add("service-status-good");
        } else {
            status.getStyleClass()
                    .add("service-status-muted");
        }

        Label version = new Label(
                tool.version()
        );
        version.getStyleClass().add("page-subtitle");

        VBox result = new VBox(
                4,
                status,
                version
        );

        result.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(
                12,
                indicator,
                information,
                spacer,
                result
        );

        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(
                header
        );

        card.setPadding(
                new Insets(20)
        );

        card.getStyleClass().add("services-card");

        return card;
    }

    private ScrollPane createServicesPage() {

        Label title = new Label("Linux Services");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label(
                "Monitor systemd services running on this Linux machine."
        );
        subtitle.getStyleClass().add("page-subtitle");

        VBox heading = new VBox(
                6,
                title,
                subtitle
        );

        HBox header = new HBox(
                20,
                heading
        );

        header.setAlignment(Pos.CENTER_LEFT);

        VBox serviceCards = new VBox(14);

        for (LinuxServiceStatusService.ServiceStatus serviceStatus
                : serviceStatusService.collectStatuses()) {

            serviceCards.getChildren().add(
                    createServiceDetailCard(serviceStatus)
            );
        }

        VBox content = new VBox(
                24,
                header,
                serviceCards
        );

        content.setPadding(
                new Insets(34)
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

    private VBox createServiceDetailCard(
            LinuxServiceStatusService.ServiceStatus serviceStatus
    ) {
        Label indicator = new Label();
        indicator.getStyleClass().add("status-indicator");

        if (serviceStatus.running()) {
            indicator.getStyleClass()
                    .add("status-indicator-good");
        } else {
            indicator.getStyleClass()
                    .add("status-indicator-muted");
        }

        Label serviceName = new Label(
                serviceStatus.displayName()
        );
        serviceName.getStyleClass().add("service-name");

        Label unitName = new Label(
                serviceStatus.unitName()
        );
        unitName.getStyleClass().add("page-subtitle");

        VBox information = new VBox(
                5,
                serviceName,
                unitName
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(
                serviceStatus.status()
        );

        if (serviceStatus.running()) {
            status.getStyleClass()
                    .add("service-status-good");
        } else {
            status.getStyleClass()
                    .add("service-status-muted");
        }

        HBox header = new HBox(
                12,
                indicator,
                information,
                spacer,
                status
        );

        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(
                header
        );

        card.setPadding(
                new Insets(20)
        );

        card.getStyleClass().add("services-card");

        return card;
    }

    private ScrollPane createPortsPage() {

        Label sectionTitle = new Label("Active listeners");
        sectionTitle.getStyleClass().add("section-title");

        List<LinuxPortService.PortInfo> ports =
                collectPortsSafely();

        allPorts.setAll(ports);

        filteredPorts = new FilteredList<>(
                allPorts,
                port -> true
        );

        GridPane summaryGrid =
                createPortSummaryGrid(ports);

        HBox filterBar =
                createPortFilterBar();

        portsTable =
                createPortsTable(filteredPorts);

        VBox content = new VBox(
                18,
                sectionTitle,
                summaryGrid,
                filterBar,
                portsTable
        );

        content.setPadding(
                new Insets(34)
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

    private void refreshSystemHealthData() {

        if (healthCpuValueLabel == null
                || healthMemoryValueLabel == null
                || healthDiskValueLabel == null
                || healthUptimeValueLabel == null
                || healthOperatingSystemValueLabel == null) {
            return;
        }

        SystemMetricsService.SystemMetrics metrics =
                metricsService.collectMetrics();

        double memoryUsagePercent = 0;

        if (metrics.totalMemoryBytes() > 0) {
            memoryUsagePercent =
                    metrics.usedMemoryBytes()
                            * 100.0
                            / metrics.totalMemoryBytes();
        }

        healthCpuValueLabel.setText(
                String.format(
                        java.util.Locale.ROOT,
                        "%.1f%%",
                        metrics.cpuUsagePercent()
                )
        );

        healthCpuStatusLabel.setText(
                metrics.cpuUsagePercent() < 80
                        ? "Healthy"
                        : "High usage"
        );

        healthMemoryValueLabel.setText(
                formatHealthBytes(
                        metrics.usedMemoryBytes()
                )
        );

        healthMemoryStatusLabel.setText(
                String.format(
                        java.util.Locale.ROOT,
                        "%.1f%% of %s",
                        memoryUsagePercent,
                        formatHealthBytes(
                                metrics.totalMemoryBytes()
                        )
                )
        );

        healthDiskValueLabel.setText(
                String.format(
                        java.util.Locale.ROOT,
                        "%.1f%%",
                        metrics.diskUsagePercent()
                )
        );

        healthDiskStatusLabel.setText(
                formatHealthBytes(
                        metrics.usedDiskBytes()
                ) + " used"
        );

        healthUptimeValueLabel.setText(
                formatHealthUptime(
                        metrics.uptimeSeconds()
                )
        );

        healthUptimeStatusLabel.setText(
                "System running"
        );

        healthOperatingSystemValueLabel.setText(
                metrics.osFamily()
        );

        healthOperatingSystemStatusLabel.setText(
                metrics.osVersion()
        );

        updateHealthChartSeries(metrics);
    }

    private void refreshPortsData() {

        if (portsTable == null
                || filteredPorts == null) {
            return;
        }

        List<LinuxPortService.PortInfo> ports =
                collectPortsSafely();

        allPorts.setAll(ports);

        updatePortSummaryValues(ports);
        applyPortFilter();
    }

    private void updatePortSummaryValues(
            List<LinuxPortService.PortInfo> ports
    ) {

        if (totalEndpointsValueLabel == null
                || tcpEndpointsValueLabel == null
                || udpEndpointsValueLabel == null
                || uniquePortsValueLabel == null) {
            return;
        }

        long totalEndpoints = ports.size();

        long tcpEndpoints = countPortsByProtocol(
                ports,
                "tcp"
        );

        long udpEndpoints = countPortsByProtocol(
                ports,
                "udp"
        );

        long uniquePorts = ports.stream()
                .map(LinuxPortService.PortInfo::port)
                .distinct()
                .count();

        totalEndpointsValueLabel.setText(
                String.valueOf(totalEndpoints)
        );

        tcpEndpointsValueLabel.setText(
                String.valueOf(tcpEndpoints)
        );

        udpEndpointsValueLabel.setText(
                String.valueOf(udpEndpoints)
        );

        uniquePortsValueLabel.setText(
                String.valueOf(uniquePorts)
        );
    }

    private long countPortsByProtocol(
            List<LinuxPortService.PortInfo> ports,
            String protocol
    ) {
        return ports.stream()
                .filter(port ->
                        protocol.equalsIgnoreCase(
                                port.protocol()
                        )
                )
                .count();
    }

    private List<LinuxPortService.PortInfo>
    collectPortsSafely() {

        try {
            return portService.collectPorts();

        } catch (RuntimeException exception) {
            System.err.println(
                    "Unable to collect ports: "
                            + exception.getMessage()
            );

            return List.of();
        }
    }

    private GridPane createPortSummaryGrid(
            List<LinuxPortService.PortInfo> ports
    ) {
        long totalEndpoints = ports.size();

        long tcpEndpoints = ports.stream()
                .filter(port ->
                        "tcp".equalsIgnoreCase(
                                port.protocol()
                        )
                )
                .count();

        long udpEndpoints = ports.stream()
                .filter(port ->
                        "udp".equalsIgnoreCase(
                                port.protocol()
                        )
                )
                .count();

        long uniquePorts = ports.stream()
                .map(LinuxPortService.PortInfo::port)
                .distinct()
                .count();

        GridPane grid = new GridPane();

        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(
                createMetricCard(
                        "Total Endpoints",
                        String.valueOf(totalEndpoints),
                        "Detected",
                        "metric-card-blue",
                        "port-summary"
                ),
                0,
                0
        );

        grid.add(
                createMetricCard(
                        "TCP Endpoints",
                        String.valueOf(tcpEndpoints),
                        "TCP",
                        "metric-card-purple",
                        "port-summary"
                ),
                1,
                0
        );

        grid.add(
                createMetricCard(
                        "UDP Endpoints",
                        String.valueOf(udpEndpoints),
                        "UDP",
                        "metric-card-green",
                        "port-summary"
                ),
                2,
                0
        );

        grid.add(
                createMetricCard(
                        "Unique Ports",
                        String.valueOf(uniquePorts),
                        "Distinct ports",
                        "metric-card-orange",
                        "port-summary"
                ),
                3,
                0
        );

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

    private HBox createPortFilterBar() {

        portSearchField = new TextField();
        portSearchField.setPromptText(
                "Search port, address, or process..."
        );
        portSearchField.getStyleClass()
                .add("port-search-field");

        protocolFilter = new ComboBox<>();

        protocolFilter.setItems(
                FXCollections.observableArrayList(
                        "All",
                        "TCP",
                        "UDP"
                )
        );

        protocolFilter.setValue("All");

        protocolFilter.getStyleClass()
                .add("port-protocol-filter");

        portSearchField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                applyPortFilter()
                );

        protocolFilter.valueProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                applyPortFilter()
                );

        HBox.setHgrow(
                portSearchField,
                Priority.ALWAYS
        );

        HBox filterBar = new HBox(
                12,
                portSearchField,
                protocolFilter
        );

        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass()
                .add("port-filter-bar");

        return filterBar;
    }

    private void applyPortFilter() {

        if (filteredPorts == null) {
            return;
        }

        String searchText =
                portSearchField.getText()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        String selectedProtocol =
                protocolFilter.getValue();

        if (selectedProtocol == null) {
            selectedProtocol = "All";
        }

        String protocol =
                selectedProtocol;

        filteredPorts.setPredicate(port -> {

            boolean protocolMatches =
                    "All".equals(protocol)
                            || port.protocol()
                            .equalsIgnoreCase(protocol);

            String searchableText = String.join(
                    " ",
                    port.protocol(),
                    port.state(),
                    port.localAddress(),
                    String.valueOf(port.port()),
                    port.processName(),
                    String.valueOf(port.processId())
            ).toLowerCase(Locale.ROOT);

            return protocolMatches
                    && searchableText.contains(searchText);
        });
    }

    private TableView<LinuxPortService.PortInfo>
    createPortsTable(
            ObservableList<LinuxPortService.PortInfo> ports
    ) {

        TableView<LinuxPortService.PortInfo> table =
                new TableView<>();

        TableColumn<
                LinuxPortService.PortInfo,
                String
                > protocolColumn =
                new TableColumn<>("Protocol");

        protocolColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue()
                                .protocol()
                                .toUpperCase(Locale.ROOT)
                )
        );

        TableColumn<
                LinuxPortService.PortInfo,
                String
                > stateColumn =
                new TableColumn<>("State");

        stateColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().state()
                )
        );

        TableColumn<
                LinuxPortService.PortInfo,
                String
                > addressColumn =
                new TableColumn<>("Local Address");

        addressColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().localAddress()
                )
        );

        TableColumn<
                LinuxPortService.PortInfo,
                String
                > portColumn =
                new TableColumn<>("Port");

        portColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(
                                data.getValue().port()
                        )
                )
        );

        TableColumn<
                LinuxPortService.PortInfo,
                String
                > processColumn =
                new TableColumn<>("Process");

        processColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().processName()
                )
        );

        TableColumn<
                LinuxPortService.PortInfo,
                String
                > pidColumn =
                new TableColumn<>("PID");

        pidColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().processId() == 0
                                ? "Unknown"
                                : String.valueOf(
                                data.getValue().processId()
                        )
                )
        );

        table.getColumns().addAll(
                protocolColumn,
                stateColumn,
                addressColumn,
                portColumn,
                processColumn,
                pidColumn
        );

        protocolColumn.setPrefWidth(100);
        stateColumn.setPrefWidth(110);
        addressColumn.setPrefWidth(260);
        portColumn.setPrefWidth(90);
        processColumn.setPrefWidth(180);
        pidColumn.setPrefWidth(100);

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        table.setPrefHeight(520);
        table.setPlaceholder(
                new Label("No listening ports detected.")
        );

        table.getStyleClass().add("ports-table");

        table.setItems(ports);

        return table;
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
                "—",
                "Loading...",
                "metric-card-blue",
                "cpu"
        );

        VBox memoryCard = createMetricCard(
                "Memory Usage",
                "—",
                "Loading...",
                "metric-card-purple",
                "memory"
        );

        VBox diskCard = createMetricCard(
                "Disk Usage",
                "—",
                "Loading...",
                "metric-card-green",
                "disk"
        );

        VBox uptimeCard = createMetricCard(
                "System Uptime",
                "—",
                "Loading...",
                "metric-card-orange",
                "uptime"
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
            String colorClass,
            String metricKey
    ) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");

        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("metric-status");

        switch (metricKey) {
            case "cpu" -> {
                cpuValueLabel = valueLabel;
                cpuStatusLabel = statusLabel;
            }

            case "memory" -> {
                memoryValueLabel = valueLabel;
                memoryStatusLabel = statusLabel;
            }

            case "disk" -> {
                diskValueLabel = valueLabel;
                diskStatusLabel = statusLabel;
            }

            case "uptime" -> {
                uptimeValueLabel = valueLabel;
                uptimeStatusLabel = statusLabel;
            }

            case "port-summary" -> {

                switch (title) {
                    case "Total Endpoints" ->
                            totalEndpointsValueLabel = valueLabel;

                    case "TCP Endpoints" ->
                            tcpEndpointsValueLabel = valueLabel;

                    case "UDP Endpoints" ->
                            udpEndpointsValueLabel = valueLabel;

                    case "Unique Ports" ->
                            uniquePortsValueLabel = valueLabel;

                    default -> {
                        // Unknown summary card.
                    }
                }
            }

                    default -> {
                // No dynamic data required for this card yet.
            }
        }

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

    private void refreshDashboardMetrics() {

        if (cpuValueLabel == null
                || memoryValueLabel == null
                || diskValueLabel == null
                || uptimeValueLabel == null) {
            return;
        }

        try {
            SystemMetrics metrics =
                    metricsService.collectMetrics();

            double memoryUsagePercent =
                    calculateMemoryUsagePercent(metrics);

            cpuValueLabel.setText(
                    formatPercentage(metrics.cpuUsagePercent())
            );

            cpuStatusLabel.setText(
                    usageStatus(metrics.cpuUsagePercent())
            );

            memoryValueLabel.setText(
                    formatMemory(metrics.usedMemoryBytes())
                            + " / "
                            + formatMemory(metrics.totalMemoryBytes())
            );

            memoryStatusLabel.setText(
                    formatPercentage(memoryUsagePercent)
                            + " used"
            );

            diskValueLabel.setText(
                    formatPercentage(metrics.diskUsagePercent())
            );

            diskStatusLabel.setText(
                    diskUsageStatus(metrics.diskUsagePercent())
            );

            uptimeValueLabel.setText(
                    formatUptime(metrics.uptimeSeconds())
            );

            uptimeStatusLabel.setText("Running");

        } catch (RuntimeException exception) {

            cpuValueLabel.setText("N/A");
            cpuStatusLabel.setText("Unavailable");

            memoryValueLabel.setText("N/A");
            memoryStatusLabel.setText("Unavailable");

            diskValueLabel.setText("N/A");
            diskStatusLabel.setText("Unavailable");

            uptimeValueLabel.setText("N/A");
            uptimeStatusLabel.setText("Unavailable");

            System.err.println(
                    "Unable to read system metrics: "
                            + exception.getMessage()
            );
        }
    }

    private double calculateMemoryUsagePercent(
            SystemMetrics metrics
    ) {
        if (metrics.totalMemoryBytes() <= 0) {
            return 0;
        }

        return (
                metrics.usedMemoryBytes()
                        * 100.0
                        / metrics.totalMemoryBytes()
        );
    }

    private String formatPercentage(double value) {
        return String.format(
                Locale.ROOT,
                "%.1f%%",
                value
        );
    }

    private String formatMemory(long bytes) {

        double gigabytes =
                bytes / (1024.0 * 1024.0 * 1024.0);

        return String.format(
                Locale.ROOT,
                "%.1f GB",
                gigabytes
        );
    }

    private String formatOperatingSystem(
            SystemMetrics metrics
    ) {
        String family = metrics.osFamily();
        String version = metrics.osVersion();

        if (version == null || version.isBlank()) {
            return family;
        }

        return family + " " + version;
    }

    private String diskUsageStatus(double percentage) {

        if (percentage < 70) {
            return "Healthy";
        }

        if (percentage < 90) {
            return "Nearly full";
        }

        return "Critical";
    }

    private String formatUptime(long totalSeconds) {

        long days = totalSeconds / 86_400;
        long hours = (totalSeconds % 86_400) / 3_600;
        long minutes = (totalSeconds % 3_600) / 60;

        if (days > 0) {
            return String.format(
                    Locale.ROOT,
                    "%dd %dh",
                    days,
                    hours
            );
        }

        if (hours > 0) {
            return String.format(
                    Locale.ROOT,
                    "%dh %dm",
                    hours,
                    minutes
            );
        }

        return String.format(
                Locale.ROOT,
                "%dm",
                minutes
        );
    }

    private String usageStatus(double percentage) {

        if (percentage < 70) {
            return "Healthy";
        }

        if (percentage < 90) {
            return "Elevated";
        }

        return "High";
    }

    private HBox createServicesCard() {

        serviceRows = new VBox(12);

        refreshServiceRows();

        HBox card = new HBox(serviceRows);

        card.setPadding(
                new Insets(22)
        );

        card.getStyleClass().add("services-card");

        return card;
    }

    private void refreshServiceRows() {

        if (serviceRows == null) {
            return;
        }

        serviceRows.getChildren().clear();

        for (LinuxServiceStatusService.ServiceStatus serviceStatus
                : serviceStatusService.collectStatuses()) {

            serviceRows.getChildren().add(
                    createServiceRow(
                            serviceStatus.displayName(),
                            serviceStatus.status(),
                            serviceStatus.running()
                    )
            );
        }
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

    private LineChart<Number, Number> createMiniLineChart(
            XYChart.Series<Number, Number> series
    ) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(
                0,
                100,
                25
        );

        xAxis.setVisible(false);
        yAxis.setVisible(false);

        LineChart<Number, Number> chart =
                new LineChart<>(
                        xAxis,
                        yAxis
                );

        chart.getData().add(series);

        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);

        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);

        chart.setMinHeight(110);
        chart.setPrefHeight(110);
        chart.setMaxHeight(110);

        return chart;
    }

    private VBox createHealthChartCard(
            String title,
            LineChart<Number, Number> chart,
            String colorClass
    ) {
        Label titleLabel =
                new Label(title);

        titleLabel.getStyleClass()
                .add("metric-title");

        VBox card = new VBox(
                8,
                titleLabel,
                chart
        );

        card.getStyleClass()
                .addAll(
                        "metric-card",
                        "health-chart-card",
                        colorClass
                );

        card.setPrefWidth(300);

        return card;
    }

    private void updateHealthChartSeries(
            SystemMetricsService.SystemMetrics metrics
    ) {
        chartSampleIndex++;

        addChartPoint(
                cpuChartSeries,
                metrics.cpuUsagePercent()
        );

        double memoryUsagePercent = 0;

        if (metrics.totalMemoryBytes() > 0) {
            memoryUsagePercent =
                    metrics.usedMemoryBytes()
                            * 100.0
                            / metrics.totalMemoryBytes();
        }

        addChartPoint(
                memoryChartSeries,
                memoryUsagePercent
        );

        addChartPoint(
                diskChartSeries,
                metrics.diskUsagePercent()
        );
    }

    private void addChartPoint(
            XYChart.Series<Number, Number> series,
            double value
    ) {
        series.getData().add(
                new XYChart.Data<>(
                        chartSampleIndex,
                        value
                )
        );

        while (
                series.getData().size()
                        > CHART_HISTORY_LIMIT
        ) {
            series.getData().remove(0);
        }
    }
}