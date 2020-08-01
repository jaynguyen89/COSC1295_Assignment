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
                case 0:
                    flasher.flash(new Flash("First Name: (allowed: '.-)", FLASH_TYPES.NONE));

                    projectOwner.setFirstName(inputScanner.nextLine());
                    Boolean fnameValidation = projectOwner.validateAndPrettifyFirstOrLastName(projectOwner.getFirstName(), true);

                    if (fnameValidation == null) flasher.flash(new Flash("First Name cannot be empty!", FLASH_TYPES.ERROR));
                    else if (!fnameValidation) flasher.flash(new Flash("First Name contains disallowed special character!", FLASH_TYPES.ERROR));

                    if (fnameValidation == null || !fnameValidation) continue;
                    projectOwnerFieldTracker++;
                    break;
                case 1:
                    flasher.flash(new Flash("Last Name: (allowed: '.-)", FLASH_TYPES.NONE));

                    projectOwner.setLastName(inputScanner.nextLine());
                    Boolean lnameValidation = projectOwner.validateAndPrettifyFirstOrLastName(projectOwner.getLastName(), false);

                    if (lnameValidation == null) flasher.flash(new Flash("Last Name cannot be empty!", FLASH_TYPES.ERROR));
                    else if (!lnameValidation) flasher.flash(new Flash("Last Name contains disallowed special character!", FLASH_TYPES.ERROR));

                    if (lnameValidation == null || !lnameValidation) continue;
                    projectOwnerFieldTracker++;
                    break;
                case 2:
                    flasher.flash(new Flash("Email Address: ", FLASH_TYPES.NONE));

                    projectOwner.setEmailAddress(inputScanner.nextLine());
                    Boolean emailValidation = projectOwner.validateAndPrettifyEmailAddress();

                    if (emailValidation == null) flasher.flash(new Flash("Email Address cannot be empty!", FLASH_TYPES.ERROR));
                    else if (!emailValidation) flasher.flash(new Flash("Email Address seems to be invalid!", FLASH_TYPES.ERROR));

                    if (emailValidation == null || !emailValidation) continue;
                    projectOwnerFieldTracker++;
                    break;
                case 3:
                    boolean selection = true;
                    if (roles.isEmpty()) flasher.flash(new Flash("Role: ", FLASH_TYPES.NONE));
                    else selection = flasher.promptForConfirmation(new Flash(
                            "Role:\n\tDo you wish to select from saved roles or enter your own?" +
                                    "\n\tY: Select saved roles\n\tN: Enter your own",
                            FLASH_TYPES.NONE
                        ));

                    if (roles.isEmpty() || !selection) {
                        if (!selection) flasher.flash(new Flash("Enter your role: ", FLASH_TYPES.NONE));
                        isNewRoleCreated = projectOwner.setRole(inputScanner.nextLine(), false);
                    }
                    else {
                        for (Role role : roles)
                            flasher.flash(new Flash(
                                "\t" + role.getId() + ". " + role.getRole(),
                                FLASH_TYPES.NONE
                            ));

                        boolean setRoleSuccess = false;
                        while (!setRoleSuccess) {
                            flasher.flash(new Flash("\nEnter role ID: ", FLASH_TYPES.NONE));
                            String roleIdString = inputScanner.next();
                            inputScanner.nextLine();

                            setRoleSuccess = projectOwner.setRole(roleIdString, true);
                            if (!setRoleSuccess) {
                                flasher.flash(new Flash("Invalid input for Role ID. Press enter to retry.", FLASH_TYPES.ATTENTION));
                                inputScanner.nextLine();
                            }
                            else {
                                boolean found = false;
                                for (Role role : roles)
                                    if (role.getId() == Integer.parseInt(roleIdString)) {
                                        found = true;
                                        break;
                                    }

                                if (found) continue;
                                flasher.flash(new Flash("Role ID not found. Press enter to continue.", FLASH_TYPES.ATTENTION));
                                inputScanner.nextLine();

                                boolean response = flasher.promptForConfirmation(new Flash(
                                                "Do you wish to select Role again or go back to previous menu?\n" +
                                                "Y: Select again\tN: Back to previous menu",
                                        FLASH_TYPES.ATTENTION
                                ));

                                if (!response) {
                                    projectOwnerFieldTracker--;
                                    setRoleSuccess = true;
                                }
                            }
                        }
                    }

                    projectOwnerFieldTracker++;
                    break;
                case 4:
                    flasher.flash(new Flash("Unique ID: (no special characters)", FLASH_TYPES.NONE));

                    projectOwner.setUniqueId(inputScanner.nextLine());
                    Boolean idValidation = projectOwner.validateAndPrettifyUniqueId();
                    if (idValidation == null) flasher.flash(new Flash("Unique ID cannot be empty!", FLASH_TYPES.ATTENTION));
                    else if (!idValidation) flasher.flash(new Flash("Unique ID should not have special characters.", FLASH_TYPES.ATTENTION));

                    if (idValidation == null || !idValidation) continue;

                    Boolean idAvailable = projectOwner.isUniqueIdAvailable();
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
                                "Project Owner ID " + projectOwner.getUniqueId() + " is duplicated. Please set another ID.\n" +
                                        "Press enter to continue.",
                                FLASH_TYPES.ERROR
                        ));

                        inputScanner.nextLine();
                        continue;
                    }

                    projectOwnerFieldTracker++;
                    break;
                default:
                    flasher.flash(new Flash("Company: ", FLASH_TYPES.NONE));

                    for (Company company : companies)
                        flasher.flash(new Flash("\t" + company.toString(), FLASH_TYPES.NONE));

                    String selectedCompanyId;
                    boolean setCompanyDone = false;
                    while (!setCompanyDone) {
                        flasher.flash(new Flash("\tSelect a Company ID: ", FLASH_TYPES.NONE));

                        selectedCompanyId = inputScanner.next();
                        inputScanner.nextLine();

                        if (Helpers.isIntegerNumber(selectedCompanyId)) {
                            int id = Integer.parseInt(selectedCompanyId);

                            if (id <= 0) {
                                flasher.flash(new Flash("Invalid ID. Press enter to select again.", FLASH_TYPES.ATTENTION));
                                inputScanner.nextLine();

                                continue;
                            }

                            for (Company company : companies)
                                if (company.getId() == id) {
                                    projectOwner.setCompany(company);
                                    setCompanyDone = true;
                                    break;
                                }

                            if (!setCompanyDone) {
                                boolean response = flasher.promptForConfirmation(new Flash(
                                        "Company not found.\n" +
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
