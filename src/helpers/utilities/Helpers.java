package helpers.utilities;

import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Helpers {

    public static boolean validateMenuSelection(String selection) {
        List<String> allMenuItems = SharedEnums.getAllEnumItemsAsList(
                SharedEnums.APPLICATION_MENU.class
        );

        return allMenuItems.contains(selection);
    }

    public static boolean validateConfirmation(String confirmation) {
        List<String> confirmationItems = SharedEnums.getAllEnumItemsAsList(
                SharedEnums.CONFIRMATIONS.class
        );

        List<String> confirmationValues = Stream.of(SharedEnums.CONFIRMATIONS.values())
                .map(SharedEnums.CONFIRMATIONS::getValue)
                .collect(Collectors.toList());

        return confirmationItems.contains(confirmation) ||
               confirmationValues.contains(confirmation);
    }

    public static boolean isNullOrBlankOrEmpty(String any) {
        return any == null || any.isEmpty() || any.trim().isEmpty();
    }

    public static String prettifyStringLiterals(String any, boolean strictMode) {
        any = any.trim()
                .replaceAll(SharedConstants.MULTIPLE_SPACE, SharedConstants.SPACE);

        if (strictMode) {
            String[] tokens = any.split(SharedConstants.SPACE);

            StringBuilder anyBuilder = new StringBuilder(SharedConstants.EMPTY_STRING);
            for (String token : tokens)
                anyBuilder.append(Character.toUpperCase(token.charAt(0)))
                        .append(token.substring(1))
                        .append(SharedConstants.SPACE);

            any = anyBuilder.toString();
        }

        return any;
    }
}
