package cosc1295.src.controllers.activities;

import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import helpers.utilities.LogicalAssistant;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class StatisticsActivity extends AnchorPane implements IActivity {


    private static final String COMPETENCY = "Skill Competency";
    private static final String PREFERENCE = "Project Preference";
    private static final String SHORTFALL = "Skill Shortfall";

    private Consumer<SharedEnums.GUI_ACTION_CONTEXT> intent;

    private final TeamService teamService;
    private final ControllerBase controllerBase;

    private List<Double> standardDeviations;

    public StatisticsActivity() {
        teamService = new TeamService();
        controllerBase = new ControllerBase();
    }

    public void drawStatisticsContents(Scene container) {
        this.setId(this.getClass().getSimpleName());
        IActivity.drawActivityTitle(container, this, "View Team Statistics");

        List<Team> teams = teamService.readAllTeamsFromFile();
        List<Student> students = (new StudentService()).readAllStudentsFromFile();
        List<Project> projects = (new ProjectService()).readAllProjectsFromFile();
        List<Preference> preferences = (new StudentService()).readAllStudentPreferencesFromFile();

        if (teams == null || projects == null || preferences == null) drawActivityFailMessage(container);

        LogicalAssistant.setStudentDataInTeams(teams, students);
        standardDeviations = controllerBase.calculateStandardDeviationsForFitnessMetrics(teams, projects, preferences);
        drawTabPaneForDisplayingCharts(container, teams);
        drawBackButton();
    }

    private void drawTabPaneForDisplayingCharts(Scene container, List<Team> teams) {
        double initialWidth = container.getWidth() - MARGIN * 2;

        TabPane triplePane = new TabPane();
        triplePane.setPrefSize(initialWidth, MARGIN * 19.5);
        triplePane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        this.getChildren().add(triplePane);

        Tab competencyTab = new Tab(COMPETENCY, drawPaneBodyWithStatisticsChartFor(COMPETENCY, teams, initialWidth));
        Tab preferenceTab = new Tab(PREFERENCE, drawPaneBodyWithStatisticsChartFor(PREFERENCE, teams, initialWidth));
        Tab shortfallTab = new Tab(SHORTFALL, drawPaneBodyWithStatisticsChartFor(SHORTFALL, teams, initialWidth));

        triplePane.getTabs().add(competencyTab);
        triplePane.getTabs().add(preferenceTab);
        triplePane.getTabs().add(shortfallTab);

        AnchorPane.setLeftAnchor(triplePane, MARGIN);
        AnchorPane.setBottomAnchor(triplePane, MARGIN);

        constraintElements(triplePane);
        setTabSelectionListener(triplePane);
    }

    private Pane drawPaneBodyWithStatisticsChartFor(String metrics, List<Team> teams, double initialWidth) {
        Pane chartPane = new Pane();
        chartPane.getStyleClass().add("chart-pane");

        chartPane.setPrefSize(initialWidth, MARGIN * 19);
        constraintElements(chartPane);

        switch (metrics) {
            case COMPETENCY:
                return drawSkillCompetencyChart(chartPane, teams, initialWidth);
            case PREFERENCE:
                return drawProjectPreferenceChart(chartPane, teams, initialWidth);
            default: //Skill Shortfall
                return drawSkillShortFallChart(chartPane, teams, initialWidth);
        }
    }

    private Pane drawSkillCompetencyChart(Pane chartPane, List<Team> teams, double initialWidth) {
        BarChart competencyChart = createEmptyBarChart("Average Skill Competency", initialWidth);

        XYChart.Series<String, Double> competencyData = new XYChart.Series<>();
        List<XYChart.Data<String, Double>> data = new ArrayList<>();
        teams.forEach(team -> {
            if (team.getFitnessMetrics() != null)
                data.add(new XYChart.Data<>(
                    "Team #" + team.getId(),
                    (Double) team.getFitnessMetrics().getAverageTeamSkillCompetency()
                ));
        });

        competencyData.getData().addAll(data);
        competencyChart.getData().add(competencyData);
        chartPane.getChildren().add(competencyChart);

        VBox standardDeviation = drawStandardDeviationLabelFor(COMPETENCY, standardDeviations.get(0));
        constraintSD(standardDeviation);
        this.getChildren().add(standardDeviation);
        return chartPane;
    }

    private Pane drawProjectPreferenceChart(Pane chartPane, List<Team> teams, double initialWidth) {
        BarChart preferenceChart = createEmptyBarChart("Percentage of 1st & 2nd Preference", initialWidth);

        XYChart.Series<String, Double> preferenceData = new XYChart.Series<>();
        List<XYChart.Data<String, Double>> data = new ArrayList<>();
        teams.forEach(team -> {
            if (team.getFitnessMetrics() != null)
                data.add(new XYChart.Data<>(
                    "Team #" + team.getId(),
                    (Double) team.getFitnessMetrics().getPreferenceSatisfaction().getKey()
                ));
        });

        preferenceData.getData().addAll(data);
        preferenceChart.getData().add(preferenceData);
        chartPane.getChildren().add(preferenceChart);

        VBox standardDeviation = drawStandardDeviationLabelFor(PREFERENCE, standardDeviations.get(1));
        constraintSD(standardDeviation);
        this.getChildren().add(standardDeviation);
        return chartPane;
    }

    private Pane drawSkillShortFallChart(Pane chartPane, List<Team> teams, double initialWidth) {
        BarChart shortfallChart = createEmptyBarChart("Average Skill Shortfalls", initialWidth);

        XYChart.Series<String, Double> shortfallData = new XYChart.Series<>();
        List<XYChart.Data<String, Double>> data = new ArrayList<>();
        teams.forEach(team -> {
            if (team.getFitnessMetrics() != null)
                data.add(new XYChart.Data<>(
                        "Team #" + team.getId(),
                        (Double) team.getFitnessMetrics().getAverageSkillShortfall()
                ));
        });

        shortfallData.getData().addAll(data);
        shortfallChart.getData().add(shortfallData);
        chartPane.getChildren().add(shortfallChart);

        VBox standardDeviation = drawStandardDeviationLabelFor(SHORTFALL, standardDeviations.get(2));
        constraintSD(standardDeviation);
        this.getChildren().add(standardDeviation);
        return chartPane;
    }

    private BarChart createEmptyBarChart(String title, double initialWidth) {
        CategoryAxis hAxis = new CategoryAxis();
        NumberAxis vAxis = new NumberAxis();

        BarChart barChart = new BarChart(hAxis, vAxis);
        barChart.setPrefSize(initialWidth * 0.75, MARGIN * 19 * 0.9);
        constraintElements(barChart);

        barChart.setLegendVisible(false);
        barChart.setBarGap(MARGIN * 1.5);
        barChart.setTitle(title);
        barChart.setStyle("-fx-font-size: " + MARGIN * 0.6 + "px;");
        barChart.setTitleSide(Side.BOTTOM);

        return barChart;
    }

    private VBox drawStandardDeviationLabelFor(String metrics, double value) {
        Label sdTitle = new Label("Standard Deviation for " + metrics);
        sdTitle.setPrefSize(MARGIN * 6, MARGIN * 8);
        sdTitle.getStyleClass().add("sd-title");

        Label sdText = new Label(value + "");
        sdText.setPrefSize(MARGIN * 6, MARGIN * 8);
        sdText.getStyleClass().add("sd-text");

        VBox sdTray = new VBox(sdTitle, sdText);
        sdTray.setId("standard-deviation-vbox-" + metrics.replace(SharedConstants.SPACE, SharedConstants.EMPTY_STRING));
        sdTray.getStyleClass().add("sd-tray");
        sdTray.setPrefSize(MARGIN * 7, MARGIN * 8);

        return sdTray;
    }

    private void setTabSelectionListener(TabPane tabPane) {
        tabPane.getSelectionModel().selectedIndexProperty().addListener(observable -> {
            int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
            VBox competencyBox = (VBox) this.lookup("#standard-deviation-vbox-" + COMPETENCY.replace(SharedConstants.SPACE, SharedConstants.EMPTY_STRING));
            VBox preferenceBox = (VBox) this.lookup("#standard-deviation-vbox-" + PREFERENCE.replace(SharedConstants.SPACE, SharedConstants.EMPTY_STRING));
            VBox shortfallBox = (VBox) this.lookup("#standard-deviation-vbox-" + SHORTFALL.replace(SharedConstants.SPACE, SharedConstants.EMPTY_STRING));

            List<VBox> vBoxes = new ArrayList<VBox>() {{ add(competencyBox); add(preferenceBox); add(shortfallBox); }};
            vBoxes.forEach(box -> box.setVisible(vBoxes.indexOf(box) == selectedIndex));
        });

        VBox preferenceBox = (VBox) this.lookup("#standard-deviation-vbox-" + PREFERENCE.replace(SharedConstants.SPACE, SharedConstants.EMPTY_STRING));
        VBox shortfallBox = (VBox) this.lookup("#standard-deviation-vbox-" + SHORTFALL.replace(SharedConstants.SPACE, SharedConstants.EMPTY_STRING));

        preferenceBox.setVisible(false);
        shortfallBox.setVisible(false);
    }

    private void drawActivityFailMessage(Scene container) {
        IActivity.drawActivityMessageOnException(container, this, "An error occurred while retrieving data from files.\nPlease try again.");
        drawBackButton();
    }

    private void constraintElements(Region... elements) {
        for (Region element : elements) {
            this.prefWidthProperty().addListener((observable, oldValue, newValue) ->
                element.setPrefWidth(element.getPrefWidth() + IActivity.offset(oldValue, newValue))
            );

            this.prefHeightProperty().addListener((observable, oldValue, newValue) ->
                element.setPrefHeight(element.getPrefHeight() + IActivity.offset(oldValue, newValue))
            );
        }
    }

    private void constraintSD(VBox sd) {
        AnchorPane.setBottomAnchor(sd, MARGIN * 6);
        AnchorPane.setRightAnchor(sd, MARGIN * 2.75);

        this.prefWidthProperty().addListener((observable, oldValue, newValue) ->
                AnchorPane.setRightAnchor(sd, AnchorPane.getRightAnchor(sd) + IActivity.offset(oldValue, newValue))
        );
    }

    private void drawBackButton() {
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("back-button");
        this.getChildren().add(backButton);

        backButton.setLayoutY(MARGIN / 2);
        backButton.setLayoutX(MARGIN / 2);

        backButton.setOnAction(event -> intent.accept(SharedEnums.GUI_ACTION_CONTEXT.LAUNCH));
    }

    @Override
    public <T> void setIntent(Consumer<T> callback) {
        this.intent = (Consumer<SharedEnums.GUI_ACTION_CONTEXT>) callback;
    }
}
