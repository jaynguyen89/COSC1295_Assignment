package cosc1295.src.controllers;

import cosc1295.src.models.Flash;
import cosc1295.src.views.FlashView;

public class FlashController {

    private FlashView flashView;

    public FlashController(FlashView view) {
        flashView = view;
    }

    public void flashing(Flash flash) {
        flashView.printFlashMessage(flash);
    }
}
