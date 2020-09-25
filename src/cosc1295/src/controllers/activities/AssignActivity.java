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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes"})
/**
 * This Activity allows for assigning a Student to a Team.
 * While assigning, user can choose to create a new Team to receive the assignee.
 * Also, user can select a Student in the Team to be replaced by the assignee.
 * Teams having 4 Students can not receive more Student, and if a Student in a Team is replaced
 * by an assignee, the replaced Student goes back to the assignable Student list.
 * When user select a Student and a Team, and optionally the Student in the Team to be replaced,
 * a message will display to suggest the requirements of the Team on the assignee.
 * If the assignee meets all requirements (Leader or personality balancing, and conflicters),
 * the `Assign` button will be enabled (clickable), otherwise disabled.
 * The design of this feature utilizes recursive calls to reduce the amount of codes and eliminate redundant codes.
 * To simplify this feature, the assignable Teams are ones that have 0-3 members despite members can be replaced.
 * I have created another activity to remove Team member.
 */
public class AssignActivity extends AnchorPane implements IActivity {

    private Consumer<SharedEnums.GUI_ACTION_CONTEXT> intent; //The Intent object for navigation
    private static final String SET_PROJECT = "SET_PROJECT";

    //Dependency injections to access data processing services
    private final StudentService studentService;
    private final TeamService teamService;
    private final ControllerBase controllerBase;

    //The observable objects to keep track of the changes made to data
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

    /**
     * Entry point to this Activity.
     * Data are read and errors are checked in preparation for the features.
     * Only on no error, the widgets are shown and the listeners are attached to the observables.
     * @param container Scene
     * @param postMessage String
     */
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
            IActivity.drawSuccessMessage("This is a success message for a testing.", this);
        }
    }

    private void attachListenersToObservables(Scene container) {
        SimpleBooleanProperty shouldEnableAssignButton = new SimpleBooleanProperty(false);
        //The Team's requirements on the assignee will be stringified into this variable, then displayed.
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

        Student selectedStudent = studentToAssign.get(); //The assignee selected by user from assignable Student list
        if (selectedStudent == null) shouldEnableAssignButton.set(false);

        //Only check Team's requirements if both assignee and the Team to receive assignee are selected
        if (selectedStudent != null && teamToReceiveMember.get() != null) {
            //Get the members currently in the Team
            List<Student> receivingTeamMembers = new ArrayList<>(teamToReceiveMember.get().getMembers());

            //Check if user want to replace a member in Team, then produce the Team's requirements
            if (studentInTeamToBeReplaced.get() != null) receivingTeamMembers.remove(studentInTeamToBeReplaced.get());
            Pair<Boolean, List<String>> teamRequirements = LogicalAssistant.produceTeamRequirementsOnNewMember(
                receivingTeamMembers, new ArrayList<Student>() {{ add(studentToAssign.get()); }}
            );

            //Null requirements means the assignee is good to join the Team
            if (teamRequirements == null) shouldEnableAssignButton.set(true);
            else {
                //Otherwise, check the requirements for the unaccepted conditions
                if (teamRequirements.getKey() && teamToReceiveMember.get().getMembers().size() < SharedConstants.GROUP_LIMIT - 1) {
                    //Team needs Leader but still has rooms for other members so Leader can be deferred
                    message.set("Leader is still missing in this Team.");
                    shouldEnableAssignButton.set(true);
                }
                else {
                    //Unsafe to assign: either Leader required, or conflicts, or imbalance personality.
                    shouldEnableAssignButton.set(false);

                    if (teamRequirements.getKey()) message.set(message.get() + "Leader must be assigned to this Team.");
                    if (teamRequirements.getValue() != null &&
                            teamRequirements.getValue().contains(selectedStudent.getUniqueId())
                    ) message.set(message.get() + " Member conflicts occur.");
                }
            }

            //Done checking the requirements, display a message to inform user so they can adjust their selection
            if (!message.get().equals(SharedConstants.EMPTY_STRING))
                drawTeamRequirementMessage(container, message.get());
            else shouldEnableAssignButton.set(true);
        }
    }

    /**
     * Draws the Team's requirements in the Activity so user is informed whether
     * the assignee is good to assign, or what goes wrong so they can adjust accordingly.
     * The new message replaces the old one.
     * Also constraints the message for its size and position on the Activity.
     * @param container Scene
     * @param message String
     */
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

    /**
     * Draws the titles on Activity to let user know where to select what.
     * Draws 2 areas for assignee selection and for team selection by calling 2 other methods.
     * @param container Scene
     * @param students List<Student>
     * @param teams List<Team>
     */
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

    /**
     * Draws a TableView widget to display the assignable Students and allow for
     * selecting an assignee. The selected assignee will be set to the observable studentToAssign.
     * @param students List<Student>
     * @param initialWidth double
     */
    private void drawStudentsSelectionArea(List<Student> students, double initialWidth) {
        TableView studentsTable = new TableView(); //Create a Table view
        studentsTable.getStyleClass().add("data-table");
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.getChildren().add(studentsTable);

        studentsTable.setPrefWidth(initialWidth);
        studentsTable.setPrefHeight(MARGIN * 15);
        AnchorPane.setTopAnchor(studentsTable, MARGIN * 5.25);
        AnchorPane.setLeftAnchor(studentsTable, MARGIN);

        //Constraint the Table view in AnchorPane
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

        //Add the Table Columns
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

        //Attach observables and selection listener to Table Rows
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

    /**
     * Control the application flow when selecting a Team to receive member, or creating a new Team.
     * This method is recursively called after every change that is made to the parameters' data.
     * The exit point of recursion is when a Team is selected, optionally a Team's member to be replaced.
     * Param `action` == null when this method is reached from method `drawWidgetsForAssigningStudentsTask`,
     * or when there is at least 1 Team to be selected while user modify data.
     * @param teams List<Team>
     * @param initialWidth double
     * @param action String
     */
    private void drawTeamsSelectionArea(List<Team> teams, double initialWidth, String action) {
        List<Team> assignableTeams = LogicalAssistant.filterAssignableTeams(teams);
        //When no Team is ever created, or no assignable Teams, user have to create a new Team
        if (action == null && (teams.size() == 0 || assignableTeams.size() == 0)) {
            eraseTeamDetailsTable();
            IActivity.removeElementIfExists("team-dropdown-select", this);
            drawTeamCreatingFragment(teams, initialWidth, true);
        }

        //This condition works in accordance with the above condition because action != null
        //This time, this method is called in a recursion (a team is being created and needed to set a project).
        //After user create a new Team, they have to set a project for that newly created Team.
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

        //This condition can only be reached if both of the above `if` conditions are false.
        //This condition can be reached in the first method call (there are at least 1 assignable Team) or in a recursion (a team has been created).
        if (action == null && assignableTeams.size() != 0)
            drawTeamSelectionFragment(teams, initialWidth, teamToReceiveMember);
    }

    /**
     * Draws a ComboBox widget allowing for selecting a Team to receive member.
     * The selected Team will be set to observable teamToReceiveStudent.
     * When a Team is selected, another fragment is drawn to allow for selecting a member to be replaced.
     * If a member is selected, it is set to observable studentToBeReplaced.
     * Param teamToReceive is to counter the situation when user has created a new Team,
     * and this method is reached (not called) afterwards.
     * @param teams List<Team>
     * @param initialWidth double
     * @param teamToReceive ObservableValue
     */
    private void drawTeamSelectionFragment(List<Team> teams, double initialWidth, ObservableValue teamToReceive) {
        IActivity.removeElementIfExists("team-dropdown-select", this);
        IActivity.changeElementText(
            Label.class, "Select 1 Team to receive Student", "select-team-title", this
        );
        IActivity.removeElementIfExists("set-project-button", this);
        IActivity.removeElementIfExists("project-selection-dropdown", this);

        //The ComboBox for selecting a Team
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

        //Set dropdown values and default item to teamDropdown ComboBox
        teamDropdown.getItems().addAll(dropdownItems);
        teamDropdown.setValue(
            selectedTeam == null || !assignableTeams.contains(selectedTeam)
                ? dropdownItems.get(0)
                : dropdownItems.get(assignableTeams.indexOf(selectedTeam) + 1)
        );

        teamDropdown.setPrefWidth(initialWidth);
        AnchorPane.setTopAnchor(teamDropdown, MARGIN * 5.25);
        AnchorPane.setLeftAnchor(teamDropdown, initialWidth + MARGIN * 2);

        this.prefWidthProperty().addListener(((observable, oldValue, newValue) -> {
            double offset = IActivity.offset(oldValue, newValue);

            AnchorPane.setLeftAnchor(teamDropdown, AnchorPane.getLeftAnchor(teamDropdown) + offset);
            teamDropdown.setPrefWidth(teamDropdown.getPrefWidth() + offset);
        }));

        teamDropdown.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 0) { //No team is selected, then...
                setTeamToReceiveMember(null); //Remove the selected Team in observable
                studentInTeamToBeReplaced.set(null); //Remove the replaced member no matter it is selected or not

                eraseTeamDetailsTable(); //No Team to display this fragment any longer
                drawTeamCreatingFragment(teams, initialWidth, false); //Re-allow user to create new Team
            }
            else { //A Team has been selected
                IActivity.removeElementIfExists("create-team-button", this);
                setTeamToReceiveMember(assignableTeams.get(newValue.intValue() - 1));
                teamDropdown.setValue(dropdownItems.get(newValue.intValue()));

                //Recursive call to this method, so it will reach the below codes on line 432
                drawTeamSelectionFragment(teams, initialWidth, teamToReceiveMember);
            }
        }));

        //Reached in a recursion: if a Team is selected or created, display the details of Team
        //If no Team is selected, recursively call drawTeamCreatingFragment to redraw the whole area.
        if (selectedTeam == null) drawTeamCreatingFragment(teams, initialWidth, false);
        if (selectedTeam != null) {
            if (selectedTeam.isNewlyAdded()) teamDropdown.setDisable(true);
            drawSelectedTeamDetailsTable(selectedTeam, initialWidth);
        }
    }

    /**
     * Draws the widgets for creating new Team. After creating the Team, set it to observable teamToReceiveMember,
     * then recursively call to drawTeamsSelectionArea with action==SET_PROJECT to set a Team Project.
     * @param teams List<Team>
     * @param initialWidth double
     * @param isCreatingFirstTeam boolean
     */
    private void drawTeamCreatingFragment(List<Team> teams, double initialWidth, boolean isCreatingFirstTeam) {
        IActivity.changeElementText(
            Label.class,
            isCreatingFirstTeam ?
                "No Team is assignable or has ever been created yet. Please create a Team to receive Student.\n\n" +
                "Click \"Create\" button if you wish to proceed with creating a Team. Otherwise, navigate back to Launch menu."
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

        //On clicking this button, an event will fire to create new Team
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

    /**
     * Draws a ComboBox widget to allow for setting a Project to the newly created Team.
     * Then recursively call to drawTeamsSelectionArea with action==null to redraw the Team selection widgets.
     * @param teams List<Team>
     * @param projects List<Project>
     * @param initialWidth double
     */
    private void drawProjectSelectionFragment(List<Team> teams, List<Project> projects, double initialWidth) {
        //Remove unnecessary elements
        IActivity.removeElementIfExists("create-team-button", this);
        IActivity.removeElementIfExists("optional-message", this);

        //Get the newly created Team to later set a Project
        AtomicInteger teamIndexToSetProject = new AtomicInteger();
        teams.forEach(team -> { if (team.isNewlyAdded()) teamIndexToSetProject.set(teams.indexOf(team)); });

        //Create a dropdown to let user pick a Project
        ComboBox<String> projectDropdown = new ComboBox<>();
        projects.forEach(p -> projectDropdown.getItems().add(p.compact()));

        projectDropdown.setId("project-selection-dropdown");
        projectDropdown.getStyleClass().add("dropdown-select");
        this.getChildren().add(projectDropdown);

        projectDropdown.setPrefWidth(initialWidth);
        AnchorPane.setTopAnchor(projectDropdown, MARGIN * 5.25);
        AnchorPane.setLeftAnchor(projectDropdown, initialWidth + MARGIN * 2);

        //The button that when user click, the selected Peoject will be set for the newly created Team
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

        //Constraint the elements in AnchorPane
        AnchorPane.setTopAnchor(projectError, MARGIN * 11);
        AnchorPane.setLeftAnchor(projectError, initialWidth + MARGIN * 2);

        this.prefWidthProperty().addListener(((observable, oldValue, newValue) -> {
            double offset = IActivity.offset(oldValue, newValue);

            AnchorPane.setLeftAnchor(projectDropdown, AnchorPane.getLeftAnchor(projectDropdown) + offset);
            AnchorPane.setLeftAnchor(setProjectButton, AnchorPane.getLeftAnchor(setProjectButton) + offset);
            AnchorPane.setLeftAnchor(projectError, AnchorPane.getLeftAnchor(projectError) + offset);
        }));

        //Attach listener to the button
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

    /**
     * Called by the above drawProjectSelectionFragment method to display error message
     * when an exception occurred while retrieving projects from files.
     * @param errorMessage String
     * @param initialWidth double
     */
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

    /**
     * After a Team is selected, this method is called to draw the team details,
     * and a ComboBox allowing for selecting a Team member to be replaced.
     * @param team Team
     * @param initialWidth double
     */
    private void drawSelectedTeamDetailsTable(@NotNull Team team, double initialWidth) {
        IActivity.removeElementIfExists("optional-message", this);
        eraseTeamDetailsTable();

        //Drawing the Team detail labels
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

        //Draw the Team detail ID, Project
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

        //Drawing the widgets for selecting a member to replace
        Label memberSelectLabel = new Label("Members:");
        memberSelectLabel.setId("members-select-label");
        memberSelectLabel.getStyleClass().add("grid-label-h");
        this.getChildren().add(memberSelectLabel);

        AnchorPane.setTopAnchor(memberSelectLabel, MARGIN * (team.getFitnessMetrics() == null ? 10 : 11.5));
        AnchorPane.setLeftAnchor(memberSelectLabel, initialWidth + MARGIN * 2);

        ComboBox<String> membersDropdown = new ComboBox(); //Dropdown for Team members
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

        //If a Team member is selected, a section will display the information of the Student
        Label selectedMemberLabel = new Label();
        selectedMemberLabel.setId("selected-member-label");
        selectedMemberLabel.setVisible(false);
        selectedMemberLabel.setPrefWidth(initialWidth);
        selectedMemberLabel.getStyleClass().add("data-table");
        this.getChildren().add(selectedMemberLabel);

        AnchorPane.setTopAnchor(selectedMemberLabel, MARGIN * (team.getFitnessMetrics() == null ? 13.5 : 16));
        AnchorPane.setLeftAnchor(selectedMemberLabel, initialWidth + MARGIN * 2);

        //Attach listener to the Team members dropdown to collect the selected member that will be replaced
        membersDropdown.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 0) { //No member selected, set null to observable studentInTeamToBeReplaced
                guidance.setVisible(true);
                selectedMemberLabel.setVisible(false);
                studentInTeamToBeReplaced.set(null);
            }
            else { //A member selected, inform the details of the member to be replaced
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

    /**
     * Draws the `Back` button and/or Assign button
     * @param container Scene
     * @param isErrorOccurred boolean
     */
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

    /**
     * Sets the listener to the Assign button. On clicking, user commit to save their changes to data into files.
     * @param container Scene
     * @param assignButton Button
     */
    private void setActionListenerFor(Scene container, Button assignButton) {
        final List<Project> projects = (new ProjectService()).readAllProjectsFromFile();
        final List<Preference> preferences = studentService.readAllStudentPreferencesFromFile();

        if (projects == null || preferences == null)
            drawActivityFailMessage(container, "An error occurred while retrieving data from files.\nPlease try again.");

        assignButton.setOnAction(event -> {
            //If user want to replace a member in Team, remove that member first
            if (studentInTeamToBeReplaced.get() != null)
                teamToReceiveMember.get().getMembers().remove(studentInTeamToBeReplaced.get());

            //Add the assignee into Team
            teamToReceiveMember.get().getMembers().add(studentToAssign.get());

            //If after assigning, Team has 4 members, calculate the Fitness Metrics
            if (teamToReceiveMember.get().getMembers().size() == SharedConstants.GROUP_LIMIT)
                teamToReceiveMember.get().setFitnessMetrics(
                    controllerBase.calculateTeamFitnessMetricsFor(teamToReceiveMember.get(), projects, preferences)
                );

            //Finally save or update data
            boolean updateSuccess = false;
            int newTeamId = -1;
            if (teamToReceiveMember.get().isNewlyAdded()) newTeamId = teamService.SaveNewTeam(teamToReceiveMember.get());
            else updateSuccess = teamService.updateTeam(teamToReceiveMember.get());

            //Check the save/update results
            if ((!teamToReceiveMember.get().isNewlyAdded() && !updateSuccess) ||
                (teamToReceiveMember.get().isNewlyAdded() && newTeamId < 0)
            ) {
                assignButton.setVisible(false);
                drawActivityFailMessage(container, "An error occurred while updating/saving data into files.\nPlease retry your task.");
            }

            if (teamToReceiveMember.get().isNewlyAdded() && newTeamId != -1) {
                teamToReceiveMember.get().setId(newTeamId);
                teamToReceiveMember.get().setNewlyAdded(false);
                updateSuccess = true;
            }

            if (updateSuccess) {
                assignButton.setDisable(true);
                studentToAssign.set(null);
                teamToReceiveMember.set(null);
                studentInTeamToBeReplaced.set(null);
                eraseTeamDetailsTable();
                drawAssigningTaskContents(container, "The selected Student has been assigned to team successfully.");
            }
        });
    }

    private void drawActivityFailMessage(Scene container, String message) {
        IActivity.drawActivityMessageOnException(container, this, message);
        drawButtonBasedOnContext(container, true);
    }

    @Override
    public <T> void setIntent(Consumer<T> intent) {
        this.intent = (Consumer<SharedEnums.GUI_ACTION_CONTEXT>) intent;
    }
}
