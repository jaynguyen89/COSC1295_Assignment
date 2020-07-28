package helpers.utilities;

import helpers.commons.SharedEnums;

import java.util.Arrays;

public final class InputValidator {

    public static boolean validateMenuSelection(String selection) {
        String[] allMenuItems = SharedEnums.getAllEnumItemsAsArray(
                SharedEnums.APPLICATION_MENU.class
        );

        return Arrays.asList(allMenuItems).contains(selection);
    }
}
