package cosc1295.src.views;

import cosc1295.designs.Flasher;
import cosc1295.src.models.Company;
import cosc1295.src.models.Flash;
import cosc1295.src.models.ProjectOwner;
import cosc1295.src.models.Role;
import helpers.commons.SharedEnums.FLASH_TYPES;
import helpers.utilities.Helpers;

import javafx.util.Pair;
import java.util.List;
import java.util.Scanner;

public class ProjectOwnerView {

    private final Flasher flasher = Flasher.getInstance();
    private final Scanner inputScanner;

    public ProjectOwnerView() { inputScanner = new Scanner(System.in); }

    /**
     * Gets user inputs to create a new Project Owner having associated Role and Company.
     * Returns a Pair with Value to control the app flow, and Key being the new Project Owner.
     * @param roles List<Role>
     * @param companies List<Company>
     * @return Pair<ProjectOwner, Boolean>
     */
    public Pair<ProjectOwner, Boolean> getProjectOwnerDetails(List<Role> roles, List<Company> companies) {
        if (companies.isEmpty()) {
            flasher.flash(new Flash(
                "Project Owners are required to have a Company ID.\n" +
                "However, no company has been added. Please add at least 1 company first.\n" +
                "Press enter to continue.",
                FLASH_TYPES.ATTENTION
            ));

            inputScanner.nextLine();
            return null;
        }

        ProjectOwner projectOwner = new ProjectOwner();

        flasher.flash(new Flash(
            "\t\tTASK: ADD PROJECT OWNER\n\t\tPlease enter the details of New Project Owner\n",
            FLASH_TYPES.NONE
        ));

        int projectOwnerFieldTracker = 0;
        boolean isNewRoleCreated = false;
        while (projectOwnerFieldTracker < 6) {
            switch (projectOwnerFieldTracker) {
                case 0: //Getting firstName
                    flasher.flash(new Flash("First Name: (allowed: '.-)", FLASH_TYPES.NONE));

                    projectOwner.setFirstName(inputScanner.nextLine());
                    Boolean fnameValidation = projectOwner.validateAndPrettifyFirstOrLastName(projectOwner.getFirstName(), true);

                    if (fnameValidation == null) flasher.flash(new Flash("First Name cannot be empty!", FLASH_TYPES.ERROR));
                    else if (!fnameValidation) flasher.flash(new Flash("First Name contains disallowed special character!", FLASH_TYPES.ERROR));

                    if (fnameValidation == null || !fnameValidation) continue; //Rerun this while statement, continue case 0
                    projectOwnerFieldTracker++;
                    break;
                case 1: //Getting lastName
                    flasher.flash(new Flash("Last Name: (allowed: '.-)", FLASH_TYPES.NONE));

                    projectOwner.setLastName(inputScanner.nextLine());
                    Boolean lnameValidation = projectOwner.validateAndPrettifyFirstOrLastName(projectOwner.getLastName(), false);

                    if (lnameValidation == null) flasher.flash(new Flash("Last Name cannot be empty!", FLASH_TYPES.ERROR));
                    else if (!lnameValidation) flasher.flash(new Flash("Last Name contains disallowed special character!", FLASH_TYPES.ERROR));

                    if (lnameValidation == null || !lnameValidation) continue; //Rerun this while statement, continue case 1
                    projectOwnerFieldTracker++;
                    break;
                case 2: //Getting emailAddress
                    flasher.flash(new Flash("Email Address: ", FLASH_TYPES.NONE));

                    projectOwner.setEmailAddress(inputScanner.nextLine());
                    Boolean emailValidation = projectOwner.validateAndPrettifyEmailAddress();

                    if (emailValidation == null) flasher.flash(new Flash("Email Address cannot be empty!", FLASH_TYPES.ERROR));
                    else if (!emailValidation) flasher.flash(new Flash("Email Address seems to be invalid!", FLASH_TYPES.ERROR));

                    if (emailValidation == null || !emailValidation) continue; //Rerun this while statement, continue case 2
                    projectOwnerFieldTracker++;
                    break;
                case 3: //Getting role
                    boolean selection = true;

                    //If no roles was recorded, prompt to enter role, otherwise, user can select a role or enter new one
                    if (roles.isEmpty()) flasher.flash(new Flash("Role: ", FLASH_TYPES.NONE));
                    else selection = flasher.promptForConfirmation(new Flash(
                            "Role:\n\tDo you wish to select from saved roles or enter your own?" +
                                    "\n\tY: Select saved roles\n\tN: Enter your own",
                            FLASH_TYPES.NONE
                        ));

                    //When no role was recorded or when user want to input a role
                    if (roles.isEmpty() || !selection) {
                        if (!selection) flasher.flash(new Flash("Enter your role: ", FLASH_TYPES.NONE));

                        //Read setRole documentation for more information
                        isNewRoleCreated = projectOwner.setRole(inputScanner.nextLine(), false);
                    }
                    else { //When some roles were recorded and user want to select a role
                        for (Role role : roles) //Print out table of roles
                            flasher.flash(new Flash(
                                "\t" + role.getId() + ". " + role.getRole(),
                                FLASH_TYPES.NONE
                            ));

                        boolean setRoleSuccess = false;
                        while (!setRoleSuccess) {
                            flasher.flash(new Flash("\nEnter role ID: ", FLASH_TYPES.NONE));
                            String roleIdString = inputScanner.next();
                            inputScanner.nextLine();

                            setRoleSuccess = projectOwner.setRole(roleIdString, true); //Read setRole documentation
                            if (!setRoleSuccess) {
                                flasher.flash(new Flash("Invalid input for Role ID. Press enter to retry.", FLASH_TYPES.ATTENTION));
                                inputScanner.nextLine();
                            }
                            else {
                                boolean found = false;
                                for (Role role : roles) //Check if the selected role is of a role that was recorded
                                    if (role.getId() == Integer.parseInt(roleIdString)) {
                                        found = true;
                                        break;
                                    }

                                if (found) continue; //Role is set successfully, stop while statement
                                flasher.flash(new Flash("Role ID not found. Press enter to continue.", FLASH_TYPES.ATTENTION));
                                inputScanner.nextLine();

                                boolean response = flasher.promptForConfirmation(new Flash(
                                    "Do you wish to select Role again or go back to previous menu?\n" +
                                    "Y: Select again\tN: Back to previous menu",
                                    FLASH_TYPES.ATTENTION
                                ));

                                if (!response) { //User wants to return to the outer while at line 51
                                    projectOwnerFieldTracker--; //...and continue case 3
                                    setRoleSuccess = true; //...so end this while statement
                                }
                            }
                        }
                    }

                    projectOwnerFieldTracker++;
                    break;
                case 4: //Getting uniqueId
                    flasher.flash(new Flash("Unique ID: (no special characters)", FLASH_TYPES.NONE));

                    projectOwner.setUniqueId(inputScanner.nextLine());
                    Boolean idValidation = projectOwner.validateAndPrettifyUniqueId(); //Validation
                    if (idValidation == null) flasher.flash(new Flash("Unique ID cannot be empty!", FLASH_TYPES.ATTENTION));
                    else if (!idValidation) flasher.flash(new Flash("Unique ID should not have special characters.", FLASH_TYPES.ATTENTION));

                    if (idValidation == null || !idValidation) continue; //Rerun this while statement, continue case 4

                    Boolean idAvailable = projectOwner.isUniqueIdAvailable(); //Check if the uniqueId is safe to create new Project Owner
                    if (idAvailable == null) { //An exception was thrown while checking saved data
                        flasher.flash(new Flash("An error occurred while checking saved data.", FLASH_TYPES.ERROR));
                        boolean response = flasher.promptForConfirmation(new Flash(
                            "Do you wish to try again or go back to main menu?\n" +
                                    "Y: Try again\tN: Back to main menu",
                            FLASH_TYPES.ATTENTION
                        ));

                        if (!response) return null; //Return to Main Menu
                        continue; //Rerun this while statement, continue case 4
                    }

                    if (!idAvailable) { //uniqueId is unsafe
                        flasher.flash(new Flash(
                            "Project Owner ID " + projectOwner.getUniqueId() + " is duplicated. Please set another ID.\n" +
                                    "Press enter to continue.",
                            FLASH_TYPES.ERROR
                        ));

                        inputScanner.nextLine();
                        continue; //Rerun this while statement, continue case 4
                    }

                    //Up to here, uniqueId is safe and set successfully
                    projectOwnerFieldTracker++;
                    break;
                default: //Getting company
                    flasher.flash(new Flash("Company: ", FLASH_TYPES.NONE));

                    for (Company company : companies) //Print out table of companies
                        flasher.flash(new Flash("\t" + company.toString(), FLASH_TYPES.NONE));

                    String selectedCompanyId;
                    boolean setCompanyDone = false;
                    while (!setCompanyDone) {
                        flasher.flash(new Flash("\tSelect a Company ID: ", FLASH_TYPES.NONE));

                        selectedCompanyId = inputScanner.next(); //Can be id or uniqueId
                        inputScanner.nextLine();

                        if (Helpers.isIntegerNumber(selectedCompanyId)) { //id
                            int id = Integer.parseInt(selectedCompanyId);

                            if (id <= 0) { //Invalid id
                                flasher.flash(new Flash("Invalid ID. Press enter to select again.", FLASH_TYPES.ATTENTION));
                                inputScanner.nextLine();

                                continue; //Rerun this while statement, continue this case
                            }

                            for (Company company : companies) //Valid id, check for an associated company
                                if (company.getId() == id) {
                                    projectOwner.setCompany(company);
                                    setCompanyDone = true;
                                    break;
                                }

                            if (!setCompanyDone) { //No associated company
                                boolean response = flasher.promptForConfirmation(new Flash(
                                    "Company not found.\n" +
                                            "Do you wish to select again or go back to main menu?\n" +
                                            "Y: Select again\tN: Back to main menu",
                                    FLASH_TYPES.ATTENTION
                                ));

                                if (!response) return null; //User wants to return to Main Menu
                            }
                        }
                        else {
                            flasher.flash(new Flash("Invalid input! Press enter to select again.",FLASH_TYPES.ATTENTION));
                            inputScanner.nextLine();
                        }
                    }

                    projectOwnerFieldTracker++;
                    break;
            }
        }

        return new Pair<>(projectOwner, isNewRoleCreated);
    }

    public void printTaskResult(Boolean taskResult) {
        flasher.flash(new Flash(
                taskResult ? "The new Project Owner has been saved successfully.\nPress enter to continue."
                           : "An error occurred while saving the new Project Owner.\nPress enter to continue.",
                taskResult ? FLASH_TYPES.SUCCESS : FLASH_TYPES.ERROR
        ));

        inputScanner.nextLine();
    }
}
