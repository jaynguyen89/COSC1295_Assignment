package cosc1295.src.views.gui;

import cosc1295.src.controllers.activities.*;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.GUI_ACTION_CONTEXT;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton object class
 */
public class ContentInflator {

    private final String STYLES_DIR = System.getProperty("user.dir").replace("\\", "/") +
                                      "/src/cosc1295/src/views/gui/styles/";

    private static final HashMap<GUI_ACTION_CONTEXT, Pane> sceneCollection = new HashMap<>();
    private static ContentInflator inflator;

    private ContentInflator() { }

    public static ContentInflator getInstance() {
        if (inflator == null) {
            synchronized (ContentInflator.class) {
                if (inflator == null) {
                    inflator = new ContentInflator();
                }
            }
        }

        if (sceneCollection.isEmpty())
            inflator.gatherResources();

        return inflator;
    }

    private void gatherResources() {
        for (Map.Entry<GUI_ACTION_CONTEXT, String> entry : SharedConstants.RESOURCES.entrySet()) {
            Pane activity;

            switch (entry.getKey()) {
                case LAUNCH:
                    activity = new LaunchActivity();
                    break;
                case ASSIGN:
                    activity = new AssignActivity();
                    break;
                case SWAP:
                    activity = new SwapActivity();
                    break;
                case REMOVE:
                    activity = new RemoveActivity();
                    break;
                case PROJECT:
                    activity = new ProjectActivity();
                    break;
                default: //STATS
                    activity = new StatisticsActivity();
                    break;
            }

            sceneCollection.put(entry.getKey(), activity);
        }
    }

    public Scene inflate(GUI_ACTION_CONTEXT context, Scene container) {

        container.getStylesheets().clear();
        container.getStylesheets().add("file:///" + STYLES_DIR + SharedConstants.RESOURCES.get(context));

        Pane activity = sceneCollection.get(context);
        activity.getChildren().clear();
        switch (context) {
            case LAUNCH:
                ((LaunchActivity) activity).drawLaunchingContents(container);
                break;
            case ASSIGN:
                ((AssignActivity) activity).drawAssigningTaskContents(container, null);
                break;
            case SWAP:
                ((SwapActivity) activity).drawSwappingTaskContents(container, null);
                break;
            case REMOVE:
                ((RemoveActivity) activity).drawRemovalTaskContents(container, null);
                break;
            case PROJECT:
                ((ProjectActivity) activity).drawProjectSettingTaskContents(container, null);
                break;
            default: //STATS
                ((StatisticsActivity) activity).drawStatisticsContents(container);
                break;
        }

        container.setRoot(activity);
        return container;
    }
}