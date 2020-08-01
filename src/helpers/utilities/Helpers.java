package helpers.utilities;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import cosc1295.providers.bases.ServiceLocator;
import cosc1295.providers.services.CompanyService;
import cosc1295.providers.services.ProjectOwnerService;
import cosc1295.providers.services.ProjectService;
import cosc1295.src.models.Company;
import cosc1295.src.models.Project;
import cosc1295.src.models.ProjectOwner;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;

import javafx.util.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public static Boolean validateLiteralName(String name, boolean isHuman) {
        if (isNullOrBlankOrEmpty(name)) return null;

        Pattern nameRegex = Pattern.compile(isHuman ? "^[a-zA-Z.\\-' ]+$" : "^[\\w.\\-'() ]+$");
        Matcher matcher = nameRegex.matcher(name);

        return matcher.matches();
    }

    public static boolean isIntegerNumber(@Nullable String any) {
        try {
            Integer.parseInt(any);
        } catch (NumberFormatException | NullPointerException ex) {
            return false;
        }

        return true;
    }

    public static Pair<String, Boolean> validateAndPrettifyUniqueId(String uniqueId) {
        if (Helpers.isNullOrBlankOrEmpty(uniqueId))
            return null;

        uniqueId = uniqueId.trim()
                .replaceAll(
                        SharedConstants.MULTIPLE_SPACE,
                        SharedConstants.EMPTY_STRING
                )
                .toUpperCase();

        Pattern idRegex = Pattern.compile("^[\\w]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = idRegex.matcher(uniqueId);

        return new Pair<>(uniqueId, matcher.matches());
    }

    public static <T> Boolean checkUniqueIdAvailableFor(Class<T> type, String uniqId) {
        ServiceLocator locator = ServiceLocator.getInstance();

        CompanyService companyService;
        ProjectOwnerService projectOwnerService;
        ProjectService projectService;

        try {
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
    
    public static HashMap<String, Integer> sortDescending(HashMap<String, Integer> any) {
        List<Map.Entry<String, Integer>> listToSort = new ArrayList<>(any.entrySet());
        listToSort.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        
        HashMap<String, Integer> sortedMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : listToSort)
            sortedMap.put(entry.getKey(), entry.getValue());
        
        return sortedMap;
    }
}
