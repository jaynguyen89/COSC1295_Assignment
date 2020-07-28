package helpers.commons;

import java.util.Arrays;

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

        APPLICATION_MENU(String value) {
            this.value = value;
        }
    }

    public enum FLASH_TYPES {
        NONE(SharedConstants.EMPTY_STRING),
        SUCCESS("Success! "),
        ATTENTION("Attention! "),
        ERROR("Error! ");

        public final String value;

        FLASH_TYPES(String value) {
            this.value = value;
        }
    }

    public static String[] getAllEnumItemsAsArray(Class<? extends Enum<?>> any) {
        return Arrays.stream(any.getEnumConstants())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}