package cosc1295.src.controllers.activities;

import javafx.scene.Scene;
import javafx.scene.control.Control;

import java.util.function.Consumer;

public interface Activity {

    double MARGIN = 20.0;

    <T> void setIntent(Consumer<T> callback);
}
