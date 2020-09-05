package cosc1295.src.controllers.activities;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.*;
import cosc1295.src.views.gui.viewmodels.StudentVM;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import helpers.utilities.LogicalAssistant;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AssignActivity extends AnchorPane implements IActivity {

    private Consumer<SharedEnums.GUI_ACTION_CONTEXT> intent;
    private static final String SET_PROJECT = "SET_PROJECT";

    private final StudentService studentService;
    private final TeamService teamService;
    private final ControllerBase controllerBase;

    private final SimpleObjectProperty<Student> studentToAssign;
    private final SimpleObjectProperty<Team> teamToReceiveMember;
    private final SimpleObjectProperty<Student> studentInTeamToBeReplaced;

    public AssignActivity() {
        studentService = new StudentService();
        teamService = new TeamService();
        controllerBase = new ControllerBase();

        studentToAssign = new SimpleObjectProperty<>(null);
        teamToReceiveMember = new SimpleObjectProperty<>(null);
        studentInTeamToBeReplaced = new SimpleObjectProperty<>(null);

        eraseTeamDetailsTable();
    }

    private void setTeamToReceiveMember(@Nullable Team team) {
        teamToReceiveMember.set(team);
    }

    public void drawAssigningTaskContents(Scene container, @Nullable String postMessage) {
        this.setId(this.getClass().getSimpleName());
        IActivity.drawActivityTitle(container, this, "Assign Students To Teams");

        List<Student> students = studentService.readAllStudentsFromFile();
        List<Team> teams = teamService.readAllTeamsFromFile();

        boolean error = students == null || teams == null;
        if (error) drawActivityFailMessage(container, "An error occurred while retrieving data from files.\nPlease try again.");

        error = !error && (students.size() < SharedConstants.GROUP_LIMIT && teams.size() == 0);
        if (error) drawActivityFailMessage(container, "The number of Students is insufficient to form new Team.\nPlease add more Students then come back.");

        if (!error) {
            eraseTeamDetailsTable();
            if (postMessage != null) IActivity.drawSuccessMessage(postMessage, this);

            attachListenersToObservables(container);
            drawButtonBasedOnContext(container, false);

            List<Student> assignableStudents = LogicalAssistant.filterUnteamedStudents(students, teams);
            LogicalAssistant.setStudentDataInTeams(teams, students);
            drawWidgetsForAssigningStudentsTask(container, assignableStudents, teams);

            drawButtonBasedOnContext(container, false);
        }
    }

    private void attachListenersToObservables(Scene container) {
        SimpleBooleanProperty shouldEnableAssignButton = new SimpleBooleanProperty(false);
        AtomicReference<String> message = new AtomicReference<>(SharedConstants.EMPTY_STRING);

        studentToAssign.addListener(observable -> {
            message.set(SharedConstants.EMPTY_STRING);
            manageStatusMessageAndAssignButtonBasedOnRequirements(container, message, shouldEnableAssignButton);
        });

        teamToReceiveMember.addListener((observable -> {
            message.set(SharedConstants.EMPTY_STRING);
            manageStatusMessageAndAssignButtonBasedOnRequirements(container, message, shouldEnableAssignButton);
        }));

        studentInTeamToBeReplaced.addListener(observable -> {
            message.set(SharedConstants.EMPTY_STRING);
            manageStatusMessageAndAssignButtonBasedOnRequirements(container, message, shouldEnableAssignButton);
        });

        shouldEnableAssignButton.addListener(observable -> {
            Button assignButton = (Button) this.lookup("#main-button");
            assignButton.setDisable(!shouldEnableAssignButton.get());
        });
    }

    private void manageStatusMessageAndAssignButtonBasedOnRequirements(
        Scene container, AtomicReference<String> message, SimpleBooleanProperty shouldEnableAssignButton
    ) {
        IActivity.removeElementIfExists("requirement-message", this);
        message.set(SharedConstants.EMPTY_STRING);

        Student selectedStudent = studentToAssign.get();
        if (selectedStudent == null) shouldEnableAssignButton.set(false);

        if (selectedStudent != null && teamToReceiveMember.get() != null) {
            List<Student> receivingTeamMembers = new ArrayList<>(teamToReceiveMember.get().getMembers());

            if (studentInTeamToBeReplaced.get() != null) receivingTeamMembers.remove(studentInTeamToBeReplaced.get());
            Pair<Boolean, List<String>> teamRequirements = LogicalAssistant.produceTeamRequirementsOnNewMember(
                    receivingTeamMembers, new ArrayList<Student>() {{ add(studentToAssign.get()); }}
            );

            if (teamRequirements == null) shouldEnableAssignButton.set(true);
            else {
                if (teamRequirements.getKey() && teamRequirements.getValue() == null)
                    shouldEnableAssignButton.set(true);
                else {
                    shouldEnableAssignButton.set(false);

                    if (teamRequirements.getKey()) message.set(message.get() + "This Team needs Leader.");
                    if (teamRequirements.getValue() != null &&
                            teamRequirements.getValue().contains(selectedStudent.getUniqueId())
                    ) message.set(message.get() + " Member conflicts occur.");
                }
            }

            if (!message.get().equals(SharedConstants.EMPTY_STRING))
                drawTeamRequirementMessage(container, message.get());
            else shouldEnableAssignButton.set(true);
        }
    }

    private void drawTeamRequirementMessage(Scene container, String message) {
        IActivity.removeElementIfExists("requirement-message", this);
        double initialWidth = (container.getWidth() - MARGIN * 3) / 2;

        Label requirementMessage = new Label(message);
        requirementMessage.setId("requirement-message");
        requirementMessage.getStyleClass().add("requirement-message");
        this.getChildren().add(requirementMessage);

        requirementMessage.setPrefWidth(initialWidth);
        AnchorPane.setBottomAnchor(requirementMessage, MARGIN * 3.1);
        AnchorPane.setLeftAnchor(requirementMessage, initialWidth + MARGIN * 2);

        this.prefWidthProperty().addListener(((observable, oldValue, newValue) -> {
            double offset = IActivity.offset(oldValue, newValue);

            AnchorPane.setLeftAnchor(requirementMessage, AnchorPane.getLeftAnchor(requirementMessage) + offset);
            requirementMessage.setPrefWidth(initialWidth + offset);
        }));
    }

    private void drawActivityFailMessage(Scene container, String message) {
        IActivity.drawActivityMessageOnException(container, this, message);
        drawButtonBasedOnContext(container, true);
    }

    private void drawWidgetsForAssigningStudentsTask(Scene container, List<Student> students, List<Team> teams) {
        Label selectStudentTitle = new Label("Select at least 1 Student to assign");
        this.getChildren().add(selectStudentTitle);

        selectStudentTitle.getStyleClass().add("subtitle");
        selectStudentTitle.setLayoutY(MARGIN * 4);
        selectStudentTitle.setLayoutX(MARGIN);

        double tablePrefWidth = (container.getWidth() - MARGIN * 3) / 2;

        Label selectTeamTitle = new Label("Select 1 Team to receive Student");
        selectTeamTitle.setWrapText(true);
        selectTeamTitle.setId("select-team-title");
        this.getChildren().add(selectTeamTitle);

        selectTeamTitle.getStyleClass().add("subtitle");
        selectTeamTitle.setLayoutY(MARGIN * 4);
        AnchorPane.setLeftAnchor(selectTeamTitle, tablePrefWidth + MARGIN * 2);

        container.widthProperty().addListener((observable, oldValue, newValue) ->
            AnchorPane.setLeftAnchor(
                selectTeamTitle, (
                AnchorPane.getLeftAnchor(selectTeamTitle) + IActivity.offset(oldValue, newValue)
            )
        ));

        drawStudentsSelectionArea(students, tablePrefWidth);
        drawTeamsSelectionArea(teams, tablePrefWidth, null);
    }

    private void drawStudentsSelectionArea(List<Student> students, double initialWidth) {
        TableView studentsTable = new TableView();
        studentsTable.getStyleClass().add("data-table");
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.getChildren().add(studentsTable);

        studentsTable.setPrefWidth(initialWidth);
        studentsTable.setPrefHeight(MARGIN * 15);
        AnchorPane.setTopAnchor(studentsTable, MARGIN * 5.25);
        AnchorPane.setLeftAnchor(studentsTable, MARGIN);

        this.prefWidthProperty().addListener(((observable, oldValue, newValue) ->
            studentsTable.setPrefWidth(
                studentsTable.getPrefWidth() + IActivity.offset(oldValue, newValue)
            )
        ));

        this.prefHeightProperty().addListener(((observable, oldValue, newValue) ->
            studentsTable.setPrefHeight(
                studentsTable.getPrefHeight() + (Double) newValue - (Double) oldValue
            )
        ));

        TableColumn<String, StudentVM> uniqueIdCol = new TableColumn<>("ID");
        uniqueIdCol.setCellValueFactory(new PropertyValueFactory<>("uniqueId"));

        TableColumn<String, StudentVM> rankingCol = new TableColumn<>("Skill Rankings");

        TableColumn<String, StudentVM> rankACol = new TableColumn<>("A");
        rankACol.setCellValueFactory(new PropertyValueFactory<>("aVal"));

        TableColumn<String, StudentVM> rankNCol = new TableColumn<>("N");
        rankNCol.setCellValueFactory(new PropertyValueFactory<>("nVal"));

        TableColumn<String, StudentVM> rankPCol = new TableColumn<>("P");
        rankPCol.setCellValueFactory(new PropertyValueFactory<>("pVal"));

        TableColumn<String, StudentVM> rankWCol = new TableColumn<>("W");
        rankWCol.setCellValueFactory(new PropertyValueFactory<>("wVal"));

        rankingCol.getColumns().addAll(rankACol, rankNCol, rankPCol, rankWCol);

        TableColumn<String, StudentVM> personaCol = new TableColumn<>("Personality");
        personaCol.setCellValueFactory(new PropertyValueFactory<>("personality"));

        TableColumn<String, StudentVM> conflictCol = new TableColumn<>("Conflicters");
        conflictCol.setCellValueFactory(new PropertyValueFactory<>("conflicters"));

        ObservableList<StudentVM> studentsData = FXCollections.observableArrayList();
        for (Student student : students) {
            StudentVM studentVm = StudentVM.cast(student);
            studentsData.add(studentVm);
        }

        studentsTable.setItems(studentsData);
        studentsTable.getColumns().addAll(uniqueIdCol, rankingCol, personaCol, conflictCol);

        TableView.TableViewSelectionModel studentSelectionModel = studentsTable.getSelectionModel();
        studentSelectionModel.setSelectionMode(SelectionMode.SINGLE);

        studentSelectionModel.selectedIndexProperty().addListener((observable, oldValue, newValue) ->
            studentToAssign.set(students.get(newValue.intValue()))
        );
    }

    private void drawTeamsSelectionArea(List<Team> teams, double initialWidth, String action) {
        List<Team> assignableTeams = LogicalAssistant.filterAssignableTeams(teams);
        if (action == null && (teams.size() == 0 || assignableTeams.size() == 0)) {
            eraseTeamDetailsTable();
            drawTeamCreatingFragment(teams, initialWidth, true);
        }

        if (action != null && action.equals(SET_PROJECT)) {
            IActivity.changeElementText(
                Label.class, "Set a Project for newly created Team", "select-team-title", this
            );
            List<Project> projects = (new ProjectService()).readAllProjectsFromFile();

            String errorMessage = null;
            if (projects == null) errorMessage = "An error occurred while reading Project data from file.\nPlease try again.";
            if (projects != null && projects.size() == 0) errorMessage = "No Project has ever added. Please add at least 1 Project first.";

            if (errorMessage != null) drawProjectErrorFragment(errorMessage, initialWidth);
            else drawProjectSelectionFragment(teams, projects, initialWidth);
        }

        if (action == null && teams.size() != 0)
            drawTeamSelectionFragment(teams, initialWidth, teamToReceiveMember);
    }

    private void drawTeamSelectionFragment(List<Team> teams, double initialWidth, ObservableValue teamToReceive) {
        IActivity.removeElementIfExists("team-dropdown-select", this);
        IActivity.changeElementText(
            Label.class, "Select 1 Team to receive Student", "select-team-title", this
        );
        IActivity.removeElementIfExists("set-project-button", this);
        IActivity.removeElementIfExists("project-selection-dropdown", this);

        ComboBox<String> teamDropdown = new ComboBox<>();
        teamDropdown.setId("team-dropdown-select");
        teamDropdown.getStyleClass().add("dropdown-select");
        this.getChildren().add(teamDropdown);

        List<String> dropdownItems = new ArrayList<String>() {{ add("Select team"); }};
        List<Team> assignableTeams = LogicalAssistant.filterAssignableTeams(teams);
        assignableTeams.forEach(t -> dropdownItems.add(t.compact()));

        //To get the newly created Team if user have created one, otherwise, to get a selected Team from dropdown later
        Team selectedTeam = null;
        if (teamToReceive != null && teamToReceive.getValue() != null) selectedTeam = (Team) teamToReceive.getValue();

        teamDropdown.getItems().addAll(dropdownItems);
        teamDropdown.setValue(selectedTeam == null ? dropdownItems.get(0) : dropdownItems.get(assignableTeams.indexOf(selectedTeam) + 1));

        teamDropdown.setPrefWidth(initialWidth);
        AnchorPane.setTopAnchor(teamDropdown, MARGIN * 5.25);
        AnchorPane.setLeftAnchor(teamDropdown, initialWidth + MARGIN * 2);

        this.prefWidthProperty().addListener(((observable, oldValue, newValue) -> {
            double offset = IActivity.offset(oldValue, newValue);

            AnchorPane.setLeftAnchor(teamDropdown, AnchorPane.getLeftAnchor(teamDropdown) + offset);
            teamDropdown.setPrefWidth(teamDropdown.getPrefWidth() + offset);
        }));

        teamDropdown.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 0) {
                setTeamToReceiveMember(null);
                studentInTeamToBeReplaced.set(null);

                eraseTeamDetailsTable();
                drawTeamCreatingFragment(teams, initialWidth, false);
            }
            else {
                IActivity.removeElementIfExists("create-team-button", this);
                setTeamToReceiveMember(assignableTeams.get(newValue.intValue() - 1));
                teamDropdown.setValue(dropdownItems.get(newValue.intValue()));

                drawTeamSelectionFragment(teams, initialWidth, teamToReceiveMember);
            }
        }));

        if (selectedTeam == null) drawTeamCreatingFragment(teams, initialWidth, false);
        if (selectedTeam != null) {
            if (selectedTeam.isNewlyAdded()) teamDropdown.setDisable(true);
            drawSelectedTeamDetailsTable(selectedTeam, initialWidth);
        }
    }

    private void drawTeamCreatingFragment(List<Team> teams, double initialWidth, boolean isCreatingFirstTeam) {
        IActivity.changeElementText(
            Label.class,
            isCreatingFirstTeam ?
                "No Team is assignable or has ever been created yet. Please create a Team to receive Student.\n\n" +
                "Click \"Create\" button if you wish to proceed with creating the first Team. Otherwise, navigate back to Launch menu."
                : "Select 1 Team to receive Student",
            "select-team-title", this
        );

        Label optional = new Label("Or you can create a new Team to take the assigned Student.");
        optional.setId("optional-message");
        if (!isCreatingFirstTeam) {
            optional.getStyleClass().add("data-table");
            optional.setPrefWidth(initialWidth);
            this.getChildren().add(optional);

            AnchorPane.setTopAnchor(optional, MARGIN * 8.5);
            AnchorPane.setLeftAnchor(optional, initialWidth + MARGIN * 2);
        }

        Button createButton = new Button("Create");
        createButton.setId("create-team-button");
        createButton.setPrefWidth(MARGIN * 5);
        createButton.getStyleClass().add("done-button");
        this.getChildren().add(createButton);

        AnchorPane.setTopAnchor(createButton, MARGIN * 10);
        AnchorPane.setLeftAnchor(createButton, initialWidth + MARGIN * 2);

        this.prefWidthProperty().addListener((observable, oldValue, newValue) -> {
            double offset = IActivity.offset(oldValue, newValue);

            AnchorPane.setLeftAnchor(createButton, AnchorPane.getLeftAnchor(createButton) + offset);
            if (!isCreatingFirstTeam)
                AnchorPane.setLeftAnchor(optional, AnchorPane.getLeftAnchor(optional) + offset);
        });

        createButton.setOnAction(event -> {
            Team newTeam = new Team();
            newTeam.setNewlyAdded(true);
            teams.add(newTeam);

            setTeamToReceiveMember(newTeam);
            drawTeamsSelectionArea(teams, initialWidth, SET_PROJECT);
        });
    }

    private void drawProjectSelectionFragment(List<Team> teams, List<Project> projects, double initialWidth) {
        IActivity.removeElementIfExists("create-team-button", this);
        IActivity.removeElementIfExists("optional-message", this);

        AtomicInteger teamIndexToSetProject = new AtomicInteger();
        teams.forEach(team -> { if (team.isNewlyAdded()) teamIndexToSetProject.set(teams.indexOf(team)); });

        ComboBox<String> projectDropdown = new ComboBox<>();
        projects.forEach(p -> projectDropdown.getItems().add(p.compact()));

        projectDropdown.setId("project-selection-dropdown");
        projectDropdown.getStyleClass().add("dropdown-select");
        this.getChildren().add(projectDropdown);

        projectDropdown.setPrefWidth(initialWidth);
        AnchorPane.setTopAnchor(projectDropdown, MARGIN * 5.25);
        AnchorPane.setLeftAnchor(projectDropdown, initialWidth + MARGIN * 2);

        Button setProjectButton = new Button("Set");
        setProjectButton.setId("set-project-button");
        this.getChildren().add(setProjectButton);

        setProjectButton.setPrefWidth(MARGIN * 5);
        AnchorPane.setTopAnchor(setProjectButton, MARGIN * 8);
        AnchorPane.setLeftAnchor(setProjectButton, initialWidth + MARGIN * 2);

        Label projectError = new Label("No Project selected. Please select 1 in dropdown.");
        projectError.setVisible(false);
        projectError.setPrefWidth(initialWidth);
        projectError.getStyleClass().add("message");
        this.getChildren().add(projectError);

        AnchorPane.setTopAnchor(projectError, MARGIN * 11);
        AnchorPane.setLeftAnchor(projectError, initialWidth + MARGIN * 2);

        this.prefWidthProperty().addListener(((observable, oldValue, newValue) -> {
            double offset = IActivity.offset(oldValue, newValue);

            AnchorPane.setLeftAnchor(projectDropdown, AnchorPane.getLeftAnchor(projectDropdown) + offset);
            AnchorPane.setLeftAnchor(setProjectButton, AnchorPane.getLeftAnchor(setProjectButton) + offset);
            AnchorPane.setLeftAnchor(projectError, AnchorPane.getLeftAnchor(projectError) + offset);
        }));

        setProjectButton.setOnAction(event -> {
            int selectedProjectIndex = projectDropdown.getSelectionModel().getSelectedIndex();
            if (selectedProjectIndex < 0)
                projectError.setVisible(true);
            else {
                projectError.setVisible(false);
                Project selectedProject = projects.get(selectedProjectIndex);

                teams.get(teamIndexToSetProject.get()).setProject(selectedProject);
                drawTeamsSelectionArea(teams, initialWidth, null);
            }
        });
    }

    private void drawProjectErrorFragment(String errorMessage, double initialWidth) {
        IActivity.toggleElement("assign-button", this);

        Label errorLabel = new Label(errorMessage);
        errorLabel.getStyleClass().add("message");
        errorLabel.setPrefWidth(initialWidth);
        this.getChildren().add(errorLabel);

        AnchorPane.setTopAnchor(errorLabel, MARGIN * 5.25);
        AnchorPane.setLeftAnchor(errorLabel, initialWidth + MARGIN * 2);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("done-button");
        this.getChildren().add(backButton);

        backButton.setPrefWidth(MARGIN * 5);
        AnchorPane.setTopAnchor(backButton, MARGIN * 9);
        AnchorPane.setLeftAnchor(backButton, initialWidth + MARGIN * 2);

        backButton.setOnAction(event -> intent.accept(SharedEnums.GUI_ACTION_CONTEXT.LAUNCH));

        this.prefWidthProperty().addListener((observable, oldValue, newValue) -> {
            double offset = IActivity.offset(oldValue, newValue);

            AnchorPane.setLeftAnchor(backButton, ( AnchorPane.getLeftAnchor(backButton) + offset));
            AnchorPane.setLeftAnchor(errorLabel, ( AnchorPane.getLeftAnchor(errorLabel) + offset));
        });
    }

    private void drawTextLabel(double initialWidth, Label errorLabel) {
        this.getChildren().add(errorLabel);

        AnchorPane.setTopAnchor(errorLabel, MARGIN * 7);
        AnchorPane.setLeftAnchor(errorLabel, initialWidth + MARGIN * 2);

        this.prefWidthProperty().addListener((observable, oldValue, newValue) ->
            AnchorPane.setLeftAnchor(
                errorLabel,
                AnchorPane.getLeftAnchor(errorLabel) + IActivity.offset(oldValue, newValue)
            )
        );
    }

    private void drawSelectedTeamDetailsTable(@NotNull Team team, double initialWidth) {
        IActivity.removeElementIfExists("optional-message", this);
        eraseTeamDetailsTable();

        Label selectedTeamLabel = new Label("Your selected Team:");
        selectedTeamLabel.setId("selected-team-label");
        selectedTeamLabel.getStyleClass().add("subtitle");
        drawTextLabel(initialWidth, selectedTeamLabel);

        GridPane teamBasicInfo = new GridPane();
        teamBasicInfo.setId("team-basic-info");
        this.getChildren().add(teamBasicInfo);

        AnchorPane.setTopAnchor(teamBasicInfo, MARGIN * 8.5);
        AnchorPane.setLeftAnchor(teamBasicInfo, initialWidth + MARGIN * 2);

        RowConstraints rowConstraint = new RowConstraints();
        rowConstraint.setPrefHeight(MARGIN * 1.25);
        teamBasicInfo.getRowConstraints().add(rowConstraint);

        teamBasicInfo.setVgap(MARGIN * 0.5);
        teamBasicInfo.setPrefWidth(initialWidth);

        List<Label> infoLabels = new ArrayList<Label>() {{
            add(new Label("Team ID:"));
            add(new Label(team.getId() == 0 ? "(new)" : team.getId() + ""));
            add(new Label("Project:"));
            add(new Label(team.getProject().getUniqueId()));
        }};

        for (int i = 0; i < 4; i++) {
            ColumnConstraints colConstraint = new ColumnConstraints();
            colConstraint.setPercentWidth((initialWidth - MARGIN * 0.5 * 3)/4);
            teamBasicInfo.getColumnConstraints().add(colConstraint);

            infoLabels.get(i).getStyleClass().add(i % 2 == 0 ? "grid-label-h" : "grid-label");
            teamBasicInfo.add(infoLabels.get(i), i, 0);
        }

        Label linkText = new Label("Click here to see Fitness Metrics.");
        linkText.setId("metrics-link-text");
        if (team.getFitnessMetrics() != null) {
            linkText.getStyleClass().add("link-text");
            this.getChildren().add(linkText);

            AnchorPane.setTopAnchor(linkText, MARGIN * 10);
            AnchorPane.setLeftAnchor(linkText, initialWidth + MARGIN * 2);

            linkText.setOnMouseClicked(event -> launchFitnessMetricsPopup(team.getFitnessMetrics()));
        }

        Label memberSelectLabel = new Label("Members:");
        memberSelectLabel.setId("members-select-label");
        memberSelectLabel.getStyleClass().add("grid-label-h");
        this.getChildren().add(memberSelectLabel);

        AnchorPane.setTopAnchor(memberSelectLabel, MARGIN * (team.getFitnessMetrics() == null ? 10 : 11.5));
        AnchorPane.setLeftAnchor(memberSelectLabel, initialWidth + MARGIN * 2);

        ComboBox<String> membersDropdown = new ComboBox();
        membersDropdown.setId("members-dropdown");
        membersDropdown.getStyleClass().add("dropdown-select");
        this.getChildren().add(membersDropdown);

        membersDropdown.setPrefWidth(initialWidth);
        AnchorPane.setTopAnchor(membersDropdown, MARGIN * (team.getFitnessMetrics() == null ? 11.5 : 13));
        AnchorPane.setLeftAnchor(membersDropdown, initialWidth + MARGIN * 2);

        List<String> memberItems = new ArrayList<String>() {{ add("No selection"); }};
        team.getMembers().forEach(m -> memberItems.add(m.display()));

        membersDropdown.getItems().addAll(memberItems);
        membersDropdown.setValue(memberItems.get(0));

        Label guidance = new Label("If you want the assigned Student to replace a member in Team, select the member to be replaced.");
        guidance.setId("guidance-label");
        guidance.getStyleClass().add("data-table");
        guidance.setPrefWidth(initialWidth);
        this.getChildren().add(guidance);

        AnchorPane.setTopAnchor(guidance, MARGIN * (team.getFitnessMetrics() == null ? 13.5 : 16));
        AnchorPane.setLeftAnchor(guidance, initialWidth + MARGIN * 2);

        Label selectedMemberLabel = new Label();
        selectedMemberLabel.setId("selected-member-label");
        selectedMemberLabel.setVisible(false);
        selectedMemberLabel.setPrefWidth(initialWidth);
        selectedMemberLabel.getStyleClass().add("data-table");
        this.getChildren().add(selectedMemberLabel);

        AnchorPane.setTopAnchor(selectedMemberLabel, MARGIN * (team.getFitnessMetrics() == null ? 13.5 : 16));
        AnchorPane.setLeftAnchor(selectedMemberLabel, initialWidth + MARGIN * 2);

        membersDropdown.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 0) {
                guidance.setVisible(true);
                selectedMemberLabel.setVisible(false);
                studentInTeamToBeReplaced.set(null);
            }
            else {
                guidance.setVisible(false);

                Student selectedMember = team.getMembers().get(newValue.intValue() - 1);
                selectedMemberLabel.setText("**Member to be replaced:\n#" + selectedMember.display());

                selectedMemberLabel.setVisible(true);
                studentInTeamToBeReplaced.set(selectedMember);
            }
        });

        this.prefWidthProperty().addListener((observable, oldValue, newValue) -> {
            double offset = IActivity.offset(oldValue, newValue);

            membersDropdown.setPrefWidth(membersDropdown.getPrefWidth() + offset);
            AnchorPane.setLeftAnchor(membersDropdown, AnchorPane.getLeftAnchor(membersDropdown) + offset);
            AnchorPane.setLeftAnchor(teamBasicInfo, AnchorPane.getLeftAnchor(teamBasicInfo) + offset);
            AnchorPane.setLeftAnchor(guidance, AnchorPane.getLeftAnchor(guidance) + offset);
            AnchorPane.setLeftAnchor(memberSelectLabel, AnchorPane.getLeftAnchor(memberSelectLabel) + offset);
            selectedMemberLabel.setPrefWidth(selectedMemberLabel.getPrefWidth() + offset);
            AnchorPane.setLeftAnchor(selectedMemberLabel, AnchorPane.getLeftAnchor(selectedMemberLabel) + offset);
            if (team.getFitnessMetrics() != null)
                AnchorPane.setLeftAnchor(linkText, AnchorPane.getLeftAnchor(linkText) + offset);
        });
    }

    private void eraseTeamDetailsTable() {
        IActivity.removeElementIfExists("selected-team-label", this);
        IActivity.removeElementIfExists("team-basic-info", this);
        IActivity.removeElementIfExists("metrics-link-text", this);
        IActivity.removeElementIfExists("members-select-label", this);
        IActivity.removeElementIfExists("members-dropdown", this);
        IActivity.removeElementIfExists("guidance-label", this);
        IActivity.removeElementIfExists("selected-member-label", this);
    }

    private void launchFitnessMetricsPopup(@NotNull TeamFitness metrics) {
        final Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setResizable(false);
        popup.setTitle("Fitness Metrics");

        popup.fullScreenProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) popup.setFullScreen(false);
        }));

        final double POPUP_WIDTH = SharedConstants.DIMENSIONS.get("WIDTH") * 0.4;
        final double POPUP_HEIGHT = SharedConstants.DIMENSIONS.get("HEIGHT") * 0.4;

        AnchorPane popActivity = new AnchorPane();
        Scene popScene = new Scene(popActivity, POPUP_WIDTH, POPUP_HEIGHT);
        popActivity.setPrefSize(POPUP_WIDTH, POPUP_HEIGHT);

        Label title = new Label("Fitness Metrics");
        title.getStyleClass().add("popup-title");
        title.setPrefWidth(POPUP_WIDTH);
        AnchorPane.setTopAnchor(title, MARGIN / 2);

        Label competencyHeading = new Label("Skill Competencies");
        competencyHeading.getStyleClass().add("popup-heading");

        AnchorPane.setTopAnchor(competencyHeading, MARGIN);
        AnchorPane.setLeftAnchor(competencyHeading, MARGIN);

        GridPane competencyVals = new GridPane();
        competencyVals.setPrefWidth(POPUP_WIDTH - MARGIN * 2);

        AnchorPane.setTopAnchor(competencyVals, MARGIN * 1.5);
        AnchorPane.setLeftAnchor(competencyVals, MARGIN);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPrefHeight(MARGIN * 0.6);
        competencyVals.getRowConstraints().add(rowConstraints);
        competencyVals.setVgap(MARGIN / 4);

        List<Label> competencyLabels = new ArrayList<Label>() {{
            add(new Label("AVG. " + metrics.getAverageTeamSkillCompetency()));
            add(new Label(SharedEnums.SKILLS.A.name() + "(" + metrics.getTeamCompetency().get(SharedEnums.SKILLS.A) + ")"));
            add(new Label(SharedEnums.SKILLS.N.name() + "(" + metrics.getTeamCompetency().get(SharedEnums.SKILLS.N) + ")"));
            add(new Label(SharedEnums.SKILLS.P.name() + "(" + metrics.getTeamCompetency().get(SharedEnums.SKILLS.P) + ")"));
            add(new Label(SharedEnums.SKILLS.W.name() + "(" + metrics.getTeamCompetency().get(SharedEnums.SKILLS.W) + ")"));
        }};

        ColumnConstraints colConstraints = new ColumnConstraints();
        for (int i = 0; i < 5; i++) {
            colConstraints.setPercentWidth((competencyVals.getPrefWidth() - competencyVals.getVgap() * 4) / 5);
            competencyVals.getColumnConstraints().add(colConstraints);

            competencyLabels.get(i).getStyleClass().add("popup-text");
            competencyVals.add(competencyLabels.get(i), i, 0);
        }

        Label preferenceHeading = new Label("Preference Satisfactions");
        preferenceHeading.getStyleClass().add("popup-heading");

        AnchorPane.setTopAnchor(preferenceHeading, MARGIN * 2.25);
        AnchorPane.setLeftAnchor(preferenceHeading, MARGIN);

        GridPane preferenceVals = new GridPane();
        preferenceVals.setVgap(MARGIN / 4);
        preferenceVals.setPrefWidth(POPUP_WIDTH - MARGIN * 2);
        preferenceVals.getRowConstraints().add(rowConstraints);

        AnchorPane.setTopAnchor(preferenceVals, MARGIN * 2.75);
        AnchorPane.setLeftAnchor(preferenceVals, MARGIN);

        List<Label> preferenceLabels = new ArrayList<Label>() {{
            add(new Label("AVG. " + metrics.getPreferenceSatisfaction().getKey()));
            add(new Label("(1st) " + metrics.getPreferenceSatisfaction().getValue().getKey()));
            add(new Label("(2nd) " + metrics.getPreferenceSatisfaction().getValue().getValue()));
        }};

        for (int i = 0; i < 3; i++) {
            colConstraints.setPercentWidth((preferenceVals.getPrefWidth() - preferenceVals.getVgap() * 2) / 3);
            preferenceVals.getColumnConstraints().add(colConstraints);

            preferenceLabels.get(i).getStyleClass().add("popup-text");
            preferenceVals.add(preferenceLabels.get(i), i, 0);
        }

        Label shortfallHeading = new Label("Skill Shortfalls");
        shortfallHeading.getStyleClass().add("popup-heading");

        AnchorPane.setTopAnchor(shortfallHeading, MARGIN * 3.5);
        AnchorPane.setLeftAnchor(shortfallHeading, MARGIN);

        GridPane shortfallVals = new GridPane();
        shortfallVals.setVgap(MARGIN / 4);
        shortfallVals.setPrefWidth(POPUP_WIDTH - MARGIN * 2);
        shortfallVals.getRowConstraints().add(rowConstraints);

        AnchorPane.setTopAnchor(shortfallVals, MARGIN * 4);
        AnchorPane.setLeftAnchor(shortfallVals, MARGIN);

        Label avgSfLabel = new Label("AVG. " + metrics.getAverageSkillShortfall());
        avgSfLabel.getStyleClass().add("popup-text");
        Label projSfLabel = new Label("Team Project: " + metrics.getPreferenceSatisfaction().getValue().getValue());
        projSfLabel.getStyleClass().add("popup-text");

        colConstraints.setPercentWidth((shortfallVals.getPrefWidth() - shortfallVals.getVgap()) / 2);
        shortfallVals.getColumnConstraints().addAll(colConstraints, colConstraints);

        shortfallVals.add(avgSfLabel, 0, 0);
        shortfallVals.add(projSfLabel, 1, 0);

        Button okButton = new Button("Okay");
        okButton.getStyleClass().add("popup-button");
        okButton.setPrefWidth(MARGIN * 4);

        AnchorPane.setBottomAnchor(okButton, MARGIN / 2);
        AnchorPane.setLeftAnchor(okButton, (POPUP_WIDTH - okButton.getPrefWidth()) / 2);

        popActivity.getChildren().addAll(
            title, competencyHeading, competencyVals, preferenceHeading,
            preferenceVals, shortfallHeading, shortfallVals, okButton
        );
        popup.setScene(popScene);
        popup.show();
    }

    private void drawButtonBasedOnContext(Scene container, boolean isErrorOccurred) {
        Button backButton = new Button(isErrorOccurred ? "Okay" : "Back");
        Button assignButton = new Button("Assign");

        IActivity.drawActivityFixedButtons(container, this, isErrorOccurred, backButton, assignButton);
        setActionListenerFor(container, assignButton);

        backButton.setOnAction(event -> {
            teamToReceiveMember.set(null);
            studentInTeamToBeReplaced.set(null);
            studentToAssign.set(null);
            intent.accept(SharedEnums.GUI_ACTION_CONTEXT.LAUNCH);
        });
    }

    private void setActionListenerFor(Scene container, Button assignButton) {
        final List<Project> projects = (new ProjectService()).readAllProjectsFromFile();
        final List<Preference> preferences = studentService.readAllStudentPreferencesFromFile();

        if (projects == null || preferences == null)
            drawActivityFailMessage(container, "An error occurred while retrieving data from files.\nPlease try again.");

        assignButton.setOnAction(event -> {
            if (studentInTeamToBeReplaced.get() != null)
                teamToReceiveMember.get().getMembers().remove(studentInTeamToBeReplaced.get());

            teamToReceiveMember.get().getMembers().add(studentToAssign.get());

            if (teamToReceiveMember.get().getMembers().size() == SharedConstants.GROUP_LIMIT)
                teamToReceiveMember.get().setFitnessMetrics(
                    controllerBase.calculateTeamFitnessMetricsFor(teamToReceiveMember.get(), projects, preferences)
                );

            boolean updateSuccess = false;
            int newTeamId = -1;
            if (teamToReceiveMember.get().isNewlyAdded()) newTeamId = teamService.SaveNewTeam(teamToReceiveMember.get());
            else updateSuccess = teamService.updateTeam(teamToReceiveMember.get());

            if ((!teamToReceiveMember.get().isNewlyAdded() && !updateSuccess) ||
                (teamToReceiveMember.get().isNewlyAdded() && newTeamId < 0)
            ) drawActivityFailMessage(container, "An error occurred while updating/saving data into files.\nPlease retry your task.");

            if (teamToReceiveMember.get().isNewlyAdded() && newTeamId != -1) {
                teamToReceiveMember.get().setId(newTeamId);
                teamToReceiveMember.get().setNewlyAdded(false);
                eraseTeamDetailsTable();
                drawAssigningTaskContents(container, "The selected Student has been assigned to team successfully.");
            }

            if (updateSuccess) drawAssigningTaskContents(container, "The selected Student has been assigned to team successfully.");
        });
    }

    @Override
    public <T> void setIntent(Consumer<T> intent) {
        this.intent = (Consumer<SharedEnums.GUI_ACTION_CONTEXT>) intent;
    }
}
