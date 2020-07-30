package cosc1295.src.views;

import cosc1295.designs.Flasher;
import cosc1295.src.models.Company;
import cosc1295.src.models.Flash;
import helpers.commons.SharedEnums;
import helpers.commons.SharedEnums.FLASH_TYPES;
import helpers.utilities.Helpers;

import java.util.Scanner;

public class CompanyView {

    private final Flasher flasher = Flasher.getInstance();
    private final Scanner inputScanner;

    public CompanyView() {
        inputScanner = new Scanner(System.in);
    }

    public Company getCompanyBasicDetails() {
        Company newCompany = new Company();

        flasher.flash(new Flash(
                "\t\tTASK: ADD COMPANY\n\t\tPlease enter the details of New Company\n",
                FLASH_TYPES.NONE
        ));

        int companyFieldTracker = 0;
        while (companyFieldTracker < 3)
            switch (companyFieldTracker) {
                case 0:
                    flasher.flash(new Flash("Company Name: (allowed: ().-')", FLASH_TYPES.NONE));

                    newCompany.setCompanyName(inputScanner.nextLine());
                    Boolean nameValidation = newCompany.validateAndPrettifyCompanyName();

                    if (nameValidation == null) flasher.flash(new Flash("Company Name cannot be empty!\n", FLASH_TYPES.ERROR));
                    else if (!nameValidation) flasher.flash(new Flash("Company Name contains disallowed special character!\n", FLASH_TYPES.ERROR));

                    if (nameValidation == null || !nameValidation) continue;
                    companyFieldTracker++;
                    break;
                case 1:
                    flasher.flash(new Flash("ABN Number: ", FLASH_TYPES.NONE));

                    newCompany.setAbnNumber(inputScanner.nextLine());
                    Boolean abnValidation = newCompany.validateAndPrettifyAbnNumber();

                    if (abnValidation == null) flasher.flash(new Flash("ABN Number cannot be empty!\n", FLASH_TYPES.ERROR));
                    else if (!abnValidation) flasher.flash(new Flash("ABN Number can only have digits and alphabetical letters!\n", FLASH_TYPES.ERROR));

                    if (abnValidation == null || !abnValidation) continue;
                    companyFieldTracker++;
                    break;
                default:
                    flasher.flash(new Flash("Website URL: ", FLASH_TYPES.NONE));

                    newCompany.setWebsiteUrl(inputScanner.nextLine());
                    Boolean urlValidation = newCompany.validateWebsiteURL();

                    if (urlValidation == null) flasher.flash(new Flash("Website URL cannot be empty!\n", FLASH_TYPES.ERROR));
                    else if (!urlValidation) flasher.flash(new Flash("Website URL seems to be invalid!\n", FLASH_TYPES.ERROR));

                    if (urlValidation == null || !urlValidation) continue;
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
