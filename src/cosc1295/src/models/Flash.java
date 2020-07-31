package cosc1295.src.models;

import helpers.commons.SharedEnums.FLASH_TYPES;

public class Flash {

    private final String message;

    private final FLASH_TYPES type;

    public Flash(String message, FLASH_TYPES type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type.getValue();
    }
}
