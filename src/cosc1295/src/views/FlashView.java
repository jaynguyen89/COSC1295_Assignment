package cosc1295.src.views;

import cosc1295.src.models.Flash;

public class FlashView {

    public void printFlashMessage(Flash flash) {
        System.out.println(
                flash.getType() + flash.getMessage()
        );
    }
}
