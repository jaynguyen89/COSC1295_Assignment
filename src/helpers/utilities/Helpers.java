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
        List<String> allMenuItems = SharedEnums.getAllEnumItemsAsList(
                SharedEnums.APPLICATION_MENU.class
        );

        return allMenuItems.contains(selection);
    }

    /**
     * Validates user inputs for the Confirmation Dialog for `Y=YES` or `N=NO` selection.
     * Checks the input against an Enum class's attribute names and values.
     * Returns true if selection is found in Enum, otherwise false.
     * @param confirmation String
     * @return boolean
     */
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

    /**
     * Returns true if a string is NULL or empty by its own or empty after trimming spaces.
     * @param any String
     * @return boolean
     */
    public static boolean isNullOrBlankOrEmpty(@Nullable String any) {
        return any == null || any.isEmpty() || any.trim().isEmpty();
    }

    /**
     * Pre-processes a string basing on strictMode. Turns an ugly string to a pretty one.
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
     * Validates unique ID string, and prettifies it by removing all spaces then capitalize.
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

        //Validate
        Pattern idRegex = Pattern.compile("^[\\w]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = idRegex.matcher(uniqueId);

        return new Pair<>(uniqueId, matcher.matches());
    }

    /**
     * Checks if a uniqueId is safe to use for saving new data into file. For this purpose,
     * it checks if a uniqueId is found in data saved in a text file determined by the type T of object.
     * Eg. if type T is Student then check if a uniqueId is found in students.txt file.
     * Returns true if the uniqueId is safe to use, otherwise false.
     * @param type Class<T>
     * @param uniqId String
     * @param <T> Type
     * @return <T>
     */
    public static <T> Boolean checkUniqueIdAvailableFor(Class<T> type, String uniqId) {
        //Singleton object that provides access to Services
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
     * To be used by any data processing Service if required. This method parses a
     * Skill-Ranking token saved in file to produce corresponding Enum values.
     * Eg. token W1 parses into SKILLS.W and RANKING.LOW.
     * Returns a Skill-Ranking Pair.
     * @param token String
     * @return Pair<SharedEnums.SKILLS, SharedEnums.RANKINGS>
     */
    public static
        Pair<SharedEnums.SKILLS,
             SharedEnums.RANKINGS>
    parseSkillRankingToken(@NotNull String token) {
        String skill = token.substring(0, 1);
        int ranking = Integer.parseInt(
                token.substring(token.length() - 1)
        ) - 1;

        SharedEnums.SKILLS eSkill =
                skill.equals(SharedEnums.SKILLS.A.name()) ? SharedEnums.SKILLS.A :
                (skill.equals(SharedEnums.SKILLS.N.name()) ? SharedEnums.SKILLS.N :
                        (skill.equals(SharedEnums.SKILLS.P.name()) ? SharedEnums.SKILLS.P
                                : SharedEnums.SKILLS.W)
                );

        SharedEnums.RANKINGS eRanking = SharedEnums.RANKINGS.values()[ranking];

        return new Pair<>(eSkill, eRanking);
    }

    /**
     * Sort a HashMap by values, the result is an ArrayList of Map.Entry in descending order.
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

    public static double round(double any) {
        DecimalFormat format = new DecimalFormat("#.##");
        return Double.parseDouble(format.format(any));
    }

    //null for any student, Key=true for leader type required, Value contains refused Student Unique Id
    public static Pair<Boolean, List<String>> produceTeamRequirementsOnNewMember(List<Student> members, @NotNull List<Student> selectedStudents) {
        boolean leaderTypeRequired = true;
        List<String> refusedStudents = new ArrayList<>();

        List<Student> teamMembers = new ArrayList<>();
        teamMembers.addAll(members);
        teamMembers.addAll(selectedStudents);

        for (Student member : teamMembers) {
            if (leaderTypeRequired && member.getPersonality().name().equals(SharedEnums.PERSONALITIES.A.name()))
                leaderTypeRequired = false;

            for (String studentUniqueId : member.getConflicters())
                if (!refusedStudents.contains(studentUniqueId))
                    refusedStudents.add(studentUniqueId);
        }

        if (!leaderTypeRequired && refusedStudents.size() == 0) return null;
        return new Pair<>(
            leaderTypeRequired,
            refusedStudents.size() == 0 ? null : refusedStudents
        );
    }

    public static Pair<Pair<Boolean, String>, Pair<Boolean, String>> isTeamRequirementsMutuallySatisfied(Pair<Team, Student> first, Pair<Team, Student> second) {
        List<Student> firstTeamMembers = new ArrayList<>(first.getKey().getMembers());
        firstTeamMembers.remove(first.getValue());

        List<Student> secondTeamMembers = new ArrayList<>(second.getKey().getMembers());
        if (second.getValue() != null) secondTeamMembers.remove(second.getValue());

        Pair<Boolean, List<String>> firstTeamRequirements = produceTeamRequirementsOnNewMember(firstTeamMembers, new ArrayList<>());
        Pair<Boolean, List<String>> secondTeamRequirements = produceTeamRequirementsOnNewMember(secondTeamMembers, new ArrayList<>());

        //boolean isSatisfied = false;

        assert firstTeamRequirements != null;
        Boolean firstTeamLeaderRequired = firstTeamRequirements.getKey();
        List<String> firstTeamRefusals = firstTeamRequirements.getValue();

        assert secondTeamRequirements != null;
        Boolean secondTeamLeaderRequired = secondTeamRequirements.getKey();
        List<String> secondTeamRefusals = secondTeamRequirements.getValue();

        //Team 2 does not send Student to Team 1, so no need to check Team 1,
        //Only check if Student sent from Team 1 meets Team 2 requirements
        if (second.getValue() == null) {
            if (secondTeamLeaderRequired && secondTeamMembers.size() < 3)
                secondTeamLeaderRequired = false;

            if (secondTeamRefusals != null && secondTeamRefusals.contains(first.getValue().getUniqueId()))
                return new Pair<>(null, new Pair<>(secondTeamLeaderRequired, first.getValue().getUniqueId()));

            if (secondTeamLeaderRequired)
                return new Pair<>(null, new Pair<>(true, null));
        }
        //Both Teams send Students to each other (swap), check both Team's requirements
        else {
            Pair<Boolean, String> firstTeamUnsatisfiedRequirements = checkForUnsatisfiedRequirements(
                    second, firstTeamMembers, firstTeamLeaderRequired, firstTeamRefusals
            );
            Pair<Boolean, String> secondTeamUnsatisfiedRequirements = checkForUnsatisfiedRequirements(
                    first, secondTeamMembers, secondTeamLeaderRequired, secondTeamRefusals
            );

            if (firstTeamUnsatisfiedRequirements != null || secondTeamUnsatisfiedRequirements != null)
                return new Pair<>(firstTeamUnsatisfiedRequirements, secondTeamUnsatisfiedRequirements);
        }

        return null;
    }

    private static Pair<Boolean, String> checkForUnsatisfiedRequirements(
            Pair<Team, Student> teamSelections,
            List<Student> members,
            Boolean isTeamLeaderRequired,
            List<String> refusals
    ) {
        Pair<Boolean, String> unsatisfiedRequirements = null;

        if (isTeamLeaderRequired && members.size() < 3)
            isTeamLeaderRequired = false;

        if (refusals != null && refusals.contains(teamSelections.getValue().getUniqueId()))
            unsatisfiedRequirements = new Pair<>(isTeamLeaderRequired, teamSelections.getValue().getUniqueId());

        if (isTeamLeaderRequired)
            unsatisfiedRequirements = new Pair<>(true, null);

        return unsatisfiedRequirements;
    }
}
