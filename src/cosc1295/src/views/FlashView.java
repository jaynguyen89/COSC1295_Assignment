package cosc1295.src.views;

import cosc1295.src.models.Flash;
import helpers.commons.SharedEnums;
import helpers.utilities.Helpers;

import java.util.Scanner;

public class FlashView {

    public void printFlashMessage(Flash flash) {
        System.out.println(
                flash.getType() + flash.getMessage()
        );
    }

    public boolean promptForConfirmation(Flash flash) {
        String response;
        Scanner inputScanner = new Scanner(System.in);

        while (true) {
            printFlashMessage(flash);

            response = inputScanner.next().toUpperCase();
            inputScanner.nextLine();

            if (!Helpers.validateConfirmation(response)) {
                printFlashMessage(new Flash(
                    "Response not recognized. Press enter to continue.",
                    SharedEnums.FLASH_TYPES.ATTENTION
                ));

                inputScanner.nextLine();
                continue;
            }

            break;
        }

        return response.equals(SharedEnums.CONFIRMATIONS.Y.getValue()) ||
                response.equals(SharedEnums.CONFIRMATIONS.Y.name());
    }
}
