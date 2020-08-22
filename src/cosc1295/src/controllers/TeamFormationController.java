package cosc1295.src.controllers;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.models.*;
import cosc1295.src.views.TeamView;
import helpers.commons.SharedConstants;

import helpers.utilities.Helpers;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;

public class TeamFormationController extends ControllerBase {

    private final TeamView teamView;
    private final StudentService studentService;
    private final TeamService teamService;
    private final ProjectService projectService;

    public TeamFormationController() {
        teamView = new TeamView();
        studentService = new StudentService();
        teamService = new TeamService();
        projectService = new ProjectService();
    }

    public Boolean executeTeamSelectionTask() {
        List<Student> students = studentService.readAllStudentsFromFile();
        List<Project> projects = projectService.readAllProjectsFromFile();
        List<Team> teams = teamService.readAllTeamsFromFile();

        if (students == null || projects == null || teams == null)
            return null;

        if (students.size() < 3) {
            teamView.displayInsufficientSelectionFor(Student.class);
            return false;
        }

        if (projects.size() == 0) {
            teamView.displayInsufficientSelectionFor(Project.class);
            return false;
        }

        String featureToRun;
        List<Student> unteamedStudents = filterUnteamedStudents(students, teams);
        if (teams.size() == 0) featureToRun = SharedConstants.ACTION_ASSIGN;
        else if (unteamedStudents.size() == 0) featureToRun = SharedConstants.ACTION_SWAP;
        else featureToRun = teamView.promptForSwapOrAssign()
                            ? SharedConstants.ACTION_SWAP
                            : SharedConstants.ACTION_ASSIGN;

        List<Team> newTeams;
        if (SharedConstants.ACTION_SWAP.equals(featureToRun))
            newTeams = executeSwapTeamMembersFunction(teams, projects);
        else
            newTeams = executeAssignStudentToTeamFunction(unteamedStudents, teams, projects);

        if (newTeams == null) return false;
        if (newTeams.size() != 0) {
            boolean error;

            for (Team newTeam : newTeams) {
                if (newTeam.getMembers().size() == SharedConstants.GROUP_LIMIT) {
                    TeamFitness teamFitness = calculateTeamFitnessMetricsFor(newTeam, projects);
                    newTeam.setFitnessMetrics(teamFitness);
                }

                if (newTeam.isNewlyAdded()) error = teamService.SaveNewTeam(newTeam);
                else error = teamService.updateTeam(newTeam);

                if (error) {
                    teamView.displayUrgentFailedMessage();
                    return false;
                }
            }
        }

        return true;
    }

    private List<Team> executeAssignStudentToTeamFunction(
        List<Student> students,
        List<Team> teams,
        List<Project> projects
    ) {
        boolean shouldCreateNewTeam = teamView.promptForCreateNewTeam();
        Team selectedTeamToAssign = createOrSelectTeam(
                shouldCreateNewTeam, teams, projects, SharedConstants.ACTION_ASSIGN, 0
        ).getKey();

        boolean shouldReplaceProject = shouldCreateNewTeam || teamView.promptForShouldReplaceTeamProject();
        Project selectedProject = null;
        if (shouldReplaceProject)
            selectedProject = teamView.selectTeamProject(projects);

        if (selectedProject != null)
            selectedTeamToAssign.setProject(selectedProject);

        List<Student> selectedStudentsToAssign = teamView.selectStudentsToAssign(selectedTeamToAssign, students);
        if (selectedStudentsToAssign.size() == 0) return null;

        for (Student student : selectedStudentsToAssign)
            selectedTeamToAssign.addMember(student);

        return new ArrayList<Team>() {{ add(selectedTeamToAssign); }};
    }

    private List<Team> executeSwapTeamMembersFunction(List<Team> teams, List<Project> projects) {
        @NotNull Team teamToRemoveStudent = null;
        @NotNull Student studentToBeRemoved = null;
        @NotNull Team teamToAssignStudent = null;
        @Nullable Student studentToBeReplaced = null;
        boolean shouldCreateNewTeam = false;

        boolean teamRequirementsMutuallySatisfied = false;
        while (!teamRequirementsMutuallySatisfied) {
            Pair<Team, Student> firstTeamAndStudentToRemove = teamView.selectTeamToSwapStudents(
                    teams, SharedConstants.ACTION_SWAP, 1
            );

            teamToRemoveStudent = firstTeamAndStudentToRemove.getKey();
            studentToBeRemoved = firstTeamAndStudentToRemove.getValue();

            shouldCreateNewTeam = teamView.promptForCreateNewTeam();
            List<Team> selectableTeams = new ArrayList<>(teams);
            selectableTeams.remove(teamToRemoveStudent);
            Pair<Team, Student> secondTeamAndStudentToBeRemoved = createOrSelectTeam(
                    shouldCreateNewTeam, selectableTeams, projects, SharedConstants.ACTION_SWAP, 2
            );

            teamToAssignStudent = secondTeamAndStudentToBeRemoved.getKey();
            studentToBeReplaced = secondTeamAndStudentToBeRemoved.getValue();

            Pair<Pair<Boolean, String>, Pair<Boolean, String>> requirementChecks =
                Helpers.isTeamRequirementsMutuallySatisfied(
                    firstTeamAndStudentToRemove, secondTeamAndStudentToBeRemoved
                );

            if (requirementChecks == null) {
                teamRequirementsMutuallySatisfied = true;
                continue;
            }

            if (requirementChecks.getKey() != null) teamView.displayTeamFailedRequirements(requirementChecks.getKey(), 1);
            if (requirementChecks.getValue() != null) teamView.displayTeamFailedRequirements(requirementChecks.getValue(), 2);
        }

        boolean shouldReplaceProject = shouldCreateNewTeam || teamView.promptForShouldReplaceTeamProject();
        Project selectedProject = null;
        if (shouldReplaceProject)
            selectedProject = teamView.selectTeamProject(projects);

        if (selectedProject != null)
            teamToAssignStudent.setProject(selectedProject);

        boolean assignOrRemoveError = false;
        if (shouldCreateNewTeam || studentToBeReplaced == null)
            teamToAssignStudent.addMember(studentToBeRemoved);
        else
            assignOrRemoveError = !teamToAssignStudent.replaceMemberByUniqueId(
                                        studentToBeReplaced.getUniqueId(),
                                        studentToBeRemoved
                                  ) &&
                                  !teamToRemoveStudent.replaceMemberByUniqueId(
                                        studentToBeRemoved.getUniqueId(),
                                        studentToBeReplaced
                                  );

        if (!assignOrRemoveError) {
            teamView.displayAssignOrRemoveMemberError();
            return null;
        }

        assignOrRemoveError = teamToRemoveStudent.removeMemberByUniqueId(studentToBeRemoved.getUniqueId());

        if (!assignOrRemoveError) {
            teamView.displayAssignOrRemoveMemberError();
            return null;
        }

        Team finalTeamToRemoveStudent = teamToRemoveStudent;
        Team finalTeamToAssignStudent = teamToAssignStudent;
        return new ArrayList<Team>() {{ add(finalTeamToRemoveStudent); add(finalTeamToAssignStudent); }};
    }

    private Pair<Team, Student> createOrSelectTeam(
        boolean shouldCreateTeam,
        List<Team> teams,
        List<Project> projects,
        @NotNull String action,
        int order
    ) {
        Team teamInAction = new Team();
        Student studentInTeam = null;

        if (shouldCreateTeam) {
            Project selectedProjectForNewTeam = teamView.selectTeamProject(projects);

            teamInAction.setNewlyAdded(true);
            teamInAction.setProject(selectedProjectForNewTeam);
        }
        else {
            Pair<Team, Student> teamToAssignStudent = teamView.selectTeamToSwapStudents(teams, action, order);

            teamInAction = teamToAssignStudent.getKey();
            studentInTeam = teamToAssignStudent.getValue();
        }

        return new Pair<>(teamInAction, studentInTeam);
    }

    private List<Student> filterUnteamedStudents(List<Student> students, List<Team> teams) {
        List<Student> unteamedStudents = new ArrayList<>();

        for (Student student : students) {
            boolean teamed = false;

            for (Team team : teams)
                if (team.getMembers().contains(student)) {
                    teamed = true;
                    break;
                }

            if (!teamed) unteamedStudents.add(student);
        }

        return unteamedStudents;
    }

    public void displayTeamSelectionFinalResult(Boolean taskResult) {
        teamView.displayTaskFinalResult(taskResult);
    }

    public void printTeamFitnessMetricsTable() {
        List<Team> teams = teamService.readAllTeamsFromFile();

        if (teams == null) teamView.displayTaskFinalResult(null);
        else if (teams.size() == 0) teamView.displayInsufficientSelectionFor(Team.class);
        else teamView.printFitnessMetricsTable(teams);
    }

    public Boolean executeTeamProjectSelectionTask() {
        List<Team> teams = teamService.readAllTeamsFromFile();
        List<Project> projects = projectService.readAllProjectsFromFile();

        if (projects == null || teams == null) return null;

        if (projects.size() == 0) {
            teamView.displayInsufficientSelectionFor(Project.class);
            return false;
        }

        if (teams.size() == 0) {
            teamView.displayInsufficientSelectionFor(Team.class);
            return false;
        }

        Team selectedTeam = teamView.selectTeamToSwapStudents(
                teams, SharedConstants.ACTION_ASSIGN, 0
        ).getKey();
        if (selectedTeam == null) return false;

        Project selectedProject = teamView.selectTeamProject(projects);
        if (selectedProject == null) return false;

        selectedTeam.setProject(selectedProject);
        if (selectedTeam.getMembers().size() == SharedConstants.GROUP_LIMIT) {
            TeamFitness teamFitness = calculateTeamFitnessMetricsFor(selectedTeam, projects);
            selectedTeam.setFitnessMetrics(teamFitness);
        }

        if (!teamService.updateTeam(selectedTeam)) {
            teamView.displayUrgentFailedMessage();
            return false;
        }

        return true;
    }

    public List<Student> getAssignableStudentsForTest(List<Student> students, List<Team> teams) {
        return filterUnteamedStudents(students, teams);
    }

    public List<Team> runFeatureAssignStudentForTest(
        List<Student> students,
        List<Team> teams,
        List<Project> projects
    ) {
        return executeAssignStudentToTeamFunction(students, teams, projects);
    }
}
