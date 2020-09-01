package cosc1295.src.controllers.activities;

import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.views.gui.viewmodels.StudentVM;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import helpers.utilities.LogicalAssistant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.util.List;
import java.util.function.Consumer;

public class AssignActivity extends AnchorPane implements Activity {

    private final StudentService studentService;
    private final TeamService teamService;

    public AssignActivity() {
        studentService = new StudentService();
        teamService = new TeamService();
    }

    private Consumer<SharedEnums.GUI_ACTION_CONTEXT> intent;

    public void drawAssigningTaskContents(Scene container) {
        this.getStyleClass().add("panel-wrapper");
        this.prefWidthProperty().bind(container.widthProperty());
        this.prefHeightProperty().bind(container.heightProperty());

        List<Student> students = studentService.readAllStudentsFromFile();
        List<Team> teams = teamService.readAllTeamsFromFile();

        Label title = new Label();
        title.setText("Assign Students To Teams");
        this.getChildren().add(title);

        title.getStyleClass().add("title");
        title.prefWidthProperty().bind(container.widthProperty());
        title.setLayoutY(MARGIN / 2);

        boolean error = false;
        if (students == null || teams == null) {
            drawStatusMessageTo(container, "An error occurred while retrieving data from file.\nPlease try again.");
            error = true;
        }

        if (!error && students.size() < SharedConstants.GROUP_LIMIT && teams.size() == 0) {
            drawStatusMessageTo(container, "The number of Students is insufficient to form new Team.\nPlease add more Students then come back.");
            error = true;
        }

        if (!error) {
            drawBackButton(container, false);

            List<Student> assignableStudents = LogicalAssistant.filterUnteamedStudents(students, teams);
            LogicalAssistant.setStudentDataInTeams(teams, students);
            drawWidgetsForAssigningStudentsTaskTo(container, assignableStudents, teams);
        }
    }

    private void drawStatusMessageTo(Scene container, String message) {
        Label warning = new Label();
        warning.setText(message);
        this.getChildren().add(warning);

        warning.getStyleClass().add("warning");
        warning.setPrefWidth(MARGIN * 25);

        AnchorPane.setTopAnchor(warning, MARGIN * 5);
        AnchorPane.setLeftAnchor(warning, (this.getPrefWidth() - warning.getPrefWidth())/2);

        container.widthProperty().addListener(((observable, oldValue, newValue) -> {
            AnchorPane.setLeftAnchor(
                warning, (
                AnchorPane.getLeftAnchor(warning) + ((Double) newValue - (Double) oldValue) / 2
            ));
        }));

        drawBackButton(container, true);
    }

    private void drawWidgetsForAssigningStudentsTaskTo(Scene container, List<Student> students, List<Team> teams) {
        Label selectStudentTitle = new Label();
        selectStudentTitle.setText("Select at least 1 Student to assign");
        this.getChildren().add(selectStudentTitle);

        selectStudentTitle.getStyleClass().add("subtitle");
        selectStudentTitle.setLayoutY(MARGIN * 4);
        selectStudentTitle.setLayoutX(MARGIN);

        double tablePrefWidth = (container.getWidth() - MARGIN * 3) / 2;

        Label selectTeamTitle = new Label();
        selectTeamTitle.setText("Select 1 Team to receive Student");
        this.getChildren().add(selectTeamTitle);

        selectTeamTitle.getStyleClass().add("subtitle");
        selectTeamTitle.setLayoutY(MARGIN * 4);
        AnchorPane.setLeftAnchor(selectTeamTitle, tablePrefWidth + MARGIN * 2);

        container.widthProperty().addListener(((observable, oldValue, newValue) -> {
            AnchorPane.setLeftAnchor(
                selectTeamTitle, (
                AnchorPane.getLeftAnchor(selectTeamTitle) + ((Double) newValue - (Double) oldValue) / 2
            ));
        }));

        Student studentToAssign = drawStudentsSelection(students, tablePrefWidth);
        Team teamToReceive = drawTeamsSelection(teams, tablePrefWidth);
    }

    private Student drawStudentsSelection(List<Student> students, double initialWidth) {
        TableView studentsTable = new TableView();
        studentsTable.getStyleClass().add("data-table");
        this.getChildren().add(studentsTable);

        studentsTable.setPrefWidth(initialWidth);
        studentsTable.setPrefHeight(MARGIN * 15.5);
        AnchorPane.setTopAnchor(studentsTable, MARGIN * 5.25);
        //TODO: set left anchor

        TableColumn<String, StudentVM> uniqueIdCol = new TableColumn<>("Id");
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

        return null;
    }

    private Team drawTeamsSelection(List<Team> teams, double initialWidth) {
        //TODO
        return null;
    }

    private void drawBackButton(Scene container, boolean isErrorOccurred) {
        Button backButton = new Button();
        backButton.getStyleClass().add(isErrorOccurred ? "done-button" : "back-button");
        this.getChildren().add(backButton);

        backButton.setText(isErrorOccurred ? "Okay" : "Back");

        if (!isErrorOccurred) {
            backButton.setLayoutY(MARGIN / 2);
            backButton.setLayoutX(MARGIN);
        }
        else {
            backButton.setPrefWidth(MARGIN * 5);
            AnchorPane.setBottomAnchor(backButton, MARGIN * 1.5);
            AnchorPane.setLeftAnchor(backButton, (this.getPrefWidth() - backButton.getPrefWidth())/2);

            container.widthProperty().addListener(((observable, oldValue, newValue) -> {
                AnchorPane.setLeftAnchor(
                    backButton, (
                    AnchorPane.getLeftAnchor(backButton) + ((Double) newValue - (Double) oldValue) / 2
                ));
            }));
        }

        backButton.setOnAction(event -> { intent.accept(SharedEnums.GUI_ACTION_CONTEXT.LAUNCH); });
    }

    @Override
    public <T> void setIntent(Consumer<T> intent) {
        this.intent = (Consumer<SharedEnums.GUI_ACTION_CONTEXT>) intent;
    }
}
