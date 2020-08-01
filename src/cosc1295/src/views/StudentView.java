package cosc1295.src.views;

import com.sun.istack.internal.NotNull;
import cosc1295.designs.Flasher;
import cosc1295.src.models.Flash;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.FLASH_TYPES;
import helpers.commons.SharedEnums.PERSONALITIES;
import helpers.utilities.Helpers;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class StudentView {

    private final Flasher flasher = Flasher.getInstance();
    private final Scanner inputScanner;

    public StudentView() {
        inputScanner = new Scanner(System.in);
    }

    /**
     * Prompts user to select a Student from Student List before any task can be done on a student.
     * @param students List<Student>
     * @return Student
     */
    public Student getStudentFromListToUpdate(@NotNull List<Student> students) {
        if (students.isEmpty()) {
            flasher.flash(new Flash(
                "No Student record has been saved: file `students.txt` or `student_info.txt` not found or empty.\n" +
                        "Please add some Students prior to capturing their personality.\n" +
                        "Press enter to continue.",
                FLASH_TYPES.ATTENTION
            ));

            inputScanner.nextLine();
            return null;
        }

        flasher.flash(new Flash(
            "\t\tTASK: CAPTURE STUDENT PERSONALITY & PREFERENCES\n\t\t" +
                    "Please select a student first from the list\n",
            FLASH_TYPES.NONE
        ));

        return selectStudentToCapturePersonality(students);
    }

    public void printTaskResult(boolean result) {
        flasher.flash(new Flash(
                result ? "Student data have been saved successfully for Students.\nPress enter to continue."
                       : "An error occurred while saving the new Student data.\nPress enter to continue.",
                result ? FLASH_TYPES.SUCCESS : FLASH_TYPES.ERROR
        ));

        inputScanner.nextLine();
    }

    /**
     * Lets user select a Student from the Student List.
     * User can enter either `id` or uniqueId to select a Student.
     * @param students List<Student>
     * @return Student
     */
    private Student selectStudentToCapturePersonality(@NotNull List<Student> students) {
        Student selectedStudent = null;

        for (Student student : students)
            flasher.flash(new Flash("\t" + student.toString(), FLASH_TYPES.NONE));

        String selectedStudentId = SharedConstants.EMPTY_STRING;
        while (selectedStudentId.isEmpty()) {
            flasher.flash(new Flash("\nSelected Student: ", FLASH_TYPES.NONE));

            selectedStudentId = inputScanner.next();
            inputScanner.nextLine();

            Pair<Student, Boolean> searchResult = searchStudentFromListByInput(selectedStudentId, students);
            if (searchResult == null) return null; //Signal app to return to Main Menu
            else if (searchResult.getValue()) { //Rerun this while statement
                selectedStudentId = SharedConstants.EMPTY_STRING;
                continue;
            }

            selectedStudent = searchResult.getKey();
        }

        return selectedStudent;
    }

    /**
     * Prompts user to set a PERSONALITIES for the selected Student. Each PERSONALITIES can only be added up to
     * n times, with n = total(students)/4 assuming the number of students is always enough to make groups of 4 students.
     * Returns NULL to signal the app returning to Main Menu, otherwise, the updated Student having a PERSONALITIES.
     * @param student Student
     * @param students List<Student>
     * @return Student
     */
    public Student captureStudentPersonality(Student student, List<Student> students) {
        //Count the times a PERSONALITIES has been assigned
        HashMap<PERSONALITIES, Integer> personalityDistribution = countPersonalityDistributions(students);

        flasher.flash(new Flash("Personality of Student " + student.getUniqueId() + ":\n", FLASH_TYPES.NONE));
        flasher.flash(new Flash(PERSONALITIES.display(), FLASH_TYPES.NONE));

        String personalityInput = SharedConstants.EMPTY_STRING;
        while (personalityInput.isEmpty()) {
            flasher.flash(new Flash("Your selection for Student " + student.getUniqueId() + ":\n", FLASH_TYPES.NONE));

            personalityInput = inputScanner.next();
            inputScanner.nextLine();

            PERSONALITIES personality = PERSONALITIES.getPersonality(personalityInput.trim().toUpperCase());
            if (personality == null) {
                flasher.flash(new Flash("Invalid input for personality. Press enter to continue.", FLASH_TYPES.ERROR));
                inputScanner.nextLine();

                personalityInput = SharedConstants.EMPTY_STRING;
                continue;
            }

            if (personality == student.getPersonality()) {
                flasher.flash(new Flash(
                    "\nPersonality is same as current. No changes is made.\nPress enter to continue.",
                    FLASH_TYPES.NONE
                ));

                inputScanner.nextLine();
                return student;
            }

            try {
                //Limit each PERSONALITIES to not letting it exceed the computed distribution
                if (personalityDistribution.get(personality) + 1 > students.size() / SharedConstants.GROUP_LIMIT) {
                    flasher.flash(new Flash(
                        "The number of this personality " + personality.name() + " exceeds the group distribution.\n" +
                                "Please select again. Press enter to continue.",
                        FLASH_TYPES.ATTENTION
                    ));

                    inputScanner.nextLine();
                    personalityInput = SharedConstants.EMPTY_STRING;
                    continue;
                }
            } catch (NullPointerException ex) {
                flasher.flash(new Flash("An error occurred while mapping personality. Please try again.", FLASH_TYPES.ERROR));
                boolean response = flasher.promptForConfirmation(new Flash(
                                "Do you wish to retry or go back to main menu?\n" +
                                "Y: Retry\tN: Back to main menu",
                        FLASH_TYPES.ATTENTION
                ));

                if (!response) return null; //Signal app to return to Main Menu
                else personalityInput = SharedConstants.EMPTY_STRING; //Rerun this while statement
            }

            student.setPersonality(personality);
            flasher.flash(new Flash("Personality is set successfully. Press enter to continue.", FLASH_TYPES.SUCCESS));
            inputScanner.nextLine();
        }

        return student;
    }

    public boolean promptToCaptureConflicters(Student student) {
        return flasher.promptForConfirmation(new Flash(
                "Student " + student.getUniqueId() + " has had " + student.getConflicters().size() + " conflicters.\n" +
                "Do you wish to continue with capturing/updating Student's conflicters?\n" +
                "Y: Yes\tN: no",
                FLASH_TYPES.NONE
        ));
    }

    /**
     * Set the uniqueId as the Conflicter for a selected student. Allows for adding up to 2 Conflicters.
     * Returns NULL to signal the app returning to Main Menu, otherwise, the updated Student having Conflicter.
     * @param student Student
     * @param students List<Student>
     * @return Student
     */
    public Student captureStudentConflicters(Student student, List<Student> students) {
        int conflicterCount = student.getConflicters().size();
        flasher.flash(new Flash("Capture the conflicters of Student " + student.getUniqueId() + ".\n", FLASH_TYPES.NONE));

        String selectedConflicterId;
        while (conflicterCount < SharedConstants.MAX_CONFLICTERS) {
            flasher.flash(new Flash(
                "You can now add up to " + (SharedConstants.MAX_CONFLICTERS - conflicterCount) + " more conflicter(s).\n",
                FLASH_TYPES.NONE
            ));

            List<Student> possibleConflicters = printConflictersTableFor(student, students);
            flasher.flash(new Flash("\nConflicter No." + (conflicterCount + 1) + ": ", FLASH_TYPES.NONE));

            selectedConflicterId = inputScanner.next();
            inputScanner.nextLine();

            Pair<Student, Boolean> searchResult = searchStudentFromListByInput(selectedConflicterId, possibleConflicters);
            if (searchResult == null) return null; //Signal app to return to Main Menu
            else if (searchResult.getValue()) continue; //Rerun this while statement

            Student conflicter = searchResult.getKey();
            student.addConflicter(conflicter.getUniqueId());
            conflicterCount++;

            flasher.flash(new Flash(
                "The selected Student " + conflicter.getUniqueId() +
                " has been successfully added to Student " + student.getUniqueId() + "'s conflict.\n",
                FLASH_TYPES.SUCCESS
            ));

            //User can add only 1 Conflicter, so prompt if they want to add the second one.
            if (conflicterCount < SharedConstants.MAX_CONFLICTERS) {
                boolean response = flasher.promptForConfirmation(new Flash(
                    "Do you want to add another? Y: Yes\tN: No",
                    FLASH_TYPES.NONE
                ));

                if (!response) return student; //User don't want
            }
        }

        return student;
    }

    //This method is quite simple to understand right?
    private HashMap<PERSONALITIES, Integer> countPersonalityDistributions(List<Student> students) {
        HashMap<PERSONALITIES, Integer> personalityCounts = new HashMap<PERSONALITIES, Integer>() {{
            put(PERSONALITIES.A, 0);
            put(PERSONALITIES.B, 0);
            put(PERSONALITIES.C, 0);
            put(PERSONALITIES.D, 0);
        }};

        for (Student student : students) {
            if (student.getPersonality() == PERSONALITIES.A) {
                int count = personalityCounts.get(PERSONALITIES.A);
                personalityCounts.replace(PERSONALITIES.A, ++count);
            }

            if (student.getPersonality() == PERSONALITIES.B) {
                int count = personalityCounts.get(PERSONALITIES.B);
                personalityCounts.replace(PERSONALITIES.B, ++count);
            }

            if (student.getPersonality() == PERSONALITIES.C) {
                int count = personalityCounts.get(PERSONALITIES.C);
                personalityCounts.replace(PERSONALITIES.C, ++count);
            }

            if (student.getPersonality() == PERSONALITIES.D) {
                int count = personalityCounts.get(PERSONALITIES.D);
                personalityCounts.replace(PERSONALITIES.D, ++count);
            }
        }

        return personalityCounts;
    }

    private List<Student> printConflictersTableFor(Student student, List<Student> students) {
        List<Student> possibilities = new ArrayList<>();

        for (Student entry : students) {
            if (student.getConflicters().contains(entry.getUniqueId()) ||
                entry.getUniqueId().equals(student.getUniqueId())
            ) continue;

            possibilities.add(entry);
            flasher.flash(new Flash("\t" + entry.toString(), FLASH_TYPES.NONE));
        }

        return possibilities;
    }

    /**
     * Searches for a Student from Student List given selectedStudentId, which can be either
     * the uniqueId or the numeric id as seen in Student class.
     * Return a Pair with the Value to control the app flow, and the Key being the Student.
     * @param selectedStudentId String
     * @param students List<Student>
     * @return Pair<Student, Boolean>
     */
    private Pair<Student, Boolean> searchStudentFromListByInput(
            String selectedStudentId,
            List<Student> students
    ) {
        boolean found = false;
        Student selectedStudent = null;

        if (Helpers.isIntegerNumber(selectedStudentId)) { //selectedStudentId is numeric id
            int studentId = Integer.parseInt(selectedStudentId);

            if (studentId <= 0) {
                flasher.flash(new Flash("Invalid ID. Press enter to select again.", FLASH_TYPES.ATTENTION));
                inputScanner.nextLine();

                return new Pair<>(null, true); //Rerun while statement
            }

            for (Student student : students)
                if (student.getId() == studentId) {
                    selectedStudent = student;
                    found = true;
                    break;
                }
        }
        else //selectedStudentId is the uniqueId
            for (Student student : students)
            if (student.getUniqueId().trim().equalsIgnoreCase(selectedStudentId.trim())) {
                selectedStudent = student;
                found = true;
                break;
            }

        if (!found) {
            boolean response = flasher.promptForConfirmation(new Flash(
                "Student not found.\n" +
                        "Do you wish to select again or go back to main menu?\n" +
                        "Y: Select again\tN: Back to main menu",
                FLASH_TYPES.ATTENTION
            ));

            if (!response) return null; //Return to Main Menu
            else return new Pair<>(null, true); //Rerun while statement
        }

        return new Pair<>(selectedStudent, false);
    }

    /**
     * Sets a PREFERENCES to a selected Project for a Student.
     * @param student Student
     * @param project Project
     * @param preference Preference
     * @return Preference
     */
    public Preference captureStudentPreferences(Student student, Project project, Preference preference) {
        flasher.flash(new Flash(
            "\nPlease set a preference for " + project.getUniqueId() +
                    " by Student " + student.getUniqueId(),
            FLASH_TYPES.NONE
        ));
        flasher.flash(new Flash(
            "1. Disliked\t2. Neutral\t3. Preferred\t4. Most Preferred\n",
            FLASH_TYPES.NONE
        ));

        while (true) {
            flasher.flash(new Flash("Your selection: ", FLASH_TYPES.NONE));
            String selection = inputScanner.next();
            inputScanner.nextLine();

            if (Helpers.isIntegerNumber(selection)) {
                int pref = Integer.parseInt(selection);

                if (pref > 0 && pref < 5) {
                    preference.addPreference(new Pair<>(project.getUniqueId(), pref));
                    break;
                }
            }

            flasher.flash(new Flash("Invalid input for preference. Press enter to continue.", FLASH_TYPES.ATTENTION));
            inputScanner.nextLine();
        }

        return preference;
    }

    public boolean promptToCapturePersonality(Student student) {
        return flasher.promptForConfirmation(new Flash(
                "Student " + student.getUniqueId() + " has had Personality " + student.getPersonality().getValue() + ".\n" +
                        "Do you wish to update? Y: Yes N: no",
                FLASH_TYPES.NONE
        ));
    }

    /**
     * Starts the updating Conflicters task: user can select to replace a current Conflicter or add more.
     * Returns the Student having updated the Conflicter.
     * @param student Student
     * @param students List<Student>
     * @return Student
     */
    public Student updateStudentConflicters(Student student, List<Student> students) {
        flasher.flash(new Flash("\nUpdate the conflicters for Student " + student.getUniqueId() + ".\n", FLASH_TYPES.NONE));

        boolean shouldReplaceConflicter = true;
        if (student.getConflicters().size() == 2) //Student has 2 Conflicter, can only replace
            flasher.flash(new Flash(
                "Student " + student.getUniqueId() + " has had 2 conflicters.\n" +
                        "You can now select a conflicter to replace:\n",
                FLASH_TYPES.NONE
            ));
        else //Student has 1 Conflicter, can select to replace or add more
            shouldReplaceConflicter = flasher.promptForConfirmation(new Flash(
                    "Student " + student.getUniqueId() + " has had 1 conflicter.\n" +
                            "Do you want to replace the current one or add 1 more?" +
                            "Y: Replace\tN: Add",
                FLASH_TYPES.ATTENTION
            ));

        if (shouldReplaceConflicter) student = replaceConflictersFor(student, students);
        else student = captureStudentConflicters(student, students);

        return student;
    }

    /**
     * Allows for replacing the Conflicter(s). User can keep replacing until whenever they are done.
     * Returns the Student having newly updated Conflicter(s).
     * @param student Student
     * @param students List<Student>
     * @return Student
     */
    private Student replaceConflictersFor(Student student, List<Student> students) {
        boolean replaceDone = false;

        while (!replaceDone) {
            flasher.flash(new Flash("\t" + student.getConflicters().toString(), FLASH_TYPES.NONE));
            flasher.flash(new Flash("\nYour selection: ", FLASH_TYPES.NONE));

            String selectedToReplace = inputScanner.next();
            inputScanner.nextLine();

            //Select a Conflicter to be replaced
            selectedToReplace = selectedToReplace.trim().toUpperCase();
            if (!student.getConflicters().contains(selectedToReplace)) {
                flasher.flash(new Flash("Invalid selection. Press enter to continue.", FLASH_TYPES.ATTENTION));

                inputScanner.nextLine();
                continue;
            }

            //Select a new Conflicter to replace
            flasher.flash(new Flash("\nSelect a new conflicter:\n", FLASH_TYPES.NONE));
            List<Student> possibleConflicters = printConflictersTableFor(student, students);
            flasher.flash(new Flash("\nYour selection:", FLASH_TYPES.NONE));

            String replaceById = inputScanner.next();
            inputScanner.nextLine();

            //Check if the new Conflicter is a valid selection
            Pair<Student, Boolean> searchResult = searchStudentFromListByInput(replaceById, possibleConflicters);

            //Invalid selection, then...
            if (searchResult == null) return null; //Return to Main Menu
            else if (searchResult.getValue()) continue; //Rerun this while statement

            //Valid selection, then...
            student.getConflicters().remove(selectedToReplace);
            student.getConflicters().add(searchResult.getKey().getUniqueId());

            replaceDone = !flasher.promptForConfirmation(new Flash(
                "Conflicter " + selectedToReplace + " has been replaced by new Conflicter " +
                        searchResult.getKey().getUniqueId() + " for Student " + student.getUniqueId() + "\n" +
                        "Do you wish to replace more? Y: Yes\tN: No",
                FLASH_TYPES.SUCCESS
            ));
        }

        return student;
    }
}
