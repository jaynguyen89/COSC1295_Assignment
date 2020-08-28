package entry;

import com.sun.istack.internal.NotNull;
import cosc1295.designs.ApplicationFacade;
import cosc1295.designs.Flasher;
import cosc1295.src.models.Flash;
import cosc1295.src.services.TeamFitnessMetricService;
import helpers.commons.SharedEnums.FLASH_TYPES;
import helpers.commons.SharedEnums.APPLICATION_MENU;
import static helpers.commons.SharedEnums.APPLICATION_MENU.*;
import helpers.utilities.Helpers;

import java.util.Scanner;

/**
 * Main Menu navigation when the app starts.
 */
public final class ProjectTeamFormationApp {

    private final Flasher flasher = Flasher.getInstance();

    public void run() {
        // TeamMetricsService is a background job that execute once every 10 seconds
        // to calculate fitness metrics for all Teams, view the class for more information
        Thread TeamMetricsService = new TeamFitnessMetricService();
        TeamMetricsService.start();

        String menuSelection; //Get user input for Main Menu

        while (true) {
            printProgramMenuInternally(); // Print the Main Menu
            flasher.flash(new Flash(
                    "\nSelect an option: ",
                    FLASH_TYPES.NONE
            ));

			Scanner inputScanner = new Scanner(System.in);
            //The first input token matters, don't care the rest, so use next() instead of nextLine()
            menuSelection = inputScanner.next().toUpperCase();
            inputScanner.nextLine(); //remove left-over from buffer, place cursor on new line for next input reading

            if (!Helpers.validateMenuSelection(menuSelection)) { //Wrong input for menu selection
                //Let user read status message before they want to advance
                flasher.flash(new Flash(
                        "\nYour selection is out of scope. Press enter to view menu again.",
                        FLASH_TYPES.ATTENTION
                ));

                inputScanner.nextLine(); //Advance the program
                continue;
            }

            APPLICATION_MENU selectedOption = valueOf(menuSelection); //Correct input for a menu item
            boolean shouldQuit = runApplicationWithSelection(selectedOption); //Run the feature according to menu selection

            if (shouldQuit) {
                flasher.flash(new Flash(
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
            System.out.printf("\t\t%s. %s%n", menuItem, menuItem.getValue());
    }

    /**
     * Runs a feature selected by user input.
     * Returns true when user selects option to quit the app.
     * @param option APPLICATION_MENU
     * @return boolean
     */
    private boolean runApplicationWithSelection(@NotNull APPLICATION_MENU option) {
        boolean taskDone = false;
        ApplicationFacade appFacade = new ApplicationFacade();

        switch (option) {
            case A:
                appFacade.runAddCompanyFeature();
                break;
            case B:
                appFacade.runAddProjectOwnerFeature();
                break;
            case C:
                appFacade.runAddProjectFeature();
                break;
            case D:
                appFacade.runStudentPersonalityCapturingFeature();
                break;
            case E:
                appFacade.runStudentPreferenceCapturingFeature();
                break;
            case F:
                appFacade.runProjectShortlistingFeature();
                break;
            case G:
                appFacade.runTeamFormationFeature();
                break;
            case H:
                appFacade.displayTeamFitnessMetrics();
                break;
            case I:
                appFacade.runTeamProjectSetOrChange();
            case J:
                appFacade.runTeamManagementGui();
            default:
                taskDone = true;
                break;
        }

        return taskDone;
    }
}