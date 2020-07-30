package cosc1295.src.views;

import cosc1295.designs.Flasher;
import cosc1295.src.models.Flash;
import cosc1295.src.models.Project;
import cosc1295.src.models.ProjectOwner;
import helpers.commons.SharedEnums.SKILLS;
import helpers.commons.SharedEnums.RANKINGS;
import helpers.commons.SharedEnums.FLASH_TYPES;
import helpers.utilities.Helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ProjectView {

    private final Flasher flasher = Flasher.getInstance();
    private final Scanner inputScanner;

    public ProjectView() { inputScanner = new Scanner(System.in); }

    public Project getProjectDetails(List<ProjectOwner> projectOwners) {
        if (projectOwners.isEmpty()) {
            flasher.flash(new Flash(
                "Projects are required to have a Project Owner ID.\n" +
                        "However, no Project Owner has been added. Please add at least 1 first.\n" +
                        "Press enter to continue.",
                FLASH_TYPES.ATTENTION
            ));

            inputScanner.nextLine();
            return null;
        }

        Project project = new Project();
        flasher.flash(new Flash(
                "\t\tTASK: ADD PROJECT\n\t\tPlease enter the details of New Project\n",
                FLASH_TYPES.NONE
        ));

        int projectFieldTracker = 0;
        while (projectFieldTracker < 5) {
            switch (projectFieldTracker) {
                case 0:
                    flasher.flash(new Flash("Project ID: ", FLASH_TYPES.NONE));

                    project.setUniqueId(inputScanner.nextLine());
                    if (!project.validateAndPrettifyId()) {
                        flasher.flash(new Flash("Invalid ID. Please re-enter Project ID.", FLASH_TYPES.ERROR));
                        continue;
                    }

                    projectFieldTracker++;
                    break;
                case 1:
                    flasher.flash(new Flash("Project Title: ", FLASH_TYPES.NONE));

                    project.setProjectTitle(inputScanner.nextLine());
                    if (!project.validateAndPrettifyProjectTitle()) {
                        flasher.flash(new Flash("Project Title cannot be empty.", FLASH_TYPES.ERROR));
                        continue;
                    }

                    projectFieldTracker++;
                    break;
                case 2:
                    flasher.flash(new Flash("Project Description: ", FLASH_TYPES.NONE));

                    project.setBriefDescription(inputScanner.nextLine());
                    if (!project.validateAndPrettifyProjectDescription()) {
                        flasher.flash(new Flash("Project Description cannot be empty.", FLASH_TYPES.ERROR));
                        continue;
                    }

                    projectFieldTracker++;
                    break;
                case 3:
                    flasher.flash(new Flash("Project Owner: ", FLASH_TYPES.NONE));

                    for (ProjectOwner projectOwner : projectOwners)
                        flasher.flash(new Flash(
                                "\t" + projectOwner.getId() + ". " + projectOwner.getFullName(),
                                FLASH_TYPES.NONE
                        ));

                    String selectedOwnerId;
                    boolean setOwnerDone = false;
                    while (!setOwnerDone) {
                        flasher.flash(new Flash("\tSelect a Project Owner ID: ", FLASH_TYPES.NONE));

                        selectedOwnerId = inputScanner.next();
                        inputScanner.nextLine();

                        if (Helpers.isIntegerNumber(selectedOwnerId)) {
                            int id = Integer.parseInt(selectedOwnerId);

                            if (id <= 0) {
                                flasher.flash(new Flash("Invalid ID. Press enter to select again.", FLASH_TYPES.ATTENTION));
                                inputScanner.nextLine();

                                continue;
                            }

                            for (ProjectOwner projectOwner : projectOwners)
                                if (projectOwner.getId() == id) {
                                    project.setProjectOwner(projectOwner);
                                    setOwnerDone = true;
                                    break;
                                }

                            if (!setOwnerDone) {
                                boolean response = flasher.promptForConfirmation(new Flash(
                                        "Project Owner not found.\n" +
                                                "Do you wish to select again or go back to main menu?\n" +
                                                "Y: Select again\tN: Back to main menu",
                                        FLASH_TYPES.ATTENTION
                                ));

                                if (!response) return null;
                            }
                        }
                        else {
                            flasher.flash(new Flash("Invalid input! Press enter to select again.",FLASH_TYPES.ATTENTION));
                            inputScanner.nextLine();
                        }
                    }

                    projectFieldTracker++;
                    break;
                default:
                    flasher.flash(new Flash("Skill Ranking: ", FLASH_TYPES.NONE));
                    getSkillRankings();
                    //TODO
                    projectFieldTracker++;
                    break;
            }
        }

        return project;
    }

    public void printTaskResult(boolean result) {
        flasher.flash(
            result ? new Flash("The new project has been added successfully.\n", FLASH_TYPES.SUCCESS)
                   : new Flash("Unable to add new project due to an error.\n", FLASH_TYPES.NONE)
        );
    }

    private HashMap<SKILLS, RANKINGS> getSkillRankings() {
        //TODO
        return null;
    }
}
