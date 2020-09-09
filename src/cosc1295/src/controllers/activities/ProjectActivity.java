package cosc1295.src.controllers.activities;

import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Project;
import cosc1295.src.models.Team;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import helpers.utilities.LogicalAssistant;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * This Activity allows user to replace Team Project.
 * When the Team's Project is changed, recalculate the Fitness Metrics.
 * Observable Design Pattern is used to control the flow of this Activity.
 */
public class ProjectActivity extends AnchorPane implements IActivity {

    private Consumer<SharedEnums.GUI_ACTION_CONTEXT> intent;

    //Dependency injections to access data processing services
    private final TeamService teamService;
    private final ProjectService projectService;

    //The observable objects to keep track of the changes made to data
    private final SimpleObjectProperty<Team> selectedTeam;
    private final SimpleObjectProperty<Project> selectedProject;

    public ProjectActivity() {
        teamService = new TeamService();
        projectService = new ProjectService();

        selectedTeam = new SimpleObjectProperty<>(null);
        selectedProject = new SimpleObjectProperty<>(null);
    }

    public void drawProjectSettingTaskContents(Scene container, String postMessage) {
        this.setId(this.getClass().getSimpleName());
        IActivity.drawActivityTitle(container, this, "Remove Students From Teams");

        List<Team> teams = teamService.readAllTeamsFromFile();
        List<Project> projects = projectService.readAllProjectsFromFile();
        List<Preference> preferences = (new StudentService()).readAllStudentPreferencesFromFile();

        boolean error = teams == null || projects == null || preferences == null;
        if (error) drawActivityFailMessage(container, "An error occurred while retrieving data from files.\nPlease try again.");

        error = !error && teams.size() == 0;
        if (error) drawActivityFailMessage(container, "You have no Team. Please select another task.");

        if (!error) {
            if (postMessage != null) IActivity.drawSuccessMessage(postMessage, this);

            double tablePrefWidth = (container.getWidth() - MARGIN * 2) / 2;
            attachListenersToObservables(tablePrefWidth, projects);

            drawButtonBasedOnContext(container, false);
            drawWidgetsForReassigningProject(teams, tablePrefWidth);
            setMainButtonListener(container, projects, preferences);
        }
    }

    private void attachListenersToObservables(double initialWidth, List<Project> projects) {
        selectedTeam.addListener(observable -> {
            if (selectedTeam.get() == null) {
                selectedProject.set(null);
                eraseProjectSelectionFragment();
            }
            else drawProjectSelectionFragment(initialWidth, projects);
        });

        selectedProject.addListener(observable -> {
            Button assignButton = (Button) this.lookup("#main-button");
            assignButton.setDisable(selectedProject.get() == null);
        });
    }

    private void drawWidgetsForReassigningProject(List<Team> teams, double initialWidth) {
        Label selectTeamLabel = new Label("Please select a Team to reassign Project");
        selectTeamLabel.getStyleClass().add("subtitle");
        AnchorPane.setTopAnchor(selectTeamLabel, MARGIN * 6);

        ComboBox<String> teamsDropdown = new ComboBox<>();
        teamsDropdown.getStyleClass().add("dropdown-select");
        List<String> dropdownItems = new ArrayList<String>() {{ add("Select team"); }};
        teams.forEach(team -> dropdownItems.add(team.compact()));

        teamsDropdown.getItems().addAll(dropdownItems);
        teamsDropdown.setValue(dropdownItems.get(0));
        AnchorPane.setTopAnchor(teamsDropdown, MARGIN * 7);

        constraintElements(initialWidth, selectTeamLabel, teamsDropdown);
        this.getChildren().addAll(selectTeamLabel, teamsDropdown);

        teamsDropdown.getSelectionModel().selectedIndexProperty().addListener(observable -> {
            int selectedIndex = teamsDropdown.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) selectedTeam.set(null);
            else selectedTeam.set(teams.get(selectedIndex - 1));
        });
    }

    private void drawProjectSelectionFragment(double initialWidth, List<Project> projects) {
        Label selectProjectLabel = new Label("Please select a Project to assign");
        selectProjectLabel.setId("select-project-label");
        selectProjectLabel.getStyleClass().add("subtitle");
        AnchorPane.setTopAnchor(selectProjectLabel, MARGIN * 9.5);

        ComboBox<String> projectsDropdown = new ComboBox<>();
        projectsDropdown.getStyleClass().add("dropdown-select");
        projectsDropdown.setId("select-project-dropdown");
        List<String> dropdownItems = new ArrayList<String>() {{ add("Select project"); }};
        projects.forEach(project -> {
            if (!project.getUniqueId().equalsIgnoreCase(selectedTeam.get().getProject().getUniqueId()))
                dropdownItems.add(project.compact());
        });

        projectsDropdown.getItems().addAll(dropdownItems);
        projectsDropdown.setValue(dropdownItems.get(0));
        AnchorPane.setTopAnchor(projectsDropdown, MARGIN * 10.5);

        constraintElements(initialWidth, selectProjectLabel, projectsDropdown);
        this.getChildren().addAll(selectProjectLabel, projectsDropdown);

        projectsDropdown.getSelectionModel().selectedItemProperty().addListener(observable -> {
            String selectedItem = projectsDropdown.getSelectionModel().getSelectedItem();
            if (selectedItem.equals(dropdownItems.get(0))) selectedProject.set(null);
            else {
                AtomicReference<Project> dropdownProject = new AtomicReference<>(null);
                projects.forEach(project -> {
                    if (selectedItem.contains(project.getUniqueId()))
                        dropdownProject.set(project);
                });

                if (dropdownProject.get() == null) selectedProject.set(null);
                else selectedProject.set(dropdownProject.get());
            }
        });
    }

    private void eraseProjectSelectionFragment() {
        IActivity.removeElementIfExists("select-project-label", this);
        IActivity.removeElementIfExists("select-project-dropdown", this);
    }

    private void constraintElements(double initialWidth, Region... elements) {
        for (Region element : elements) {
            element.setPrefWidth(initialWidth);
            AnchorPane.setLeftAnchor(element, (this.getPrefWidth() - element.getPrefWidth()) / 2);

            this.prefWidthProperty().addListener((observable, oldValue, newValue) ->
                AnchorPane.setLeftAnchor(element, AnchorPane.getLeftAnchor(element) + IActivity.offset(oldValue, newValue))
            );
        }
    }

    private void drawActivityFailMessage(Scene container, String message) {
        IActivity.drawActivityMessageOnException(container, this, message);
        drawButtonBasedOnContext(container, true);
    }

    private void drawButtonBasedOnContext(Scene container, boolean isErrorOccurred) {
        Button backButton = new Button(isErrorOccurred ? "Okay" : "Back");
        Button assignButton = new Button("Assign");

        IActivity.drawActivityFixedButtons(container, this, isErrorOccurred, backButton, assignButton);
        backButton.setOnAction(event -> intent.accept(SharedEnums.GUI_ACTION_CONTEXT.LAUNCH));
    }

    private void setMainButtonListener(Scene container, List<Project> projects, List<Preference> preferences) {
        Button assignButton = (Button) this.lookup("#main-button");

        assignButton.setOnAction(event -> {
            selectedTeam.get().setProject(selectedProject.get());
            if (selectedTeam.get().getMembers().size() == SharedConstants.GROUP_LIMIT)
                selectedTeam.get().setFitnessMetrics((new ControllerBase()).calculateTeamFitnessMetricsFor(selectedTeam.get(), projects, preferences));

            if (teamService.updateTeam(selectedTeam.get())) {
                selectedTeam.set(null);
                drawProjectSettingTaskContents(container, "The new Project has set to the Team successfully.");
            }
            else drawActivityFailMessage(container, "An error occurred while updating data to files.\nPlease try again.");
        });
    }

    @Override
    public <T> void setIntent(Consumer<T> callback) {
        intent = (Consumer<SharedEnums.GUI_ACTION_CONTEXT>) callback;
    }
}
