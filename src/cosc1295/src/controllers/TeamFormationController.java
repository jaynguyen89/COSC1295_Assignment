package cosc1295.src.controllers;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.models.*;
import cosc1295.src.views.TeamView;
import helpers.commons.SharedConstants;

import helpers.utilities.LogicalAssistant;
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

        if (students.size() < 3) { //The number of Students is not enough to form Teams
            teamView.displayInsufficientSelectionFor(Student.class);
            return false;
        }

        if (projects.size() == 0) { //No Projects to form Teams
            teamView.displayInsufficientSelectionFor(Project.class);
            return false;
        }

        //Teams read from file holds members data with only Student Unique ID,
        //So set Student from Student List into Team members before any further tasks
        setStudentDataInTeams(teams, students);

        String featureToRun; //Task to run: assign Students to Teams, or swap Students between Teams
        //Get only Students that do not already in a Team for assigning task
        List<Student> unteamedStudents = filterUnteamedStudents(students, teams);

        //No Team was formed, so user can only assign
        if (teams.size() == 0) featureToRun = SharedConstants.ACTION_ASSIGN;
        //No unteamed Students is left, so user can only swap
        else if (unteamedStudents.size() == 0) featureToRun = SharedConstants.ACTION_SWAP;
        //Some Students are unteamed, and some Teams have formed, so ask user for a task to run
        else featureToRun = teamView.promptForSwapOrAssign()
                            ? SharedConstants.ACTION_SWAP
                            : SharedConstants.ACTION_ASSIGN;

        List<Team> newTeams; //The Teams after assigning or swapping Students
        if (SharedConstants.ACTION_SWAP.equals(featureToRun)) //Run swapping task
            newTeams = executeSwapTeamMembersFunction(teams, projects);
        else //Run assigning task
            newTeams = executeAssignStudentToTeamFunction(unteamedStudents, teams, projects);

        if (newTeams == null) return false; //user have changed their mind, no change was made, so go back to Main menu
        if (newTeams.size() != 0) { //Changes were made to teams, now save changes
            boolean success;

            for (Team newTeam : newTeams) {
                //Calculate Fitness Metrics only if a Team has enough 4 Students
                if (newTeam.getMembers().size() == SharedConstants.GROUP_LIMIT) {
                    TeamFitness teamFitness = calculateTeamFitnessMetricsFor(newTeam, projects);
                    newTeam.setFitnessMetrics(teamFitness);
                }

                //A brand new Team has created just now, so save it into file
                if (newTeam.isNewlyAdded()) success = teamService.SaveNewTeam(newTeam);
                else success = teamService.updateTeam(newTeam); //otherwise, just update it

                if (!success) { //File processing was failed (some exception)
                    teamView.displayUrgentFailedMessage();
                    return false;
                }
            }
        }

        return true;
    }

    //Set full Student data into Team members
    private void setStudentDataInTeams(List<Team> teams, List<Student> students) {
        for (Team team : teams) {
            List<Student> members = team.getMembers();
            List<Student> memberData = new ArrayList<>();

            for (Student member : members)
                for (Student student : students)
                    if (student.getUniqueId().equals(member.getUniqueId())) {
                        memberData.add(student);
                        break;
                    }

            team.setMembers(memberData);
        }
    }

    //Actually run the Assigning task
    private List<Team> executeAssignStudentToTeamFunction(
        List<Student> students,
        List<Team> teams,
        List<Project> projects
    ) {
        //Ask if user want to create a new Team to take the assigning Student
        //If no Team was ever created, then it's sure to create the first Team, otherwise, ask user
        boolean shouldCreateNewTeam = teams.size() == 0 || teamView.promptForCreateNewTeam();
        Pair<Team, Student> teamSelection = createOrSelectTeam(
                shouldCreateNewTeam, teams, projects, SharedConstants.ACTION_ASSIGN, 0
        );
        if (teamSelection == null) return null;
        Team selectedTeamToAssign = teamSelection.getKey();

        //Allow for changing Team Project while assigning Student into Team
        boolean shouldReplaceProject;
        //If the Team is just created, user can only assign Project
        if (shouldCreateNewTeam) shouldReplaceProject = false;
        //Otherwise, ask if user want to change Team Project
        else shouldReplaceProject = teamView.promptForShouldReplaceTeamProject();

        Project selectedProject = null;
        if (shouldReplaceProject) //User want to change Team Project, so let them pick a new one
            selectedProject = teamView.selectTeamProject(projects);

        if (selectedProject != null) //Set new Team Project
            selectedTeamToAssign.setProject(selectedProject);

        //Now let user pick the Students they want to assign into Team
        List<Student> selectedStudentsToAssign = teamView.selectStudentsToAssign(selectedTeamToAssign, students);
        if (selectedStudentsToAssign.size() == 0) return null;

        //Add all selected Student into Team
        for (Student student : selectedStudentsToAssign)
            selectedTeamToAssign.addMember(student);

        return new ArrayList<Team>() {/**
			 * 
			 */
			private static final long serialVersionUID = -1309752910091385980L;

		{ add(selectedTeamToAssign); }};
    }

    //Actually run the Swapping task
    private List<Team> executeSwapTeamMembersFunction(List<Team> teams, List<Project> projects) {
        @NotNull Team teamToRemoveStudent = null;
        @NotNull Student studentToBeRemoved = null;
        @NotNull Team teamToAssignStudent = null;
        @Nullable Student studentToBeReplaced = null;
        boolean shouldCreateNewTeam = false;

        //Check Team requirements on the Student it is about to take in, for Leader Type and conflicters
        boolean teamRequirementsMutuallySatisfied = false;
        while (!teamRequirementsMutuallySatisfied) {
            //The first Team in swap, and the Student it offers for swap
            Pair<Team, Student> firstTeamAndStudentToRemove = teamView.selectTeamsAndStudentsToSwap(
                    teams, SharedConstants.ACTION_SWAP, 1
            );

            teamToRemoveStudent = firstTeamAndStudentToRemove.getKey();
            studentToBeRemoved = firstTeamAndStudentToRemove.getValue();

            //Then ask if user want to take the above selected Student into a brand new Team
            //instead of putting into another existing Team
            shouldCreateNewTeam = teamView.promptForCreateNewTeam();

            //If user want to put the Student into an existing Team,
            // find the possible Teams to select as second Team in swap
            List<Team> selectableTeams = new ArrayList<>(teams);
            selectableTeams.remove(teamToRemoveStudent);

            //If no possible Team to select (ie. only 1 Team has ever created)
            //Then user must create a new Team, and put the Student into that Team
            if (selectableTeams.size() == 0) {
                teamView.displayInsufficientSelectionFor(Team.class);
                shouldCreateNewTeam = true;
            }

            //So call a method to handle both situations: creating or selecting a Team
            Pair<Team, Student> secondTeamAndStudentToBeRemoved = createOrSelectTeam(
                    shouldCreateNewTeam, selectableTeams, projects, SharedConstants.ACTION_SWAP, 2
            );

            //Null here means user have changed their mind and want to go back to Main Menu
            if (secondTeamAndStudentToBeRemoved == null) return null;

            teamToAssignStudent = secondTeamAndStudentToBeRemoved.getKey();
            studentToBeReplaced = secondTeamAndStudentToBeRemoved.getValue();

            //Check both Teams' requirements on the new Member using the utility method
            Pair<Pair<Boolean, String>, Pair<Boolean, String>> requirementChecks =
                LogicalAssistant.isTeamRequirementsMutuallySatisfied(
                    firstTeamAndStudentToRemove, secondTeamAndStudentToBeRemoved
                );

            //Null here means both Teams agree with each other's assignee
            if (requirementChecks == null) {
                teamRequirementsMutuallySatisfied = true;
                continue;
            }

            //Otherwise, Team requirements contain something to inform user,
            //Meaning Student is not suitable for assign/swap, so rerun this while statement
            if (requirementChecks.getKey() != null) teamView.displayTeamFailedRequirements(requirementChecks.getKey(), 1);
            if (requirementChecks.getValue() != null) teamView.displayTeamFailedRequirements(requirementChecks.getValue(), 2);
        }

        //User have done picking 2 Teams and Students for swap task, now swap
        boolean assignOrRemoveSuccess;
        if (shouldCreateNewTeam || studentToBeReplaced == null) {
            teamToAssignStudent.addMember(studentToBeRemoved);
            assignOrRemoveSuccess = true;
        }
        else
            assignOrRemoveSuccess = teamToAssignStudent.replaceMemberByUniqueId(
                                        studentToBeReplaced.getUniqueId(),
                                        studentToBeRemoved
                                    ) &&
                                    teamToRemoveStudent.replaceMemberByUniqueId(
                                        studentToBeRemoved.getUniqueId(),
                                        studentToBeReplaced
                                    );

        if (!assignOrRemoveSuccess) {
            teamView.displayAssignOrRemoveMemberError();
            return null;
        }

        assignOrRemoveSuccess = teamToRemoveStudent.removeMemberByUniqueId(studentToBeRemoved.getUniqueId());
        teamToRemoveStudent.addMember(studentToBeReplaced);

        if (!assignOrRemoveSuccess) {
            teamView.displayAssignOrRemoveMemberError();
            return null;
        }

        Team finalTeamToRemoveStudent = teamToRemoveStudent;
        Team finalTeamToAssignStudent = teamToAssignStudent;
        return new ArrayList<Team>() {/**
			 * 
			 */
			private static final long serialVersionUID = 3071345686511809968L;

		{ add(finalTeamToRemoveStudent); add(finalTeamToAssignStudent); }};
    }

    //Handle the situations create/select Team when swapping Students
    private Pair<Team, Student> createOrSelectTeam(
        boolean shouldCreateTeam,
        List<Team> teams,
        List<Project> projects,
        @NotNull String action,
        int order
    ) {
        Team teamInAction = new Team();
        Student studentInTeam = null;

        if (shouldCreateTeam) { //Create a Team, also pick a Team Project
            Project selectedProjectForNewTeam = teamView.selectTeamProject(projects);

            teamInAction.setNewlyAdded(true);
            teamInAction.setProject(selectedProjectForNewTeam);
        }
        else { //Select a Team, also pick a Student for swap
            Pair<Team, Student> teamToAssignStudent = teamView.selectTeamsAndStudentsToSwap(teams, action, order);
            if (teamToAssignStudent == null) return null;

            teamInAction = teamToAssignStudent.getKey();
            studentInTeam = teamToAssignStudent.getValue();
        }

        return new Pair<>(teamInAction, studentInTeam);
    }

    //Pick up only the Students who have not already in any Teams when user want to assign a Student to a Team
    private List<Student> filterUnteamedStudents(List<Student> students, List<Team> teams) {
        List<Student> unteamedStudents = new ArrayList<>(students);

        for (Team team : teams)
            for (Student member : team.getMembers())
                unteamedStudents.removeIf(
                    m -> m.getUniqueId().equals(member.getUniqueId())
                );

        return unteamedStudents;
    }

    public void displayTeamSelectionFinalResult(Boolean taskResult) {
        teamView.displayTaskFinalResult(taskResult);
    }

    //The method name speaks its task
    public void printTeamFitnessMetricsTable() {
        List<Team> teams = teamService.readAllTeamsFromFile();

        if (teams == null) teamView.displayTaskFinalResult(null);
        else if (teams.size() == 0) teamView.displayInsufficientSelectionFor(Team.class);
        else teamView.printFitnessMetricsTable(teams);
    }

    //Additional Menu feature that is not in assignment requirement
    //Allow for selecting a Project to replace/assign to a Team's Project
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

        //This method in TeamView can also handle this Team selection
        Team selectedTeam = teamView.selectTeamsAndStudentsToSwap(
                teams, SharedConstants.ACTION_ASSIGN, 0
        ).getKey();
        if (selectedTeam == null) return false;

        //Let user pick a project
        Project selectedProject = teamView.selectTeamProject(projects);
        if (selectedProject == null) return false;

        //Then set new Project, and recalculate Fitness Metrics if Team has enough 4 Students
        selectedTeam.setProject(selectedProject);
        if (selectedTeam.getMembers().size() == SharedConstants.GROUP_LIMIT) {
            TeamFitness teamFitness = calculateTeamFitnessMetricsFor(selectedTeam, projects);
            selectedTeam.setFitnessMetrics(teamFitness);
        }

        //Finally update the Team
        if (!teamService.updateTeam(selectedTeam)) {
            teamView.displayUrgentFailedMessage();
            return false;
        }

        return true;
    }

    //Used for Unittest
    public List<Student> getAssignableStudentsForTest(List<Student> students, List<Team> teams) {
        return filterUnteamedStudents(students, teams);
    }

    //Used for Unittest
    public List<Team> runFeatureAssignStudentForTest(
        List<Student> students,
        List<Team> teams,
        List<Project> projects
    ) {
        return executeAssignStudentToTeamFunction(students, teams, projects);
    }
}
