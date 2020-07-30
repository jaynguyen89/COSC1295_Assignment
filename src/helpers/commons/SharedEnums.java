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

        public final String value;

        APPLICATION_MENU(String value) { this.value = value; }
    }

    public enum CONFIRMATIONS {
        Y("YES"),
        N("NO");

        public final String value;
        public String getValue() { return value; }

        CONFIRMATIONS(String value) { this.value = value; }
    }

    public enum FLASH_TYPES {
        NONE(SharedConstants.EMPTY_STRING),
        SUCCESS("Success! "),
        ATTENTION("Attention! "),
        ERROR("Error! ");

        public final String value;

        FLASH_TYPES(String value) { this.value = value; }
    }

    public enum DATA_TYPES {
        ADDRESS, COMPANY, ROLE, PROJECT_OWNER, PROJECT
    }

    public enum RANKINGS {
        LOW, AVERAGE, HIGH, HIGHEST
    }

    public enum SKILLS {
        A("Analytics & Big Data"),
        N("Networking & Security"),
        P("Programming & Software Engineering"),
        W("Web & Mobile Applications");

        public final String value;
        public String getValue() { return value; }

        SKILLS(String value) { this.value = value; }
    }

    public static List<String> getAllEnumItemsAsList(Class<? extends Enum<?>> any) {
        String[] items = Arrays.stream(any.getEnumConstants())
                               .map(Enum::name)
                               .toArray(String[]::new);

        return Arrays.asList(items);
    }
}