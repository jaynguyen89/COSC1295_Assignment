package cosc1295.src.views;

import cosc1295.src.controllers.FlashController;
import cosc1295.src.models.Flash;
import cosc1295.src.models.ProjectOwner;
import helpers.commons.SharedEnums;

import java.util.Scanner;

public class ProjectOwnerView {

    private final FlashController flashController = FlashController.getInstance();
    private final Scanner inputScanner;

    public ProjectOwnerView() { inputScanner = new Scanner(System.in); }

    public ProjectOwner getProjectOwnerDetails() {
        ProjectOwner projectOwner = new ProjectOwner();

        flashController.flash(new Flash(
                "\t\tTASK: ADD PROJECT OWNER\n\t\tPlease enter the details of New Project Owner\n",
                SharedEnums.FLASH_TYPES.NONE
        ));

        int projectOwnerFieldTracker = 0;
        while (projectOwnerFieldTracker < 6) {
            switch (projectOwnerFieldTracker) {
                case 0:
                    flashController.flash(new Flash("First Name: ", SharedEnums.FLASH_TYPES.NONE));

                    projectOwner.setFirstName(inputScanner.nextLine());

                    projectOwnerFieldTracker++;
                    break;
                case 1:
                    flashController.flash(new Flash("Last Name: ", SharedEnums.FLASH_TYPES.NONE));

                    projectOwnerFieldTracker++;
                    break;
                case 2:
                    flashController.flash(new Flash("Email Address: ", SharedEnums.FLASH_TYPES.NONE));

                    projectOwnerFieldTracker++;
                    break;
                case 3:
                    flashController.flash(new Flash("Role: ", SharedEnums.FLASH_TYPES.NONE));

                    projectOwnerFieldTracker++;
                    break;
                case 4:
                    flashController.flash(new Flash("Unique ID: ", SharedEnums.FLASH_TYPES.NONE));

                    projectOwnerFieldTracker++;
                    break;
                default:
                    flashController.flash(new Flash("Company: ", SharedEnums.FLASH_TYPES.NONE));

                    projectOwnerFieldTracker++;
                    break;
            }
        }

        return null;
    }
}
