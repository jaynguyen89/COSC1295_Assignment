package cosc1295.src.controllers;

import cosc1295.src.models.Flash;
import cosc1295.src.views.FlashView;

public final class FlashController {

    private static FlashController flashController;
    private FlashView flashView;

    private FlashController() { }

    public static FlashController getInstance() {
        if (flashController == null) {
            synchronized (FlashController.class) {
                flashController = flashController == null ?
                        new FlashController() :
                        flashController;
            }

            flashController.setFlashView();
        }

        return flashController;
    }

    public void flash(Flash flash) {
        flashView.printFlashMessage(flash);
    }

    private void setFlashView() {
        flashView = new FlashView();
    }
}
