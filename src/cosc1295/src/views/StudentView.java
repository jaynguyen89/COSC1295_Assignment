package cosc1295.src.views;

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

    public Student getStudentFromListToUpdate(List<Student> students) {
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
            "\t\tTASK: CAPTURE STUDENT PERSONALITY\n\t\tPlease select a student first from the list\n",
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

    private Student selectStudentToCapturePersonality(List<Student> students) {
        Student selectedStudent = null;

        for (Student student : students)
            flasher.flash(new Flash("\t" + student.toString(), FLASH_TYPES.NONE));

        String selectedStudentId = SharedConstants.EMPTY_STRING;
        while (selectedStudentId.isEmpty()) {
            flasher.flash(new Flash("\nSelected Student: ", FLASH_TYPES.NONE));

            selectedStudentId = inputScanner.next();
            inputScanner.nextLine();

            Pair<Student, Boolean> searchResult = searchStudentFromListByInput(selectedStudentId, students);
            if (searchResult == null) return null;
            else if (searchResult.getValue()) {
                selectedStudentId = SharedConstants.EMPTY_STRING;
                continue;
            }

            selectedStudent = searchResult.getKey();
        }

        return selectedStudent;
    }

    public Student captureStudentPersonality(Student student, List<Student> students) {
        HashMap<PERSONALITIES, Integer> personalityCounts = countPersonalityDistributions(students);

        flasher.flash(new Flash("Personality of Student " + student.getUniqueId() + ":\n", FLASH_TYPES.NONE));
        flasher.flash(new Flash(PERSONALITIES.display(), FLASH_TYPES.NONE));

        String personalityInput = SharedConstants.EMPTY_STRING;
        while (personalityInput.isEmpty()) {
            flasher.flash(new Flash("Your selection: " + student.getUniqueId() + ":\n", FLASH_TYPES.NONE));

            personalityInput = inputScanner.next();
            inputScanner.nextLine();

            PERSONALITIES personality = PERSONALITIES.getPersonality(personalityInput.trim().toUpperCase());
            if (personality == null) {
                flasher.flash(new Flash("Invalid input for personality. Press enter to continue.", FLASH_TYPES.ERROR));
                inputScanner.nextLine();

                personalityInput = SharedConstants.EMPTY_STRING;
                continue;
            }

            try {
                if (personalityCounts.get(personality) + 1 > students.size() / SharedConstants.GROUP_LIMIT) {
                    flasher.flash(new Flash(
                        "The number of this personality " + personality.name() + " exceeds the group distribution." +
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

                if (!response) return null;
                else personalityInput = SharedConstants.EMPTY_STRING;
            }

            student.setPersonality(personality);
            flasher.flash(new Flash("Personality is set successfully. Press enter to continue.", FLASH_TYPES.SUCCESS));
            inputScanner.nextLine();
        }

        return student;
    }

    public Student captureStudentConflicters(Student student, List<Student> students) {
        int conflicterCount = student.getConflicters().size();
        Student selectedConflict = null;

        flasher.flash(new Flash("Capture the conflicters of Student " + student.getUniqueId() + ".\n", FLASH_TYPES.NONE));

        String selectedConflicterId = SharedConstants.EMPTY_STRING;
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
            if (searchResult == null) return null;
            else if (searchResult.getValue()) continue;

            Student conflicter = searchResult.getKey();
            student.addConflicter(conflicter.getUniqueId());
            conflicterCount++;

            flasher.flash(new Flash(
                "The selected Student " + conflicter.getUniqueId() +
                " has been successfully added to Student " + student.getUniqueId() + "'s conflict.\n",
                FLASH_TYPES.SUCCESS
            ));

            if (conflicterCount < SharedConstants.MAX_CONFLICTERS) {
                boolean response = flasher.promptForConfirmation(new Flash(
                    "Do you want to add another? Y: Yes\tN: No",
                    FLASH_TYPES.NONE
                ));

                if (!response) return student;
            }
        }

        return student;
    }

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

    public void displayConflicterSkippingInformationFor(String uniqueId) {
        flasher.flash(new Flash(
                "Capturing conflicters will be skipped because Student " +
                        uniqueId + " has added 2 conflicters.",
                FLASH_TYPES.NONE
        ));

        flasher.flash(new Flash("Now proceed to saving this student. Press enter to continue.", FLASH_TYPES.NONE));
        inputScanner.nextLine();
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

    private Pair<Student, Boolean> searchStudentFromListByInput(
            String selectedStudentId,
            List<Student> students
    ) {
        boolean found = false;
        Student selectedStudent = null;

        if (Helpers.isIntegerNumber(selectedStudentId)) {
            int studentId = Integer.parseInt(selectedStudentId);

            if (studentId <= 0) {
                flasher.flash(new Flash("Invalid ID. Press enter to select again.", FLASH_TYPES.ATTENTION));
                inputScanner.nextLine();

                return new Pair<>(null, true);
            }

            for (Student student : students)
                if (student.getId() == studentId) {
                    selectedStudent = student;
                    found = true;
                    break;
                }
        }
        else for (Student student : students)
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

            if (!response) return null;
            else return new Pair<>(null, true);
        }

        return new Pair<>(selectedStudent, false);
    }

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
}
