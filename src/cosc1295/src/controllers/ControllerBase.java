package cosc1295.src.controllers;

import cosc1295.designs.Flasher;
import helpers.commons.SharedEnums.DATA_TYPES;

class ControllerBase {

    protected final Flasher flasher = Flasher.getInstance();

    protected boolean checkUniqueIdIntegrityFor(DATA_TYPES type, String uniqueId) {
        //TODO
        return true;
    }
}
