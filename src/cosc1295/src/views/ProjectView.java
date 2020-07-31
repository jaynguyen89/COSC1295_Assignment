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
                        "However, no Project Owner has been added. Please add at least 1 Project Owner first.\n" +
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
                    Boolean idValidation = project.validateAndPrettifyId();
                    if (idValidation == null) flasher.flash(new Flash("Project ID cannot be empty!", FLASH_TYPES.ATTENTION));
                    else if (!idValidation) flasher.flash(new Flash("Project ID should not have special characters.", FLASH_TYPES.ATTENTION));

                    if (idValidation == null || !idValidation) continue;

                    Boolean idAvailable = project.isUniqueIdAvailable();
                    if (idAvailable == null) {
                        flasher.flash(new Flash("An error occurred while checking saved data.", FLASH_TYPES.ERROR));
                        boolean response = flasher.promptForConfirmation(new Flash(
                                        "Do you wish to try again or go back to main menu?\n" +
                                        "Y: Try again\tN: Back to main menu",
                                FLASH_TYPES.ATTENTION
                        ));

                        if (!response) return null;
                        continue;
                    }

                    if (!idAvailable) {
                        flasher.flash(new Flash(
                            "Project ID " + project.getUniqueId() + " is duplicated. Please set another ID.\n" +
                                    "Press enter to continue.",
                            FLASH_TYPES.ERROR
                        ));

                        inputScanner.nextLine();
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
                    flasher.flash(new Flash("Skill Ranking:\n1: Low\t2: Average\t3: High\t4: Highest", FLASH_TYPES.NONE));
                    project.setSkillRanking(getSkillRankings());

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

        flasher.flash(new Flash("Press enter to continue.", FLASH_TYPES.NONE));
        inputScanner.nextLine();
    }

    private HashMap<SKILLS, RANKINGS> getSkillRankings() {
        HashMap<SKILLS, RANKINGS> skillRankings = new HashMap<>();
        int skillInputTracker = 0;

        RANKINGS ranking;
        while (skillInputTracker < 4) {
            switch (skillInputTracker) {
                case 0:
                    ranking = getRankingInput(skillInputTracker);
                    skillRankings.put(SKILLS.A, ranking);

                    skillInputTracker++;
                    break;
                case 1:
                    ranking = getRankingInput(skillInputTracker);
                    skillRankings.put(SKILLS.N, ranking);

                    skillInputTracker++;
                    break;
                case 2:
                    ranking = getRankingInput(skillInputTracker);
                    skillRankings.put(SKILLS.P, ranking);

                    skillInputTracker++;
                    break;
                default:
                    ranking = getRankingInput(skillInputTracker);
                    skillRankings.put(SKILLS.W, ranking);

                    skillInputTracker++;
                    break;
            }
        }


        return skillRankings;
    }

    private RANKINGS getRankingInput(int skill) {
        boolean validRanking = false;
        RANKINGS eRanking = null;

        while (!validRanking) {
            flasher.flash(new Flash(
                    "\tRanking for " + (skill == 0 ? SKILLS.A.getValue()
                            : (skill == 1 ? SKILLS.N.getValue()
                            : (skill == 2 ? SKILLS.P.getValue() : SKILLS.W.getValue()))) + ": ",
                    FLASH_TYPES.NONE
            ));

            String rankingString = inputScanner.next();
            inputScanner.nextLine();

            if (Helpers.isIntegerNumber(rankingString)) {
                int ranking = Integer.parseInt(rankingString);

                if (ranking < 1 || ranking > 4) {
                    flasher.flash(new Flash("Invalid ranking! Press enter to retry.", FLASH_TYPES.ERROR));
                    inputScanner.nextLine();
                    continue;
                }

                eRanking = RANKINGS.values()[ranking - 1];
                validRanking = true;
            }
            else {
                flasher.flash(new Flash("Unrecognized input format! Press enter to retry.", FLASH_TYPES.ERROR));
                inputScanner.nextLine();
            }
        }

        return eRanking;
    }
}
