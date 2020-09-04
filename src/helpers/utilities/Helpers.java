package helpers.utilities;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import cosc1295.providers.bases.ServiceLocator;
import cosc1295.providers.services.CompanyService;
import cosc1295.providers.services.ProjectOwnerService;
import cosc1295.providers.services.ProjectService;
import cosc1295.src.models.*;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;

import javafx.util.Pair;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This Helper class provides utility methods to be used anywhere in the project, including:
 * menu validation, confirmation dialog validation, string and number pre-processing and prettifying,
 * first-step data checking and parsing, collection searching/sorting...
 */
public final class Helpers {

    /**
     * Validates user inputs for the Main Menu (when application starts).
     * Checks the input against an Enum class's attribute names.
     * Returns true if input is found in Enum, otherwise false.
     * @param selection String
     * @return boolean
     */
    public static boolean validateMenuSelection(@NotNull String selection) {
        List<String> allMenuItems = SharedEnums.getAllEnumAttributesAsList(
            SharedEnums.APPLICATION_MENU.class
        );

        return allMenuItems.contains(selection);
    }

    /**
     * Validates user inputs for the Confirmation Dialog for `Y=YES` or `N=NO` selection.
     * Checks the input against an Enum class's attribute names and values.
     * Returns true if input selection is found in Enum, otherwise false.
     * @param confirmation String
     * @return boolean
     */
    public static boolean validateConfirmation(String confirmation) {
        List<String> confirmationItems = SharedEnums.getAllEnumAttributesAsList(
            SharedEnums.CONFIRMATIONS.class
        ); //Get {"Y", "N"}

        List<String> confirmationValues = Stream.of(SharedEnums.CONFIRMATIONS.values())
                .map(SharedEnums.CONFIRMATIONS::getValue)
                .collect(Collectors.toList()); //Get {"Yes", "No"}

        return confirmationItems.contains(confirmation) ||
               confirmationValues.contains(confirmation);
    }

    /**
     * Returns true if a string is NULL or empty by its own, or empty after trimming spaces.
     * @param any String
     * @return boolean
     */
    public static boolean isNullOrBlankOrEmpty(@Nullable String any) {
        return any == null || any.isEmpty() || any.trim().isEmpty();
    }

    /**
     * Pre-processes a string basing on strictMode. Turns an ugly string to a pretty one.
     * strictMode == false -> only trim and remove spaces.
     * strictMode == true -> capitalize first letter of every word in the string.
     * @param any String
     * @param strictMode boolean
     * @return String
     */
    public static String prettifyStringLiterals(@NotNull String any, boolean strictMode) {
        any = any.trim()
                .replaceAll(SharedConstants.MULTIPLE_SPACE, SharedConstants.SPACE);

        //Capitalize first character of each word in string
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

    /**
     * Validates human and object names basing on isHuman param, using a regex pattern.
     * Returns NULL if the name is NULL; true is the name is valid; otherwise false.
     * A valid name does not contain special characters other than the allowed ones.
     * @param name String
     * @param isHuman boolean
     * @return Boolean
     */
    public static Boolean validateLiteralName(@Nullable String name, boolean isHuman) {
        if (isNullOrBlankOrEmpty(name)) return null;

        Pattern nameRegex = Pattern.compile(isHuman ? "^[a-zA-Z.\\-' ]+$" : "^[\\w.\\-'() ]+$");
        Matcher matcher = nameRegex.matcher(name);

        return matcher.matches();
    }

    /**
     * Checks if a string is a valid Integer number.
     * @param any String
     * @return boolean
     */
    public static boolean isIntegerNumber(@Nullable String any) {
        try {
            Integer.parseInt(any);
        } catch (NumberFormatException | NullPointerException ex) {
            return false;
        }

        return true;
    }

    /**
     * Validates unique ID string, and prettifies it by removing all spaces then capitalizing.
     * Returns NULL if the ID is NULL or empty or blank, otherwise returns Pair with the Key
     * being the prettified ID, and the Value indicating if the ID is valid or not.
     * @param uniqueId String
     * @return Pair<String, Boolean>
     */
    public static Pair<String, Boolean> validateAndPrettifyUniqueId(String uniqueId) {
        if (Helpers.isNullOrBlankOrEmpty(uniqueId))
            return null;

        //Prettify
        uniqueId = uniqueId.trim()
                .replaceAll(
                    SharedConstants.MULTIPLE_SPACE,
                    SharedConstants.EMPTY_STRING
                )
                .toUpperCase();

        //Validate: Unique ID is valid if it contains no special characters
        Pattern idRegex = Pattern.compile("^[\\w]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = idRegex.matcher(uniqueId);

        return new Pair<>(uniqueId, matcher.matches());
    }

    /**
     * Checks if a uniqueId is safe to use for saving new entry into file. For this purpose,
     * it checks if a uniqueId is found in data saved in a text file determined by the type T of object.
     * Eg. if type T is Student then check if a uniqueId is found in students.txt file.
     * Returns true if the uniqueId is safe to use, otherwise false.
     * @param type Class<T>
     * @param uniqId String
     * @param <T> Type
     * @return <T>
     */
    public static <T> Boolean checkUniqueIdAvailableFor(Class<T> type, String uniqId) {
        //Singleton object that provides access to the Dependency Injection Services
        ServiceLocator locator = ServiceLocator.getInstance();

        CompanyService companyService;
        ProjectOwnerService projectOwnerService;
        ProjectService projectService;

        try {
            //Get a Service instance then call service to check uniqueId
            //Check src\cosc1295\providers for more information

            if (Company.class.equals(type)) {
                companyService = locator.getService(CompanyService.class);
                return !companyService.isUniqueIdDuplicated(uniqId);
            } else if (ProjectOwner.class.equals(type)) {
                projectOwnerService = locator.getService(ProjectOwnerService.class);
                return !projectOwnerService.isUniqueIdDuplicated(uniqId);
            } else if (Project.class.equals(type)) {
                projectService = locator.getService(ProjectService.class);
                return !projectService.isUniqueIdDuplicated(uniqId);
            }
        } catch (
            IllegalAccessException |
            InstantiationException |
            NullPointerException ex
        ) {
            return null;
        }

        return null;
    }

    /**
     * Sort a HashMap by values, the result is an ArrayList of Map.Entry in descending order.
     * Uses to shortlist projects: Key holds the Project unique ID, Value holds Preference
     * @param any HashMap<String, Integer>
     * @return List<Map.Entry<String, Integer>>
     */
    public static List<Map.Entry<String, Integer>> sortDescending(HashMap<String, Integer> any) {
        List<Map.Entry<String, Integer>> listToSort = new ArrayList<>(any.entrySet());

        //Perform bubble sorting
        for (int i = 0; i < listToSort.size(); i++)
            for (int j = 1; j < listToSort.size() - i; j++)
                if (listToSort.get(j - 1).getValue() < listToSort.get(j).getValue()) {
                    Map.Entry<String, Integer> temp = listToSort.get(j - 1);
                    listToSort.set(j - 1, listToSort.get(j));
                    listToSort.set(j, temp);
                }
        
        return listToSort;
    }

    /**
     * Round a floating-point number, taking the decimal numbers by precision.
     * Eg. 1.23456789 is rounded in 1.23 if precision == 2, or 1.234 if precision == 3
     * @param any double
     * @param precision int
     * @return double
     */
    public static double round(double any, int precision) {
        precision = precision == 0 ? 1 : Math.min(precision, 3);

        DecimalFormat format = new DecimalFormat(
                precision == 2 ? "#.##" : "#.###"
        );
        return Double.parseDouble(format.format(any));
    }

    public static String getIdFromCompact(String compact) {
        return compact.split(SharedConstants.SPACE)[1]
            .replace("#", SharedConstants.EMPTY_STRING)
            .replace(":", SharedConstants.EMPTY_STRING);
    }
}
