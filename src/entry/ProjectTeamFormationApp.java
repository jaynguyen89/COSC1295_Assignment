package entry;

import cosc1295.src.controllers.FlashController;
import cosc1295.src.models.Flash;
import helpers.commons.SharedEnums.FLASH_TYPES;
import helpers.commons.SharedEnums.APPLICATION_MENU;
import static helpers.commons.SharedEnums.APPLICATION_MENU.*;
import helpers.utilities.Helpers;

import java.util.Scanner;

public final class ProjectTeamFormationApp {

    private final FlashController flashController = FlashController.getInstance();

    public void run() {
        String menuSelection;

        while (true) {
            printProgramMenuInternally();
            flashController.flash(new Flash(
                    "\nSelect an option: ",
                    FLASH_TYPES.NONE
            ));

            Scanner inputScanner = new Scanner(System.in);
            menuSelection = inputScanner.next().toUpperCase();
            inputScanner.nextLine();

            if (!Helpers.validateMenuSelection(menuSelection)) {
                flashController.flash(new Flash(
                        "\nYour selection is out of scope. Press enter to view menu again.",
                        FLASH_TYPES.ATTENTION
                ));

                inputScanner.nextLine();
                continue;
            }

            APPLICATION_MENU selectedOption = valueOf(menuSelection);
            boolean shouldQuit = runApplicationWithSelection(selectedOption);

            if (shouldQuit) {
                flashController.flash(new Flash(
                        "\nApplication closed.",
                        FLASH_TYPES.NONE
                ));

                inputScanner.close();
                break;
            }
        }
    }

    private void printProgramMenuInternally() {
        System.out.println("\t\tProject Team Formation - Menu");
        System.out.println("\t\t\t----------\n");

        for (APPLICATION_MENU menuItem : values())
            System.out.println(String.format("\t\t%s. %s", menuItem, menuItem.value));
    }

    private boolean runApplicationWithSelection(APPLICATION_MENU option) {
        boolean taskDone = false;
        ApplicationFacade appFacade = new ApplicationFacade();

        switch (option) {
            case A:
                appFacade.runAddCompanyFeature();
                break;
            case B:
                System.out.println("Run task: " + B.value);
                break;
            case C:
                System.out.println("Run task: " + C.value);
                break;
            case D:
                System.out.println("Run task: " + D.value);
                break;
            case E:
                System.out.println("Run task: " + E.value);
                break;
            case F:
                System.out.println("Run task: " + F.value);
                break;
            default:
                taskDone = true;
                break;
        }

        return taskDone;
    }
}