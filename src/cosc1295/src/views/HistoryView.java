package cosc1295.src.views;

import cosc1295.designs.Flasher;
import cosc1295.src.models.Flash;
import helpers.commons.SharedEnums;

import java.util.Scanner;

public class HistoryView {

    private final Flasher flasher = Flasher.getInstance();
    private final Scanner inputScanner;

    public HistoryView() {
        inputScanner = new Scanner(System.in);
    }

    public boolean promptForUndoConfirmation() {
        return flasher.promptForConfirmation(
            new Flash(
                "\nChanges will be lost and not recoverable after undoing. Are you sure?" +
                        "\nY: Yes, undo it.\tN: No, cancel.",
                SharedEnums.FLASH_TYPES.NONE
            ));
    }

    public void displayUndoFailMessage() {
        flasher.flash(new Flash(
            "\nAn error occurred while undoing the last change. Please try again." +
                    "\nPress enter to continue",
            SharedEnums.FLASH_TYPES.ERROR
        ));

        inputScanner.nextLine();
    }

    public void displayUndoSuccessMessage() {
        flasher.flash(new Flash(
            "\nAll good. The last change has been undone successfully." +
                    "\nPress enter to continue",
            SharedEnums.FLASH_TYPES.SUCCESS
        ));

        inputScanner.nextLine();
    }

    public void displayUndoEmptyMessage() {
        flasher.flash(new Flash(
            "\nNo changes to undo.\nPress enter to continue.\n",
            SharedEnums.FLASH_TYPES.NONE
        ));

        inputScanner.nextLine();
    }

    public boolean promptForContinueUndo() {
        return flasher.promptForConfirmation(
            new Flash(
                "\nDo you wish to undo more?\tY: Yes.\tN: No.",
                SharedEnums.FLASH_TYPES.NONE
            ));
    }
}
