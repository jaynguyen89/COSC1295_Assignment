package cosc1295.src.views;

import cosc1295.designs.Flasher;
import cosc1295.src.models.Flash;
import cosc1295.src.models.Project;
import cosc1295.src.models.ProjectOwner;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import helpers.commons.SharedEnums.RANKINGS;
import helpers.commons.SharedEnums.FLASH_TYPES;
import helpers.utilities.Helpers;

import javafx.util.Pair;
import java.util.*;

public class ProjectView {

    private final Flasher flasher = Flasher.getInstance();
    private final Scanner inputScanner;

    public ProjectView() { inputScanner = new Scanner(System.in); }

    /**
     * Gets user inputs to create a new Project.
     * Returns NULL to signal the app to return to Main Menu, otherwise,
     * the Project on all valid inputs.
     * @param projectOwners List<ProjectOwner>
     * @return Project
     */
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
                case 0: //Getting uniqueId
                    flasher.flash(new Flash("Project ID: ", FLASH_TYPES.NONE));

                    project.setUniqueId(inputScanner.nextLine());
                    Boolean idValidation = project.validateAndPrettifyId();
                    if (idValidation == null) flasher.flash(new Flash("Project ID cannot be empty!", FLASH_TYPES.ATTENTION));
                    else if (!idValidation) flasher.flash(new Flash("Project ID should not have special characters.", FLASH_TYPES.ATTENTION));

                    if (idValidation == null || !idValidation) continue; //Rerun this while statement, continue case 0

                    //Check if uniqueId is safe to use for creating new Project
                    Boolean idAvailable = project.isUniqueIdAvailable();
                    if (idAvailable == null) { //An exception was thrown while checking data in file
                        flasher.flash(new Flash("An error occurred while checking saved data.", FLASH_TYPES.ERROR));
                        boolean response = flasher.promptForConfirmation(new Flash(
                                        "Do you wish to try again or go back to main menu?\n" +
                                        "Y: Try again\tN: Back to main menu",
                                FLASH_TYPES.ATTENTION
                        ));

                        if (!response) return null; //Return to Main Menu
                        continue; //Rerun this while statement, continue case 0
                    }

                    if (!idAvailable) { //uniqueId is unsafe (already set for another Project)
                        flasher.flash(new Flash(
                            "Project ID " + project.getUniqueId() + " is duplicated. Please set another ID.\n" +
                                    "Press enter to continue.",
                            FLASH_TYPES.ERROR
                        ));

                        inputScanner.nextLine();
                        continue; //Rerun this while statement, continue case 0
                    }

                    //Up to here, the uniqueId is safe and set successfully, continue to case 1
                    projectFieldTracker++;
                    break;
                case 1: //Getting projectTitle
                    flasher.flash(new Flash("Project Title: ", FLASH_TYPES.NONE));

                    project.setProjectTitle(inputScanner.nextLine());
                    if (!project.validateAndPrettifyProjectTitle()) { //Input validation
                        flasher.flash(new Flash("Project Title cannot be empty.", FLASH_TYPES.ERROR));
                        continue;
                    }

                    //The projectTitle is set successfully, continue case 2
                    projectFieldTracker++;
                    break;
                case 2: //Getting projectDescription
                    flasher.flash(new Flash("Project Description: ", FLASH_TYPES.NONE));

                    project.setBriefDescription(inputScanner.nextLine());
                    if (!project.validateAndPrettifyProjectDescription()) { //Input validation
                        flasher.flash(new Flash("Project Description cannot be empty.", FLASH_TYPES.ERROR));
                        continue;
                    }

                    //The projectDescription is set successfully, continue case 3
                    projectFieldTracker++;
                    break;
                case 3: //Getting projectOwner
                    flasher.flash(new Flash("Project Owner: ", FLASH_TYPES.NONE));

                    //Print out table of Project Owners so user can select 1
                    for (ProjectOwner projectOwner : projectOwners)
                        flasher.flash(new Flash(
                                "\t" + projectOwner.getId() + ". " + projectOwner.getFullName(),
                                FLASH_TYPES.NONE
                        ));

                    String selectedOwnerId;
                    boolean setOwnerDone = false;
                    while (!setOwnerDone) {
                        flasher.flash(new Flash("\tSelect a Project Owner ID: ", FLASH_TYPES.NONE));

                        selectedOwnerId = inputScanner.next(); //Can be either id or uniqueId
                        inputScanner.nextLine();

                        if (Helpers.isIntegerNumber(selectedOwnerId)) { //id
                            int id = Integer.parseInt(selectedOwnerId);

                            if (id <= 0) { //Invalid id
                                flasher.flash(new Flash("Invalid ID. Press enter to select again.", FLASH_TYPES.ATTENTION));
                                inputScanner.nextLine();

                                continue; //Rerun this while statement
                            }

                            //Valid id, check if id associates to any of the Project Owners
                            for (ProjectOwner projectOwner : projectOwners)
                                if (projectOwner.getId() == id) { //found 1
                                    project.setProjectOwner(projectOwner); //set Project Owner
                                    setOwnerDone = true;
                                    break;
                                }

                            //Valid id, but not associate with any Project Owner
                            if (!setOwnerDone) {
                                boolean response = flasher.promptForConfirmation(new Flash(
                                        "Project Owner not found.\n" +
                                                "Do you wish to select again or go back to main menu?\n" +
                                                "Y: Select again\tN: Back to main menu",
                                        FLASH_TYPES.ATTENTION
                                ));

                                if (!response) return null; //==false, Return to Main Menu
                            }
                        }
                        else {
                            flasher.flash(new Flash("Invalid input! Press enter to select again.",FLASH_TYPES.ATTENTION));
                            inputScanner.nextLine();
                        }
                    }

                    //Up to here, the Project Owner is set successfully, continue to case 4
                    projectFieldTracker++;
                    break;
                default: //Getting skillRanking
                    flasher.flash(new Flash("Skill Ranking:\n1: Low\t2: Average\t3: High\t4: Highest", FLASH_TYPES.NONE));
                    project.setSkillRanking(getSkillRankings());

                    projectFieldTracker++;
                    break;
            }
        }

        return project;
    }

    public void printAddProjectTaskResult(boolean result) {
        flasher.flash(
            result ? new Flash("The new project has been added successfully.\n", FLASH_TYPES.SUCCESS)
                   : new Flash("Unable to add new project due to an error.\n", FLASH_TYPES.NONE)
        );

        flasher.flash(new Flash("Press enter to continue.", FLASH_TYPES.NONE));
        inputScanner.nextLine();
    }

    /**
     * Controls the flow of getting inputs for SKILLS-RANKINGS.
     * Returns HashMap containing the RANKINGS for 4 SKILLS.
     * @return HashMap<SKILLS, RANKINGS>
     */
    private HashMap<SKILLS, RANKINGS> getSkillRankings() {
        HashMap<SKILLS, RANKINGS> skillRankings = new HashMap<>();
        int skillInputTracker = 0; //To track RANKINGS inputs for 4 SKILLS

        RANKINGS ranking;
        List<RANKINGS> selectedRankings = new ArrayList<>();
        while (skillInputTracker < 4) {
            switch (skillInputTracker) {
                case 0: //Analytics skill
                    ranking = getRankingInput(skillInputTracker, selectedRankings);
                    skillRankings.put(SKILLS.A, ranking);

                    skillInputTracker++;
                    break;
                case 1: //Networking skill
                    ranking = getRankingInput(skillInputTracker, selectedRankings);
                    skillRankings.put(SKILLS.N, ranking);

                    skillInputTracker++;
                    break;
                case 2: //Programming skill
                    ranking = getRankingInput(skillInputTracker, selectedRankings);
                    skillRankings.put(SKILLS.P, ranking);

                    skillInputTracker++;
                    break;
                default: //Web skill
                    ranking = getRankingInput(skillInputTracker, selectedRankings);
                    skillRankings.put(SKILLS.W, ranking);

                    skillInputTracker++;
                    break;
            }

            selectedRankings.add(ranking);
        }


        return skillRankings;
    }

    /**
     * Actually gets the inputs of RANKINGS for a selected SKILLS.
     * The RANKINGS will be constrained by selectedRankings: a RANKINGS can only be selected once.
     * Returns the RANKINGS to be assigned to a SKILLS.
     * @param skill int
     * @param selectedRankings List<RANKINGS>
     * @return RANKINGS
     */
    private RANKINGS getRankingInput(int skill, List<RANKINGS> selectedRankings) {
        boolean validRanking = false;
        RANKINGS eRanking = null;

        while (!validRanking) {
            flasher.flash(new Flash(
                "\nRanking for " + (skill == 0 ? SKILLS.A.getValue()
                        : (skill == 1 ? SKILLS.N.getValue()
                        : (skill == 2 ? SKILLS.P.getValue() : SKILLS.W.getValue()))) + ": ",
                FLASH_TYPES.NONE
            ));

            String rankingString = inputScanner.next(); //Get a number input
            inputScanner.nextLine();

            if (Helpers.isIntegerNumber(rankingString)) { //Input is a number
                int ranking = Integer.parseInt(rankingString);

                if (ranking < 1 || ranking > 4) { //The number is out of RANKINGS range
                    flasher.flash(new Flash("Invalid ranking! Press enter to retry.", FLASH_TYPES.ERROR));
                    inputScanner.nextLine();
                    continue; //Rerun this while statement
                }

                eRanking = RANKINGS.values()[ranking - 1];
                if (selectedRankings.contains(eRanking)) { //The RANKINGS is prior selected
                    flasher.flash(new Flash(
                        "Ranking " + (eRanking.getValue() + 1) + " has been set previously for another skill.",
                        FLASH_TYPES.ATTENTION
                    ));
                    flasher.flash(new Flash("Please select another one. Press enter to continue.", FLASH_TYPES.NONE));

                    inputScanner.nextLine();
                    continue;
                }

                //The RANKINGS is valid to use, stop this while statement
                validRanking = true;
            }
            else { //Input is not a number
                flasher.flash(new Flash("Unrecognized input format! Press enter to retry.", FLASH_TYPES.ERROR));
                inputScanner.nextLine();
            }
        }

        return eRanking;
    }

    /**
     * Prompts user to select a Project from the Project List.
     * @param projects List<Project>
     * @return Project
     */
    public Project getProjectFromList(List<Project> projects) {
        if (projects.isEmpty()) {
            flasher.flash(new Flash(
                "No Project record has been saved: file `projects.txt` not found or empty.\n" +
                        "Please add at least 4 Projects before attempting this task.\n" +
                        "Press enter to continue.",
                FLASH_TYPES.ATTENTION
            ));

            inputScanner.nextLine();
            return null;
        }

        flasher.flash(new Flash("\nPlease select a project from the list.\n", FLASH_TYPES.NONE));
        Project selectedProject = null;

        //Print out the table of Projects so user can easily select
        for (Project project : projects)
            flasher.flash(new Flash("\t" + project.display(), FLASH_TYPES.NONE));

        String selectedProjectId = SharedConstants.EMPTY_STRING;
        while (selectedProjectId.isEmpty()) {
            flasher.flash(new Flash("\nSelected Project: ", FLASH_TYPES.NONE));

            selectedProjectId = inputScanner.next(); //Can be Project id or uniqueId
            inputScanner.nextLine();

            Pair<Project, Boolean> searchResult = searchProjectFromListByInput(selectedProjectId, projects);
            if (searchResult == null) return null; //Return to Main Menu
            else if (searchResult.getValue()) {
                selectedProjectId = SharedConstants.EMPTY_STRING;
                continue; //Rerun this while statement
            }

            selectedProject = searchResult.getKey();
        }

        return selectedProject;
    }

    /**
     * Checks if the selectedProjectId associates with any Project to select that Project from the list.
     * Return a Pair with Value to control the app flow, and Key being the the selected Project.
     * @param selectedProjectId Project
     * @param projects List<Project>
     * @return Pair<Project, Boolean>
     */
    private Pair<Project, Boolean> searchProjectFromListByInput(
        String selectedProjectId, //Can be either id or uniqueId
        List<Project> projects
    ) {
        boolean found = false;
        Project selectedProject = null;

        if (Helpers.isIntegerNumber(selectedProjectId)) { //id
            int projectId = Integer.parseInt(selectedProjectId);

            if (projectId <= 0) { //Invalid id
                flasher.flash(new Flash("Invalid ID. Press enter to select again.", FLASH_TYPES.ATTENTION));
                inputScanner.nextLine();

                return new Pair<>(null, true); //Rerun while statement
            }

            //Valid id, search for the associated Project
            for (Project project : projects)
                if (project.getId() == projectId) {
                    selectedProject = project;
                    found = true;
                    break;
                }
        } //uniqueId
        else for (Project project : projects)
            if (project.getUniqueId().trim().equalsIgnoreCase(selectedProjectId.trim())) {
                selectedProject = project;
                found = true;
                break;
            }

        if (!found) { //No Project associated with the selectedProjectId
            boolean response = flasher.promptForConfirmation(new Flash(
                "Project not found.\n" +
                        "Do you wish to select again or go back to main menu?\n" +
                        "Y: Select again\tN: Back to main menu",
                FLASH_TYPES.ATTENTION
            ));

            if (!response) return null; //Return to Main Menu
            else return new Pair<>(null, true); //rerun while statement
        }

        return new Pair<>(selectedProject, false);
    }

    public void printShortlistedProjects(List<Map.Entry<String, Integer>> shortlist) {
        flasher.flash(new Flash("\nPlease view below the shortlisted Projects:\n", FLASH_TYPES.NONE));
        flasher.flash(new Flash("\t-------------\n", FLASH_TYPES.NONE));

        for (Map.Entry<String, Integer> entry : shortlist) {
            flasher.flash(new Flash("\t" + entry.getKey() + "\t" + entry.getValue(), FLASH_TYPES.NONE));

            if (shortlist.indexOf(entry) == shortlist.size() - 1 - SharedConstants.SHORTLISTED_NUM)
                flasher.flash(new Flash("\n\tProjects with lowest preference:", FLASH_TYPES.NONE));
        }

        flasher.flash(new Flash("\n\t-------------\n", FLASH_TYPES.NONE));
        flasher.flash(new Flash("Press enter to continue.", FLASH_TYPES.NONE));

        inputScanner.nextLine();
    }
}
