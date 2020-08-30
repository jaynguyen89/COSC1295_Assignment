package cosc1295.src.controllers.activities;

import java.util.function.Consumer;

public interface Activity {

    <T> void setIntent(Consumer<T> callback);
}
