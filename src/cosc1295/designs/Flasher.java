package cosc1295.designs;

import cosc1295.src.models.Flash;
import cosc1295.src.views.FlashView;

public final class Flasher {

    private static Flasher flasher;
    private FlashView flashView;

    private Flasher() { }

    public static Flasher getInstance() {
        if (flasher == null) {
            synchronized (Flasher.class) {
                flasher = flasher == null ?
                        new Flasher() :
                        flasher;
            }

            flasher.setFlashView();
        }

        return flasher;
    }

    public void flash(Flash flash) {
        flashView.printFlashMessage(flash);
    }

    public boolean promptForConfirmation(Flash flash) {
        return flashView.promptForConfirmation(flash);
    }

    private void setFlashView() {
        flashView = new FlashView();
    }
}
