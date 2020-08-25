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
     * To be used by any data processing Service if required. This method parses a
     * Skill-Ranking token saved in file to produce corresponding Enum values.
     * Eg. token "W1" parses into SKILLS.W and RANKING.LOW
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

    /**
     * Produce the requirements on new member for a Team when user assign a Student into Team.
     * Requirements include Leader type and Personality.
     * The selectedStudents is list of Students that are pending to be added (these students are validated against the Team requirements priorly).
     * Returns a Pair, with Key indicating if Leader type is required, and Value indicating the Students that are refused to be added.
     * @param members List<Student>
     * @param selectedStudents List<Student>
     * @return Pair<Boolean, List<String>>
     */
    //null for any student, Key=true for leader type required, Value contains refused Student Unique Id
    public static Pair<Boolean, List<String>> produceTeamRequirementsOnNewMember(List<Student> members, @Nullable List<Student> selectedStudents) {
        boolean leaderTypeRequired = true; //Key of the Pair to be returned
        List<String> refusedStudents = new ArrayList<>(); //Value of the Pair to be returned

        //Temporarily add the pending Students to Team to check the resulting Team
        List<Student> teamMembers = new ArrayList<>();
        teamMembers.addAll(members);
        teamMembers.addAll(selectedStudents);

        //Check the resulting Team
        for (Student member : teamMembers) {
            //leaderTypeRequired is combined in this condition to omit this checking if Leader type is already assigned in Team
            if (leaderTypeRequired && member.getPersonality().name().equals(SharedEnums.PERSONALITIES.A.name()))
                leaderTypeRequired = false;

            //Get the Students that cannot be added to Team due to conflicts
            for (String studentUniqueId : member.getConflicters())
                if (!refusedStudents.contains(studentUniqueId))
                    refusedStudents.add(studentUniqueId);
        }

        //No requirements, any Students can be added, returns null, otherwise, returns the requirements
        if (!leaderTypeRequired && refusedStudents.size() == 0) return null;
        return new Pair<>(
            leaderTypeRequired,
            refusedStudents.size() == 0 ? null : refusedStudents
        );
    }

    /**
     * Checks the requirements of both Teams in swapping Students. Each Team passes into this method its
     * Team details and the Student it offers for swap. The user will get a result as follow:
     * Pair<Key, Value> with Key is the requirements of First Team, Value is the requirements of Second Team.
     * Each Key, Value is Pair<Boolean, String>, with Boolean indicating if the Team needs Leader type, and
     * Value indicating the Students it refuses.
     * @param first Pair<Team, Student>
     * @param second Pair<Team, Student>
     * @return Pair<Pair<Boolean, String>, Pair<Boolean, String>>
     */
    public static Pair<Pair<Boolean, String>, Pair<Boolean, String>>
    isTeamRequirementsMutuallySatisfied(Pair<Team, Student> first, Pair<Team, Student> second) {
        List<Student> firstTeamMembers = new ArrayList<>(first.getKey().getMembers());
        firstTeamMembers.remove(first.getValue()); //Fake removing the Student offered for swap in first Team

        List<Student> secondTeamMembers = new ArrayList<>(second.getKey().getMembers());
        if (second.getValue() != null) secondTeamMembers.remove(second.getValue()); //Fake removing the Student offered for swap in second Team

        //Call the above method to produce requirements of each Team
        Pair<Boolean, List<String>> firstTeamRequirements = produceTeamRequirementsOnNewMember(firstTeamMembers, new ArrayList<>());
        Pair<Boolean, List<String>> secondTeamRequirements = produceTeamRequirementsOnNewMember(secondTeamMembers, new ArrayList<>());

        //Pick up requirements of first Team
        Boolean firstTeamLeaderRequired = firstTeamRequirements != null && firstTeamRequirements.getKey();
        List<String> firstTeamRefusals = firstTeamRequirements == null ? null : firstTeamRequirements.getValue();

        //Pick up requirements of second Team
        boolean secondTeamLeaderRequired = secondTeamRequirements!= null && secondTeamRequirements.getKey();
        List<String> secondTeamRefusals = secondTeamRequirements == null ? null : secondTeamRequirements.getValue();

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

    /**
     * When swapping Students between Teams, if both Teams refuse each other, pick up what is being refused.
     * Eg. Pick up what Team 1 needs on Team 2 and vice versa.
     * Return a Pair with Key indicating if one Team requires Leader from the other,
     * and Value indicating the Students that one Team refuses.
     * @param teamSelections Pair<Team, Student>
     * @param members List<Student>
     * @param isTeamLeaderRequired Boolean
     * @param refusals List<String>
     * @return Pair<Boolean, String>
     */
    private static Pair<Boolean, String> checkForUnsatisfiedRequirements(
        Pair<Team, Student> teamSelections,
        List<Student> members,
        Boolean isTeamLeaderRequired,
        List<String> refusals
    ) {
        Pair<Boolean, String> unsatisfiedRequirements = null;

        //Team only has 0-2 Student, not yet enforce Leader type as there are slot available later
        if (isTeamLeaderRequired && members.size() < 3)
            isTeamLeaderRequired = false;

        //Team has 3 Students and Leader already assigned, no need to enforce Leader type
        if (members.size() == 3 && teamSelections.getValue().getPersonality() == SharedEnums.PERSONALITIES.A)
            isTeamLeaderRequired = false;

        //Team refuses some Students, check if the offered Student is among the refused ones
        //If yes, return isTeamLeaderRequired along with refusal to simplify the next return
        if (refusals != null && refusals.contains(teamSelections.getValue().getUniqueId()))
            unsatisfiedRequirements = new Pair<>(isTeamLeaderRequired, teamSelections.getValue().getUniqueId());

        //Team does not refuse the offered Student
        if (isTeamLeaderRequired)
            unsatisfiedRequirements = new Pair<>(true, null);

        //Team is cool with the offered student, return null here
        return unsatisfiedRequirements;
    }

    /**
     * The selection param holds Team being the Team to get the assigned Student.
     * Checks for Personality Imbalance if the Team actually gets the offered Student.
     * Returns a Pair with Key indicating if Team's Personality is imbalance,
     * and Value indicating the Personalities that the Team needs, so user know what
     * Student they should select.
     * @param selection Pair<Team, Student>
     * @return Pair<Boolean, List<SharedEnums.PERSONALITIES>>
     */
    //Key indicates if Personality is imbalance, when Key==true,
    // Value indicates the Personalities it requires for the next added member (not the current one)
    public static Pair<Boolean, List<SharedEnums.PERSONALITIES>> checkImbalancePersonalityOnAssign(Pair<Team, Student> selection) {
        List<SharedEnums.PERSONALITIES> allPersonalities = new ArrayList<SharedEnums.PERSONALITIES>() {{
            add(SharedEnums.PERSONALITIES.A);
            add(SharedEnums.PERSONALITIES.B);
            add(SharedEnums.PERSONALITIES.C);
            add(SharedEnums.PERSONALITIES.D);
        }};
        List<Student> teamMembers = new ArrayList<>(selection.getKey().getMembers());

        //Team has no member, no PERSONALITIES enforcement is required yet.
        if (teamMembers.size() < 1) return null;
        teamMembers.add(selection.getValue()); //Add the student to Team, so we have a prospective Team to inspect

        List<SharedEnums.PERSONALITIES> teamPersonalities = new ArrayList<>();
        for (Student member : teamMembers)
            if (!teamPersonalities.contains(member.getPersonality()))
                teamPersonalities.add(member.getPersonality());

        //The prospective team has 3 or more Personality types, no requirement is needed
        if (teamPersonalities.size() > 2) return null;

        //The prospective team has only 1 or 2 Personality types, but team has rooms for 2 more Personality types
        //So personality enforcement is not yet required, but indicates Team's requirement on Personality
        List<SharedEnums.PERSONALITIES> required = new ArrayList<>(allPersonalities);
        required.removeAll(teamPersonalities);

        if (teamMembers.size() == 1 || (
            teamMembers.size() == 2 && teamPersonalities.size() >= 1) || (
            teamMembers.size() == 3 && teamPersonalities.size() >= 2)
        ) return new Pair<>(false, required);

        return new Pair<>(true, required);
    }

    /**
     * Checks for Personality Imbalance of 2 Teams in swapping Students.
     * Params are the Teams and the Student it offered for swap.
     * Returns a Pair for each Team, with Boolean indicating if Personality imbalance occurs,
     * and List of Personalities that it needs on the offered Student.
     * @param first Pair<Team, Student>
     * @param second Pair<Team, Student>
     * @return Pair<
            * Pair<Boolean, List<SharedEnums.PERSONALITIES>>,
     *        Pair<Boolean, List<SharedEnums.PERSONALITIES>>
     *      >
     */
    public static Pair<
        Pair<Boolean, List<SharedEnums.PERSONALITIES>>,
        Pair<Boolean, List<SharedEnums.PERSONALITIES>>
    > isImbalancePersonalityOnSwap(Pair<Team, Student> first, Pair<Team, Student> second) {
        Team firstTeam = first.getKey();
        Team secondTeam = second.getKey();

        //Fake removing the Student that each Team offers for swap,
        // and add the offered Student from the other Team to have the prospective Teams
        firstTeam.getMembers().remove(first.getValue());
        secondTeam.getMembers().remove(second.getValue());

        //Call the above method to produce requirements for each Team
        Pair<Boolean, List<SharedEnums.PERSONALITIES>> firstTeamImbalanceCheck =
                checkImbalancePersonalityOnAssign(new Pair<>(firstTeam, second.getValue()));
        Pair<Boolean, List<SharedEnums.PERSONALITIES>> secondTeamImbalanceCheck =
                checkImbalancePersonalityOnAssign(new Pair<>(secondTeam, first.getValue()));

        //If 1 or both Teams disagree on the offered Student, return at this
        if (firstTeamImbalanceCheck != null || secondTeamImbalanceCheck != null)
            return new Pair<>(firstTeamImbalanceCheck, secondTeamImbalanceCheck);

        return null;
    }
}
