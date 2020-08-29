package cosc1295.src.views.gui;

import cosc1295.src.views.gui.activities.LaunchActivity;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.GUI_ACTION_CONTEXT;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;

class ContentInflator {

    private final String STYLES_DIR = System.getProperty("user.dir").replace("\\", "/") +
                                      "/src/cosc1295/src/views/gui/styles/";

    private static final HashMap<GUI_ACTION_CONTEXT, Pane> sceneCollection = new HashMap<>();

    void gatherResources() {
        for (Map.Entry<GUI_ACTION_CONTEXT, String> entry : SharedConstants.RESOURCES.entrySet()) {
            LaunchActivity activity = new LaunchActivity();
            sceneCollection.put(entry.getKey(), activity);
            //TODO
        }
    }

    Scene inflate(GUI_ACTION_CONTEXT context, Scene container) {
        container.getStylesheets().add("file:///" + STYLES_DIR + SharedConstants.RESOURCES.get(context));

        LaunchActivity activity = (LaunchActivity) sceneCollection.get(context);
        container = activity.drawContentsToScene(container);

        return container;
    }
}