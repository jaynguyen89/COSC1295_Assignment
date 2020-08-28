package cosc1295.src.views;

import com.sun.istack.internal.NotNull;
import cosc1295.designs.Flasher;
import cosc1295.src.models.Flash;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import helpers.commons.SharedEnums.FLASH_TYPES;
import helpers.utilities.LogicalAssistant;

import javafx.util.Pair;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TeamView {

    private final Flasher flasher = Flasher.getInstance();
    private Scanner inputScanner;

    public TeamView() {
        inputScanner = new Scanner(System.in);
    }

    /**
     * Displays a message when user select Assign/Swap Students feature but the number of Teams/Students is insufficient
     * @param type Class<T>
     * @param <T> Type
     */
    public <T> void displayInsufficientSelectionFor(Class<T> type) {
        flasher.flash(new Flash(
            "The number of " + type.getSimpleName() + " is insufficient to assign to teams. Please go back to add more " + type.getSimpleName() + ".",
            FLASH_TYPES.ATTENTION
        ));

        flasher.flash(new Flash("Press enter to continue.", FLASH_TYPES.NONE));
        inputScanner.nextLine();
    }

    /**
     * Displays a Confirmation asking user to select if they want to assign or swap Student
     * @return boolean
     */
    public boolean promptForSwapOrAssign() {
        return flasher.promptForConfirmation(new Flash(
            "Please select the specific task you would like to do:\n" +
                    "Y: Swap students between teams\n" +
                    "N: Assign students to teams",
            FLASH_TYPES.NONE
        ));
    }

    /**
     * Displays a message to user telling an error occurred while saving data into files.
     */
    public void displayUrgentFailedMessage() {
        flasher.flash(new Flash(
            "An error occurred while saving data to files. Saving progress was cancelled.\n" +
                    "Please review changes by inspecting your Teams. Press enter to continue.",
            FLASH_TYPES.ERROR
        ));

        inputScanner.nextLine();
    }

    /**
     * Displays a message at the end of user task to inform the final result of their task.
     * @param taskResult Boolean
     */
    public void displayTaskFinalResult(Boolean taskResult) {
        if (taskResult == null || taskResult)
            flasher.flash(new Flash(
                taskResult == null
                    ? "An error occurred while reading data from files. Please try again.\nPress enter to continue."
                    : "Teams data have been saved successfully.\nPress enter to continue.",
                taskResult == null ? FLASH_TYPES.ERROR : FLASH_TYPES.SUCCESS
            ));

        inputScanner.nextLine();
    }

    /**
     * Displays a message when an exception occurred while swapping/assigning Students into Teams.
     */
    public void displayAssignOrRemoveMemberError() {
        flasher.flash(new Flash(
            "Error occurred while assigning/removing students to/from Teams. Please re-try.\nPress enter to continue",
            FLASH_TYPES.ERROR
        ));
        inputScanner.nextLine();
    }

    /**
     * Displays a Confirmation asking user if they wish to change Team Project while they select Teams for assigning/swapping Students.
     * @return boolean
     */
    public boolean promptForShouldReplaceTeamProject() {
        return flasher.promptForConfirmation(new Flash(
                "Do you wish to replace Project for the selected Team?\n" +
                        "Y: Yes\tN: No",
                FLASH_TYPES.NONE
        ));
    }

    /**
     * Displays a Confirmation asking user if they want to create new Team to assign/swap Student into.
     * @return boolean
     */
    public boolean promptForCreateNewTeam() {
        return flasher.promptForConfirmation(new Flash(
            "Do you wish to create new Team for assigning/swapping Student to?\n" +
                    "Y: Yes\tN: No",
            FLASH_TYPES.NONE
        ));
    }

    /**
     * Lets user pick a Project from Project List when they change Team Project.
     * Returns the Project being picked.
     * @param projects List<Project>
     * @return Project
     */
    public Project selectTeamProject(List<Project> projects) {
        flasher.flash(new Flash("Please select a Project to set for the Team:\n", FLASH_TYPES.NONE));
        for (Project project : projects)
            flasher.flash(new Flash("\t" + project.display(), FLASH_TYPES.NONE));

        boolean taskDone = false;
        while (!taskDone) {
            flasher.flash(new Flash(
                "\nSelected Project: (or press X if you change your mind and want to skip this)",
                FLASH_TYPES.NONE
            ));

            String selectedProjectId = inputScanner.next(); //Take user input
            inputScanner.nextLine();

            //If user press X, they change their mind and want to skip this action
            if (selectedProjectId.trim().equalsIgnoreCase(SharedEnums.APPLICATION_MENU.X.name())) {
                taskDone = true;
                continue;
            }

            //User enter input for Project ID, check their input for a project in list
            try {
                for (Project project : projects)
                    if (project.getUniqueId().equals(selectedProjectId) ||
                        project.getId() == Integer.parseInt(selectedProjectId)
                    ) return project; //Project found, return immediately
            } catch (NumberFormatException ex) { //user input something else
                flasher.flash(new Flash("Your selection is invalid. Press enter to continue.", FLASH_TYPES.ATTENTION));
                inputScanner.nextLine();
                continue;
            }

            flasher.flash(new Flash( //Project ID not in list
                "No Project was found with your selection. Please select again.\n" +
                        "Press enter to continue.",
                FLASH_TYPES.ERROR
            ));
            inputScanner.nextLine();
        }

        return null;
    }

    /**
     * Lets user select a Team first, then select a Student in that Team to swap out.
     * Return a Pair with the selected Team and Student.
     * That Pair can be regarded as the first/second Team in a swap by param order.
     * Param action specifies whether user want to assign Student from 1 Team to another instead of
     * swapping Students between Teams (this is a particular logic in my app).
     * @param teams List<Team>
     * @param action String
     * @param order int
     * @return Pair<Team, Student>
     */
    public Pair<Team, Student> selectTeamsAndStudentsToSwap(List<Team> teams, @NotNull String action, int order) {
        Team selectedTeam = null;
        Student selectedStudent = null;

        if (action.equals(SharedConstants.ACTION_SWAP)) //Swap Students between Teams
            flasher.flash(new Flash(
                "\nPlease select the " + (order == 1 ? "first" : "second") + " Team to swap Student:\n",
                FLASH_TYPES.NONE
            ));
        else { //Assign Student from 1 Team to the other
            //Unlike swapping Students, Teams that already have 4 Students can't be assigned more, so remove it from list
            //In the If statement above, Teams having 4 Students are okay for swap, so don't need to pre-process it
            teams.removeIf(m -> m.getMembers().size() == SharedConstants.GROUP_LIMIT);
            if (teams.size() == 0) { //If after removing "full" Teams, no selectable Teams left in the list, then...
                flasher.flash(new Flash(
                    "All Teams have enough 4 Students, unable to assign more." +
                    "Please select another action. Press enter to continue.\n",
                    FLASH_TYPES.NONE
                ));

                inputScanner.nextLine();
                return null;
            }

            flasher.flash(new Flash("Please select a team to assign Student:\n", FLASH_TYPES.NONE));
        }

        //Print out Teams list
        for (Team team : teams)
            flasher.flash(new Flash("\t" + team.display(), FLASH_TYPES.NONE));

        boolean taskDone = false;
        while (!taskDone) {
            flasher.flash(new Flash("\nYour Selection:", FLASH_TYPES.NONE));

            String selectedTeamId = inputScanner.next(); //Take user input for the selected Team
            inputScanner.nextLine();

            boolean inputError = false;
            for (Team team : teams) { //Check input against the processed Teams list, so user can't hack selecting illegal Teams
                int teamId;
                try {
                    teamId = Integer.parseInt(selectedTeamId);
                } catch (NumberFormatException ex) { //Invalid inputs
                    flasher.flash(new Flash("Your selection is invalid. Press enter to continue.", FLASH_TYPES.ATTENTION));
                    inputScanner.nextLine();

                    inputError = true;
                    break;
                }

                if (team.getId() == teamId) { //Team found
                    selectedTeam = team;
                    break;
                }
            }

            if (inputError) { //When invalid inputs or Team not found
                flasher.flash(new Flash(
                    "No Team was found with your selection. Please select again.\n" +
                            "Press enter to continue.",
                    FLASH_TYPES.ERROR
                ));
                inputScanner.nextLine();
            }

            taskDone = selectedTeam != null;
        }

        //When user's task is swapping, more jobs need to do below: let user select Student to swap
        if (action.equals(SharedConstants.ACTION_SWAP)) {
            flasher.flash(new Flash(
                "Please select a Student in the " + (order == 1 ? "first" : "second") + " Team to swap:",
                FLASH_TYPES.NONE
            ));

            //Print out members in the selected Team so user can pick one
            for (Student student : selectedTeam.getMembers())
                flasher.flash(new Flash("\t" + student.display(), FLASH_TYPES.NONE));

            taskDone = false;
            while (!taskDone) {
                flasher.flash(new Flash("\nYour Selection:", FLASH_TYPES.NONE));

                String selectedStudentId = inputScanner.next(); //user input for Student
                inputScanner.nextLine();

                for (Student student : selectedTeam.getMembers())
                    try {
                        if (student.getId() == Integer.parseInt(selectedStudentId) ||
                            student.getUniqueId().equals(selectedStudentId)
                        ) {
                            selectedStudent = student; //A member found with user input
                            break;
                        }
                    } catch (NumberFormatException ex) { //Invalid inputs
                        flasher.flash(new Flash("Your selection is invalid. Press enter to continue.", FLASH_TYPES.ATTENTION));
                        inputScanner.nextLine();
                        break;
                    }

                taskDone = selectedStudent != null;
                if (!taskDone) { //If member not found or invalid user input
                    flasher.flash(new Flash(
                        "No Student was found with your selection. Please select again.\n" +
                                "Press enter to continue.",
                        FLASH_TYPES.ERROR
                    ));
                    inputScanner.nextLine();
                }
            }
        }

        return new Pair<>(selectedTeam, selectedStudent);
    }

    /**
     * Lets user select Students from Student list to assign into a Team priorly selected.
     * Return a list of Students being selected.
     * @param teamToAssign Team
     * @param students List<Student>
     * @return List<Student>
     */
    public List<Student> selectStudentsToAssign(Team teamToAssign, List<Student> students) {
        List<Student> selectedStudents = new ArrayList<>(); //user can assign multiple Students at once
        //Get Team requirements on the assigned Student
        Pair<Boolean, List<String>> teamRequirements = LogicalAssistant.produceTeamRequirementsOnNewMember(teamToAssign.getMembers(), selectedStudents);

        flasher.flash(new Flash("Please select Students to assign into new Team.\n", FLASH_TYPES.NONE));

        boolean taskDone = false;
        while (!taskDone) {
            //Count the available slots in Team
            int selectableCount = SharedConstants.GROUP_LIMIT - selectedStudents.size() - teamToAssign.getMembers().size();
            if (selectableCount == 0) //no available slots left
                return selectedStudents;

            // Pre-process the selectable Students based on Team requirements
            // this list will be used to check against user input, not the original Student list
            // So that user can't hack selecting an illegal Student (ie. duplicate assignment or Student already in a Team)
            List<Student> selectableStudents = new ArrayList<>();
            if (teamRequirements != null) {
                if (teamRequirements.getValue() == null) selectableStudents.addAll(students);
                else selectableStudents.addAll(removeRefusedStudents(students, teamRequirements.getValue()));
            }

            selectableStudents.removeAll(selectedStudents); //Remove Students that have just been selected here
            selectableStudents.removeAll(teamToAssign.getMembers()); //Remove Students already in the Team itself

            //Print out the selectable Students
            for (Student student : selectableStudents)
                flasher.flash(new Flash("\t" + student.display(), FLASH_TYPES.NONE));

            //Inform the number of slots available, and the Team requirements on the new Student, so user is informed
            flasher.flash(new Flash(
                "\nYou can now select " + selectableCount + " more Student(s).\n" +
                        (teamRequirements != null && teamRequirements.getKey()
                                ? "This Team needs a Student with Leader personality type (A).\n" : SharedConstants.EMPTY_STRING
                        ) +
                        "Your selection: (press X to skip and go back or if you have done selection)",
                FLASH_TYPES.NONE
            ));

            String selectedStudentId = inputScanner.next(); //Take user input
            inputScanner.nextLine();

            //User have done selection, or they change their mind and want to go back
            if (selectedStudentId.trim().equalsIgnoreCase(SharedEnums.APPLICATION_MENU.X.name())) {
                taskDone = true;
                continue;
            }

            Student selectedStudent = null;
            boolean error = false;
            for (Student student : selectableStudents) { //Check the selected Student against the selectable Students
                if (teamToAssign.getMembers().contains(student) ||
                    selectedStudents.contains(student)
                ) continue; //Perform this checking again to assure the integrity of the selected Student

                try {
                    if (student.getId() == Integer.parseInt(selectedStudentId) ||
                        student.getUniqueId().equals(selectedStudentId)
                    ) {
                        selectedStudent = student; //The selected Student is found in selectable Students
                        break;
                    }
                } catch (NumberFormatException ex) { //Invalid inputs
                    flasher.flash(new Flash("Your selection is invalid. Press enter to continue.", FLASH_TYPES.ATTENTION));
                    inputScanner.nextLine();

                    error = true;
                    break;
                }
            }

            taskDone = selectedStudent != null; //whether Student is found and valid
            if (!taskDone && !error) { //Student not found or invalid inputs
                flasher.flash(new Flash(
                    "No Student was found with your selection, or the Student you select has been assigned to a Team.\n" +
                            "Please select again. Press enter to continue.",
                    FLASH_TYPES.ERROR
                ));

                inputScanner.nextLine();
                continue;
            }

            if (selectedStudent != null) { //Student is found and valid
                //Now check the Student with Team requirement for whether Leader type is enforced
                if (teamRequirements.getKey() && (
                    teamToAssign.getMembers().size() + selectedStudents.size() == 3) &&
                    selectedStudent.getPersonality() != SharedEnums.PERSONALITIES.A
                ) {
                    flasher.flash(new Flash(
                            "You must select a Student with Leader personality type (A) " +
                                    "because this Team only has 1 slot left but no Leader is assigned.\n" +
                                    "Please press enter to select again.",
                            FLASH_TYPES.ERROR
                    ));

                    inputScanner.nextLine();
                    taskDone = false;
                    continue;
                }

                //The selected Student went through all validations and checking, is safe to assign
                selectedStudents.add(selectedStudent);
                flasher.flash(new Flash("Student " + selectedStudent.getUniqueId() + " will be added to Team.\n", FLASH_TYPES.SUCCESS));

                //Reproduce the Team requirements for the next members
                teamRequirements = LogicalAssistant.produceTeamRequirementsOnNewMember(teamToAssign.getMembers(), selectedStudents);
                taskDone = selectedStudents.size() == SharedConstants.GROUP_LIMIT;
            }
        }

        return selectedStudents;
    }

    /**
     * Remove the Students who are refused by a Team when displaying Student list for user selection (while assigning or swapping).
     * Refusals are Students with conflicts to Team members.
     * Return list of selectable Students.
     * @param students List<Student>
     * @param refusals List<String>
     * @return List<Student>
     */
    private List<Student> removeRefusedStudents(List<Student> students, List<String> refusals) {
        List<Student> selectableStudents = new ArrayList<>();

        for (Student student : students)
            if (!refusals.contains(student.getUniqueId()))
                selectableStudents.add(student);

        return selectableStudents;
    }

    /**
     * Print out the Team Fitness Metrics as table for each Team.
     * Fitness Metrics are only calculated for Teams with enough 4 members,
     * So display a message if a Team has less members so user are informed.
     * @param teams List<Team>
     */
    public void printFitnessMetricsTable(List<Team> teams) {
        flasher.flash(new Flash("\tTeam Fitness Metrics Table\n", FLASH_TYPES.NONE));

        for (Team team : teams) {
            flasher.flash(new Flash("\t" + team.display(), FLASH_TYPES.NONE));

            if (team.getFitnessMetrics() == null) { //Null because Team has <4 members
                flasher.flash(new Flash(
                    "\t\tThis team do not have enough " + SharedConstants.GROUP_LIMIT + " Students. " +
                            "Fitness Metrics are not yet calculated.\n",
                    FLASH_TYPES.NONE
                ));
                continue;
            }

            flasher.flash(new Flash(team.getFitnessMetrics().display() + "\n", FLASH_TYPES.NONE));
        }

        flasher.flash(new Flash("End of Team Fitness Metrics Table. Press enter to continue.", FLASH_TYPES.NONE));
        inputScanner.nextLine();
    }

    /**
     * Failed requirements means "failed to meet requirements" of the other Team when swapping Students.
     * Displays the requirements that the Team failed to meet (Leader type required, and/or refused Students).
     * @param failures Pair<Boolean, String> failures
     * @param teamOrder int
     */
    public void displayTeamFailedRequirements(Pair<Boolean, String> failures, int teamOrder) {
        if (failures != null) {
            String firstTeam = teamOrder == 1 ? "The first Team" : "The second Team";
            String secondTeam = teamOrder == 1 ? "The second Team" : "The first Team";

            String leaderMessage = !failures.getKey() ? SharedConstants.EMPTY_STRING
                                                      : firstTeam + " requires a Student with Leader personality type (A). " +
                                                        "You must select a Leader from " + secondTeam + ".\n";
            String refusalMessage = failures.getValue() == null ? SharedConstants.EMPTY_STRING
                                                                : firstTeam + " has a Student with personal conflict to Student " +
                                                                  failures.getValue() + ", who is selected from " + secondTeam +
                                                                  ". Please select another Student from " + secondTeam;

            flasher.flash(new Flash(
                "The Students selected from 2 Teams do not satisfied each other's requirement:\n" +
                        "\t" + leaderMessage + "\t" + refusalMessage + "\nPress enter to select again.",
                FLASH_TYPES.ERROR
            ));

            inputScanner.nextLine();
        }
    }

    //This method is used by Unittest to send user inputs into app
    public void sendTestInput(ByteArrayInputStream in) {
        System.setIn(in);
        inputScanner = new Scanner(System.in);
    }
}
