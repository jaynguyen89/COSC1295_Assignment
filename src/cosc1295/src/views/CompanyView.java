package cosc1295.src.views;

import cosc1295.src.controllers.FlashController;
import cosc1295.src.models.Company;
import cosc1295.src.models.Flash;
import helpers.commons.SharedEnums;
import helpers.commons.SharedEnums.FLASH_TYPES;
import helpers.utilities.Helpers;

import java.util.Scanner;

public class CompanyView {

    private final FlashController flashController = FlashController.getInstance();
    private Scanner inputScanner;

    public CompanyView() {
        inputScanner = new Scanner(System.in);
    }

    public Company getCompanyBasicDetails() {
        Company newCompany = new Company();

        flashController.flash(new Flash(
                "\t\tTASK: ADD COMPANY\n\t\tPlease enter the details of New Company\n",
                FLASH_TYPES.NONE
        ));

        int companyFieldTracker = 0;
        while (companyFieldTracker < 3)
            switch (companyFieldTracker) {
                case 0:
                    flashController.flash(new Flash("Company Name: ", FLASH_TYPES.NONE));

                    newCompany.setCompanyName(inputScanner.nextLine());
                    if (!newCompany.validateAndPrettifyCompanyName()) {
                        flashController.flash(new Flash("Company Name cannot be empty!\n", FLASH_TYPES.ERROR));
                        continue;
                    }

                    companyFieldTracker++;
                    break;
                case 1:
                    flashController.flash(new Flash("ABN Number: ", FLASH_TYPES.NONE));

                    newCompany.setAbnNumber(inputScanner.nextLine());
                    if (!newCompany.validateAndPrettifyAbnNumber()) {
                        flashController.flash(new Flash("ABN Number cannot be empty!\n", FLASH_TYPES.ERROR));
                        continue;
                    }

                    companyFieldTracker++;
                    break;
                default:
                    flashController.flash(new Flash("Website URL: ", FLASH_TYPES.NONE));

                    newCompany.setWebsiteUrl(inputScanner.nextLine());
                    if (!newCompany.validateWebsiteURL()) {
                        flashController.flash(new Flash("Website URL cannot be empty!\n", FLASH_TYPES.ERROR));
                        continue;
                    }

                    companyFieldTracker++;
                    break;
            }

        return newCompany;
    }

    public boolean promptToRerunAddCompanyTaskAfterFailure() {
        String response;

        while (true) {
            flashController.flash(new Flash(
                    "\t\tTASK: ADD COMPANY\n\t\t" +
                            "An error occurred while adding the company.\n\t\t" +
                            "Do you wish to retry? (Y/N) ",
                    FLASH_TYPES.NONE
            ));

            response = inputScanner.next().toUpperCase();
            inputScanner.nextLine();

            if (!Helpers.validateConfirmation(response)) {
                flashController.flash(new Flash(
                        "Response not recognized. Press enter to continue.",
                        FLASH_TYPES.ATTENTION
                ));

                inputScanner.nextLine();
                continue;
            }

            break;
        }

        return response.equals(SharedEnums.CONFIRMATIONS.Y.value) ||
               response.equals(SharedEnums.CONFIRMATIONS.Y.name());
    }

    public void printAddCompanyResult(boolean result) {
        flashController.flash(
            result ? new Flash("The new company has been added successfully.\n", FLASH_TYPES.SUCCESS) :
                     new Flash("Unable to add new company due to an error.\n", FLASH_TYPES.NONE)
        );
    }
}
