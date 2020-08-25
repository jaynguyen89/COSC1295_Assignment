package helpers.commons;

import java.util.Arrays;
import java.util.List;

/**
 * All the Enum classes that will be used in the whole project.
 */
public final class SharedEnums {

    public enum APPLICATION_MENU {
        A("Add Company"),
        B("Add Project Owner"),
        C("Add Project"),
        D("Capture Student Personalities"),
        E("Add Student Preferences"),
        F("Shortlist Projects"),
        G("Form Teams"),
        H("Display Team Fitness Metrics"),
        I("Set or Change Team Project"),
        X("Quit");

        private final String value;
        public String getValue() { return value; }

        APPLICATION_MENU(String value) { this.value = value; }
    }

    public enum CONFIRMATIONS {
        Y("YES"),
        N("NO");

        private final String value;
        public String getValue() { return value; }

        CONFIRMATIONS(String value) { this.value = value; }
    }

    public enum FLASH_TYPES {
        NONE(SharedConstants.EMPTY_STRING),
        SUCCESS("Success! "),
        ATTENTION("Attention! "),
        ERROR("Error! ");

        private final String value;
        public String getValue() { return value; }

        FLASH_TYPES(String value) { this.value = value; }
    }

    public enum DATA_TYPES {
        ADDRESS, COMPANY, ROLE, PROJECT_OWNER, PROJECT, STUDENT, PREFERENCE, PROJECT_TEAM, FITNESS_METRICS
    }

    public enum RANKINGS {
        LOW(0), AVERAGE(1), HIGH(2), HIGHEST(3);

        private final int value;
        public int getValue() { return value; }

        RANKINGS(int value) { this.value = value; }
    }

    public enum SKILLS {
        A("Analytics & Big Data"),
        N("Networking & Security"),
        P("Programming & Software Engineering"),
        W("Web & Mobile Applications");

        private final String value;
        public String getValue() { return value; }

        SKILLS(String value) { this.value = value; }
    }

    public enum PERSONALITIES {
        A("Director"),
        B("Socializer"),
        C("Thinker"),
        D("Supporter");

        private final String value;
        public String getValue() { return value; }

        public static String display() {
            return "\t" + A.name() + ". " + A.value + "\n" +
                "\t" + B.name() + ". " + B.value + "\n" +
                "\t" + C.name() + ". " + C.value + "\n" +
                "\t" + D.name() + ". " + D.value + "\n";
        }

        public static PERSONALITIES getPersonality(String input) {
            if (input.equals(A.name())) return A;
            if (input.equals(B.name())) return B;
            if (input.equals(C.name())) return C;
            if (input.equals(D.name())) return D;

            return null;
        }

        PERSONALITIES(String value) { this.value = value; }
    }

    /**
     * Get all attributes' name in an Enum class into a List of String through reflection
     * @param any Class<? extends Enum<?>>
     * @return List<String>
     */
    public static List<String> getAllEnumAttributesAsList(Class<? extends Enum<?>> any) {
        String[] items = Arrays.stream(any.getEnumConstants())
                               .map(Enum::name)
                               .toArray(String[]::new);

        return Arrays.asList(items);
    }
}