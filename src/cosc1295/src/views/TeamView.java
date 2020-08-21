package cosc1295.src.views;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import cosc1295.designs.Flasher;
import cosc1295.src.models.Flash;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.FLASH_TYPES;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TeamView {

    private final Flasher flasher = Flasher.getInstance();
    private final Scanner inputScanner;

    public TeamView() {
        inputScanner = new Scanner(System.in);
    }

    public <T> void displayInsufficientSelectionFor(Class<T> type) {
        flasher.flash(new Flash(
            "The number of " + type.getSimpleName() + "is insufficient to assign to teams. Please go back to add more " + type.getSimpleName(),
            FLASH_TYPES.ATTENTION
        ));

        flasher.flash(new Flash("Press enter to continue.", FLASH_TYPES.NONE));
        inputScanner.nextLine();
    }

    public boolean promptForSwapOrAssign() {
        return flasher.promptForConfirmation(new Flash(
            "Please select the specific task you would like to do:\n" +
                    "Y: Swap students between teams\n" +
                    "N: Assign students to teams",
            FLASH_TYPES.NONE
        ));
    }

    public void displayUrgentFailedMessage() {
        flasher.flash(new Flash(
            "An error occurred while saving data to files. Saving progress was cancelled.\n" +
                    "Please review changes by inspecting your Teams. Press enter to continue.",
            FLASH_TYPES.ERROR
        ));

        inputScanner.nextLine();
    }

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

    public void displayAssignOrRemoveMemberError() {
        flasher.flash(new Flash(
            "Error occurred while assigning/removing students to/from Teams. Please re-try.\nPress enter to continue",
            FLASH_TYPES.ERROR
        ));
        inputScanner.nextLine();
    }

    public boolean promptForShouldReplaceTeamProject() {
        return flasher.promptForConfirmation(new Flash(
                "Do you wish to replace Project for the selected Team?\n" +
                        "Y: Yes\tN: No",
                FLASH_TYPES.NONE
        ));
    }

    public boolean promptForCreateNewTeam() {
        return flasher.promptForConfirmation(new Flash(
            "Do you wish to create new Team for assigning/swapping Student to?\n" +
                    "Y: Yes\tN: No",
            FLASH_TYPES.NONE
        ));
    }

    public Project selectTeamProject(List<Project> projects) {
        flasher.flash(new Flash("Please select a Project to replace the current Team's Project:\n", FLASH_TYPES.NONE));
        for (Project project : projects)
            flasher.flash(new Flash("\t" + project.display(), FLASH_TYPES.NONE));

        boolean taskDone = false;
        while (!taskDone) {
            flasher.flash(new Flash(
                "Selected Project: (or press Enter if you change your mind and want to skip this)",
                FLASH_TYPES.NONE
            ));

            String selectedProjectId = inputScanner.next();
            inputScanner.nextLine();

            if (selectedProjectId.length() == 0 || selectedProjectId.trim().equals(SharedConstants.EMPTY_STRING)) {
                taskDone = true;
                continue;
            }

            try {
                for (Project project : projects)
                    if (project.getUniqueId().equals(selectedProjectId) ||
                        project.getId() == Integer.parseInt(selectedProjectId)
                    ) return project;
            } catch (NumberFormatException ex) {
                flasher.flash(new Flash("Your selection is invalid. Press enter to continue.", FLASH_TYPES.ATTENTION));
                inputScanner.nextLine();
            }

            flasher.flash(new Flash(
                "No Team was found with your selection. Please select again.\n" +
                        "Press enter to continue.",
                FLASH_TYPES.ERROR
            ));
            inputScanner.nextLine();
        }

        return null;
    }

    public Pair<Team, Student> selectTeamToAssignOrSwapStudents(
        List<Team> teams,
        @NotNull String action,
        int order
    ) {
        Team selectedTeam = null;
        Student selectedStudent = null;

        if (action.equals(SharedConstants.ACTION_SWAP))
            flasher.flash(new Flash(
                "Please select the " + (order == 1 ? "first" : "second") + " Team to swap Student:\n",
                FLASH_TYPES.NONE
            ));
        else
            flasher.flash(new Flash("Please select a team to assign Student:\n", FLASH_TYPES.NONE));

        for (Team team : teams)
            flasher.flash(new Flash("\t" + team.display(), FLASH_TYPES.NONE));

        boolean taskDone = false;
        while (!taskDone) {
            flasher.flash(new Flash("Your Selection:", FLASH_TYPES.NONE));

            String selectedTeamId = inputScanner.next();
            inputScanner.nextLine();

            boolean inputError = false;
            for (Team team : teams) {
                int teamId;
                try {
                    teamId = Integer.parseInt(selectedTeamId);
                } catch (NumberFormatException ex) {
                    flasher.flash(new Flash("Your selection is invalid. Press enter to continue.", FLASH_TYPES.ATTENTION));
                    inputScanner.nextLine();

                    inputError = true;
                    break;
                }

                if (team.getId() == teamId) {
                    selectedTeam = team;
                    break;
                }
            }

            if (!inputError) {
                flasher.flash(new Flash(
                        "No Team was found with your selection. Please select again.\n" +
                                "Press enter to continue.",
                        FLASH_TYPES.ERROR
                ));
                inputScanner.nextLine();
            }

            taskDone = selectedTeam != null;
        }

        if (action.equals(SharedConstants.ACTION_SWAP)) {
            flasher.flash(new Flash(
                "Please select a Student in the " + (order == 1 ? "first" : "second") + " Team to swap:",
                FLASH_TYPES.NONE
            ));

            for (Student student : selectedTeam.getMembers())
                flasher.flash(new Flash("\t" + student.display(), FLASH_TYPES.NONE));

            taskDone = false;
            while (!taskDone) {
                flasher.flash(new Flash("Your Selection:", FLASH_TYPES.NONE));

                String selectedStudentId = inputScanner.next();
                inputScanner.nextLine();

                for (Student student : selectedTeam.getMembers())
                    try {
                        if (student.getId() == Integer.parseInt(selectedStudentId) ||
                            student.getUniqueId().equals(selectedStudentId)
                        ) selectedStudent = student;
                    } catch (NumberFormatException ex) {
                        flasher.flash(new Flash("Your selection is invalid. Press enter to continue.", FLASH_TYPES.ATTENTION));
                        inputScanner.nextLine();
                        break;
                    }

                taskDone = selectedStudent != null;
                if (!taskDone) {
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

    public List<Student> selectStudentsToAssign(Team teamToAssign, List<Student> students) {
        List<Student> selectedStudents = new ArrayList<>();
        flasher.flash(new Flash("Please select Students to assign into new Team.\n", FLASH_TYPES.NONE));

        boolean taskDone = false;
        while (!taskDone) {
            int selectableCount = SharedConstants.GROUP_LIMIT - selectedStudents.size() - teamToAssign.getMembers().size();
            if (selectableCount == 0)
                return selectedStudents;

            for (Student student : students)
                if (!teamToAssign.getMembers().contains(student) &&
                    !selectedStudents.contains(student)
                )
                    flasher.flash(new Flash("\t" + student.display(), FLASH_TYPES.NONE));


            flasher.flash(new Flash(
                "You can now select " + selectableCount + " more Student(s).\n" +
                        "Your selection: (press Enter to skip and go back or if you have done selection)",
                FLASH_TYPES.NONE
            ));

            String selectedStudentId = inputScanner.next();
            inputScanner.nextLine();

            if (selectedStudentId.length() == 0 || selectedStudentId.trim().equals(SharedConstants.EMPTY_STRING)) {
                taskDone = true;
                continue;
            }

            for (Student student : students) {
                if (teamToAssign.getMembers().contains(student) ||
                        selectedStudents.contains(student)
                ) continue;

                try {
                    if (student.getId() == Integer.parseInt(selectedStudentId) ||
                        student.getUniqueId().equals(selectedStudentId)
                    ) {
                        selectedStudents.add(student);
                        break;
                    }
                } catch (NumberFormatException ex) {
                    flasher.flash(new Flash("Your selection is invalid. Press enter to continue.", FLASH_TYPES.ATTENTION));
                    inputScanner.nextLine();
                }
            }
        }

        return null;
    }

    public void printFitnessMetricsTable(List<Team> teams) {
        flasher.flash(new Flash("\tTeam Fitness Metrics Table\n", FLASH_TYPES.NONE));

        for (Team team : teams) {
            flasher.flash(new Flash("\t" + team.display(), FLASH_TYPES.NONE));

            if (team.getFitnessMetrics() == null) {
                flasher.flash(new Flash(
                    "\t\tThis team do not have enough " + SharedConstants.GROUP_LIMIT + " Students. " +
                            "Fitness Metrics are not yet calculated.\n",
                    FLASH_TYPES.NONE
                ));
                continue;
            }

            flasher.flash(new Flash(team.getFitnessMetrics().display() + "\n", FLASH_TYPES.NONE));
        }

        flasher.flash(new Flash("\nEnd of Team Fitness Metrics Table. Press enter to continue.", FLASH_TYPES.NONE));
        inputScanner.nextLine();
    }
}
