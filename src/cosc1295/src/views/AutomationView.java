package cosc1295.src.views;

import cosc1295.designs.Flasher;
import cosc1295.src.models.Flash;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import javafx.util.Pair;

import java.util.Scanner;

public class AutomationView {

    private final Flasher flasher = Flasher.getInstance();
    private final Scanner inputScanner;

    public AutomationView() {
        inputScanner = new Scanner(System.in);
    }

    public boolean promptForFeatureToRun() {
        flasher.flash(new Flash(
            "\nWelcome to Automation Feature.\n" +
                    "In this feature, we will recommend a prospective Assign or Swap for you, basing on the feature you are about to select.\n" +
                    "If you confirm to execute the suggestion, we will run it. All data will be saved automatically.\n" +
                    "You can undo each and every change happened here by select menu option J.\n",
            SharedEnums.FLASH_TYPES.NONE
        ));

        return flasher.promptForConfirmation(new Flash(
            "Please select a feature you wish to use:\nY: Auto Assign \t\tN: Auto Swap",
            SharedEnums.FLASH_TYPES.NONE
        ));
    }

    public boolean promptForContinue() {
        return flasher.promptForConfirmation(new Flash(
            "\nDo you wish to continue?\nY: Continue\tN:Quit",
            SharedEnums.FLASH_TYPES.NONE
        ));
    }

    public boolean promptForAssignConfirmation(Pair<Student, Pair<Team, Student>> suggestion) {
        flasher.flash(new Flash(
            "\nRecommended Assignee: Student " + suggestion.getKey().getUniqueId() + " for Team #" + suggestion.getValue().getKey().getId() + (
                suggestion.getValue().getValue() == null ? SharedConstants.EMPTY_STRING : " Replacing member " + suggestion.getValue().getValue().getUniqueId()
            ),
            SharedEnums.FLASH_TYPES.NONE
        ));

        return flasher.promptForConfirmation(new Flash("Do you wish to proceed?\nY: yes\tN: Cancel", SharedEnums.FLASH_TYPES.NONE));
    }



    public boolean promptForSwapConfirmation(Pair<Pair<Team, Student>, Pair<Team, Student>> suggestion) {
        flasher.flash(new Flash(
            "\nRecommended Swap: Student " + suggestion.getKey().getValue().getUniqueId() + " in Team #" + suggestion.getKey().getKey().getId() +
                    " Swapping to Student " + suggestion.getValue().getValue().getUniqueId() + " in Team #" + suggestion.getValue().getKey().getId(),
            SharedEnums.FLASH_TYPES.NONE
        ));

        return flasher.promptForConfirmation(new Flash("Do you wish to proceed?\nY: yes\tN: Cancel", SharedEnums.FLASH_TYPES.NONE));
    }

    public void displayUrgentFailMessage() {
        flasher.flash(new Flash(
            "An error occurred while retrieving data. Please try again.\n" +
                    "Press enter to continue.",
            SharedEnums.FLASH_TYPES.ERROR
        ));

        inputScanner.nextLine();
    }

    public void displaySuccessMessage() {
        flasher.flash(new Flash(
            "Done! The change has been made and saved successfully.\n" +
                    "Press enter to continue.",
            SharedEnums.FLASH_TYPES.SUCCESS
        ));

        inputScanner.nextLine();
    }

    public void displayNoSuggestionMessage() {
        flasher.flash(new Flash(
            "\nOh! It looks like all Teams have been balanced. So no suggestion could be made. " +
                    "Please use other features instead.\nPress enter to continue.",
            SharedEnums.FLASH_TYPES.NONE
        ));

        inputScanner.nextLine();
    }
}
