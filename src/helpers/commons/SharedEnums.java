package helpers.commons;

import java.util.Arrays;
import java.util.List;

public final class SharedEnums {

    public enum APPLICATION_MENU {
        A("Add Company"),
        B("Add Project Owner"),
        C("Add Project"),
        D("Capture Student Personalities"),
        E("Add Student Preferences"),
        F("Shortlist Projects"),
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
        ADDRESS, COMPANY, ROLE, PROJECT_OWNER, PROJECT
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

        PERSONALITIES(String value) { this.value = value; }
    }

    public static List<String> getAllEnumItemsAsList(Class<? extends Enum<?>> any) {
        String[] items = Arrays.stream(any.getEnumConstants())
                               .map(Enum::name)
                               .toArray(String[]::new);

        return Arrays.asList(items);
    }
}