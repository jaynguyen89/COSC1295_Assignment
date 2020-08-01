package cosc1295.src.views;

import cosc1295.designs.Flasher;
import cosc1295.src.models.Company;
import cosc1295.src.models.Flash;
import helpers.commons.SharedEnums.FLASH_TYPES;

import java.util.Scanner;

public class CompanyView {

    private final Flasher flasher = Flasher.getInstance();
    private final Scanner inputScanner;

    public CompanyView() {
        inputScanner = new Scanner(System.in);
    }

    /**
     * Gets user inputs to create a new Company.
     * @return Company
     */
    public Company getCompanyBasicDetails() {
        Company newCompany = new Company();

        flasher.flash(new Flash(
                "\t\tTASK: ADD COMPANY\n\t\tPlease enter the details of New Company\n",
                FLASH_TYPES.NONE
        ));

        int companyFieldTracker = 0;
        while (companyFieldTracker < 4)
            switch (companyFieldTracker) {
                case 0: //Getting uniqueId
                    flasher.flash(new Flash("Company ID: ", FLASH_TYPES.NONE));

                    newCompany.setUniqueId(inputScanner.nextLine());
                    Boolean idValidation = newCompany.validateAndPrettifyUniqueId(); //Validation
                    if (idValidation == null) flasher.flash(new Flash("Company ID cannot be empty!", FLASH_TYPES.ATTENTION));
                    else if (!idValidation) flasher.flash(new Flash("Company ID should not have special characters.", FLASH_TYPES.ATTENTION));

                    if (idValidation == null || !idValidation) continue; //Rerun while statement, continue case 0

                    //Check if uniqueId is safe to create a new Company
                    Boolean idAvailable = newCompany.isUniqueIdAvailable();
                    if (idAvailable == null) { //An exception is thrown while checking saved data
                        flasher.flash(new Flash("An error occurred while checking saved data.", FLASH_TYPES.ERROR));
                        boolean response = flasher.promptForConfirmation(new Flash(
                                "Do you wish to try again or go back to main menu?\n" +
                                        "Y: Try again\tN: Back to main menu",
                                FLASH_TYPES.ATTENTION
                        ));

                        if (!response) return null; //Return to Main Menu
                        continue; //Rerun while statement, continue case 0
                    }

                    if (!idAvailable) { //uniqueId is unsafe
                        flasher.flash(new Flash(
                                "Company ID " + newCompany.getUniqueId() + " is duplicated. Please set another ID.\n" +
                                        "Press enter to continue.",
                                FLASH_TYPES.ERROR
                        ));

                        inputScanner.nextLine();
                        continue; //Rerun while statement, continue case 0
                    }

                    //Up to here, uniqueId is safe and set successfully
                    companyFieldTracker++;
                    break;
                case 1: //Getting companyName
                    flasher.flash(new Flash("Company Name: (allowed: ().-')", FLASH_TYPES.NONE));

                    newCompany.setCompanyName(inputScanner.nextLine());
                    Boolean nameValidation = newCompany.validateAndPrettifyCompanyName(); //Validation

                    if (nameValidation == null) flasher.flash(new Flash("Company Name cannot be empty!\n", FLASH_TYPES.ERROR));
                    else if (!nameValidation) flasher.flash(new Flash("Company Name contains disallowed special character!\n", FLASH_TYPES.ERROR));

                    if (nameValidation == null || !nameValidation) continue; //Rerun while statement, continue case 1
                    companyFieldTracker++;
                    break;
                case 2: //Getting abnNumber
                    flasher.flash(new Flash("ABN Number: ", FLASH_TYPES.NONE));

                    newCompany.setAbnNumber(inputScanner.nextLine());
                    Boolean abnValidation = newCompany.validateAndPrettifyAbnNumber(); //Validation

                    if (abnValidation == null) flasher.flash(new Flash("ABN Number cannot be empty!\n", FLASH_TYPES.ERROR));
                    else if (!abnValidation) flasher.flash(new Flash("ABN Number can only have digits and alphabetical letters!\n", FLASH_TYPES.ERROR));

                    if (abnValidation == null || !abnValidation) continue; //Rerun while statement, continue case 2
                    companyFieldTracker++;
                    break;
                default: //Getting websiteUrl
                    flasher.flash(new Flash("Website URL: ", FLASH_TYPES.NONE));

                    newCompany.setWebsiteUrl(inputScanner.nextLine());
                    Boolean urlValidation = newCompany.validateWebsiteURL(); //Validation

                    if (urlValidation == null) flasher.flash(new Flash("Website URL cannot be empty!\n", FLASH_TYPES.ERROR));
                    else if (!urlValidation) flasher.flash(new Flash("Website URL seems to be invalid!\n", FLASH_TYPES.ERROR));

                    if (urlValidation == null || !urlValidation) continue; //Rerun while statement, continue this case
                    companyFieldTracker++;
                    break;
            }

        return newCompany;
    }

    public boolean promptToRerunAddCompanyTaskAfterFailure() {
        return flasher.promptForConfirmation(new Flash(
            "\t\tTASK: ADD COMPANY\n\t\t" +
                    "An error occurred while adding the company.\n\t\t" +
                    "Do you wish to retry? (Y/N) ",
            FLASH_TYPES.NONE
        ));
    }

    public void printAddCompanyResult(boolean result) {
        flasher.flash(
            result ? new Flash("The new company has been added successfully.\n", FLASH_TYPES.SUCCESS) :
                     new Flash("Unable to add new company due to an error.\n", FLASH_TYPES.NONE)
        );
    }
}
