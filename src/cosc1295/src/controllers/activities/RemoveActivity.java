package cosc1295.src.controllers.activities;

import com.sun.istack.internal.Nullable;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
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
import java.util.function.Consumer;

/**
 * This Activity allows user to remove a member from a Team.
 * When removing a member, if Team has a Fitness Metrics, delete it; if Team has no member left, delete team.
 * Observable Design Pattern is used to control the flow of this Activity.
 */
public class RemoveActivity extends AnchorPane implements IActivity {

    private Consumer<SharedEnums.GUI_ACTION_CONTEXT> intent;

    //Dependency injections to access data processing services
    private final TeamService teamService;
    private final StudentService studentService;

    //The observable objects to keep track of the changes made to data
    private final SimpleObjectProperty<Team> selectedTeam;
    private final SimpleObjectProperty<Student> selectedStudent;

    public RemoveActivity() {
        studentService = new StudentService();
        teamService = new TeamService();

        selectedTeam = new SimpleObjectProperty<>(null);
        selectedStudent = new SimpleObjectProperty<>(null);
    }

    public void drawRemovalTaskContents(Scene container, @Nullable String postMessage) {
        this.setId(this.getClass().getSimpleName());
        IActivity.drawActivityTitle(container, this, "Remove Students From Teams");

        List<Team> teams = teamService.readAllTeamsFromFile();
        List<Student> students = studentService.readAllStudentsFromFile();

        boolean error = teams == null || students == null;
        if (error) drawActivityFailMessage(container, "An error occurred while retrieving data from files.\nPlease try again.");

        error = !error && teams.size() == 0;
        if (error) drawActivityFailMessage(container, "You have no Team to remove.\nPlease select another task.");

        if (!error) {
            if (postMessage != null) IActivity.drawSuccessMessage(postMessage, this);

            double tablePrefWidth = (container.getWidth() - MARGIN * 2) / 2;
            attachListenersToObservables(tablePrefWidth);

            drawButtonBasedOnContext(container, false);
            LogicalAssistant.setStudentDataInTeams(teams, students);
            drawWidgetsForRemovingStudentsTask(teams, tablePrefWidth);
        }
    }

    private void attachListenersToObservables(double initialWidth) {
        selectedTeam.addListener(observable -> {
            if (selectedTeam.get() == null) {
                selectedStudent.set(null);
                eraseStudentSelectionFragment();
            }
            else drawStudentSelectionFragment(initialWidth);
        });

        selectedStudent.addListener(observable -> {
            Button removeButton = (Button) this.lookup("#main-button");
            removeButton.setDisable(selectedStudent.get() == null);
        });
    }

    private void drawWidgetsForRemovingStudentsTask(List<Team> teams, double initialWidth) {
        Label removeWarning = new Label("Please note: Removing the last Student from a Team will delete that Team. Teams no longer having 4 members will have it Fitness Metrics data deleted.");
        removeWarning.getStyleClass().add("remove-warning");
        AnchorPane.setTopAnchor(removeWarning, MARGIN * 3.35);

        Label selectTeamLabel = new Label("Please select a Team to remove Student from");
        selectTeamLabel.getStyleClass().add("subtitle");
        AnchorPane.setTopAnchor(selectTeamLabel, MARGIN * 6);

        ComboBox<String> teamsDropdown = new ComboBox<>();
        teamsDropdown.getStyleClass().add("dropdown-select");
        List<String> dropdownItems = new ArrayList<String>() {{ add("Select team"); }};
        teams.forEach(team -> dropdownItems.add(team.compact()));

        teamsDropdown.getItems().addAll(dropdownItems);
        teamsDropdown.setValue(dropdownItems.get(0));
        AnchorPane.setTopAnchor(teamsDropdown, MARGIN * 7);

        constraintElements(initialWidth, removeWarning, selectTeamLabel, teamsDropdown);
        this.getChildren().addAll(removeWarning, selectTeamLabel, teamsDropdown);

        teamsDropdown.getSelectionModel().selectedIndexProperty().addListener(observable -> {
            int selectedIndex = teamsDropdown.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) selectedTeam.set(null);
            else selectedTeam.set(teams.get(selectedIndex - 1));
        });
    }

    private void drawStudentSelectionFragment(double initialWidth) {
        Label selectStudentLabel = new Label("Please select a Student to remove");
        selectStudentLabel.setId("select-student-label");
        selectStudentLabel.getStyleClass().add("subtitle");
        AnchorPane.setTopAnchor(selectStudentLabel, MARGIN * 9.5);

        ComboBox<String> studentsDropdown = new ComboBox<>();
        studentsDropdown.getStyleClass().add("dropdown-select");
        studentsDropdown.setId("select-student-dropdown");
        List<String> dropdownItems = new ArrayList<String>() {{ add("Select student"); }};
        selectedTeam.get().getMembers().forEach(member -> dropdownItems.add(member.display()));

        studentsDropdown.getItems().addAll(dropdownItems);
        studentsDropdown.setValue(dropdownItems.get(0));
        AnchorPane.setTopAnchor(studentsDropdown, MARGIN * 10.5);

        constraintElements(initialWidth, selectStudentLabel, studentsDropdown);
        this.getChildren().addAll(selectStudentLabel, studentsDropdown);

        studentsDropdown.getSelectionModel().selectedIndexProperty().addListener(observable -> {
            int selectedIndex = studentsDropdown.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) selectedStudent.set(null);
            else selectedStudent.set(selectedTeam.get().getMembers().get(selectedIndex - 1));
        });
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

    private void eraseStudentSelectionFragment() {
        IActivity.removeElementIfExists("select-student-label", this);
        IActivity.removeElementIfExists("select-student-dropdown", this);
    }

    private void drawButtonBasedOnContext(Scene container, boolean isErrorOccurred) {
        Button backButton = new Button(isErrorOccurred ? "Okay" : "Back");
        Button removeButton = new Button("Remove");

        IActivity.drawActivityFixedButtons(container, this, isErrorOccurred, backButton, removeButton);
        backButton.setOnAction(event -> intent.accept(SharedEnums.GUI_ACTION_CONTEXT.LAUNCH));

        removeButton.setOnAction(event -> {
            selectedTeam.get().getMembers().remove(selectedStudent.get());
            boolean result;
            if (selectedTeam.get().getFitnessMetrics() != null) {
                result = teamService.removeTeamFitness(selectedTeam.get().getFitnessMetrics().getId());
                selectedTeam.get().setFitnessMetrics(null);

                if (!result) {
                    removeButton.setVisible(false);
                    drawActivityFailMessage(container, "An error occurred while updating data. Please retry.");
                }
            }

            if (selectedTeam.get().getMembers().size() == 0) result = teamService.deleteTeam(selectedTeam.get());
            else result = teamService.updateTeam(selectedTeam.get());

            if (!result) drawActivityFailMessage(container, "An error occurred while updating data. Please retry.");
            else {
                selectedTeam.set(null);
                eraseStudentSelectionFragment();
                drawRemovalTaskContents(container, "The Student has been removed from Team successfully.");
            }
        });
    }

    @Override
    public <T> void setIntent(Consumer<T> callback) {
        intent = (Consumer<SharedEnums.GUI_ACTION_CONTEXT>) callback;
    }
}
