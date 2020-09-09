package cosc1295.src.views.gui;

import cosc1295.src.controllers.activities.*;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.GUI_ACTION_CONTEXT;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton object class.
 * This class provides an instance of Inflator, which will
 * inflate an appropriate content into the Scene basing on a navigation option.
 */
public class ContentInflator {

    private final String STYLES_DIR = System.getProperty("user.dir").replace("\\", "/") +
                                      "/src/cosc1295/src/views/gui/styles/";

    //When the GUI launches, before inflating any Activity into Scene,
    // all Activities are prepared and stored into this HashMap
    private static final HashMap<GUI_ACTION_CONTEXT, Pane> activityCollection = new HashMap<>();
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

        if (activityCollection.isEmpty())
            inflator.gatherResources();

        return inflator;
    }

    /**
     * Prepare all Activities and store them into a collection for later use
     */
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

            activityCollection.put(entry.getKey(), activity);
        }
    }

    /**
     * Inflate an Activity into the Scene when user make a navigation.
     * While inflating an Activity, the stylesheet CSS of that Activity will be added to the Scene accordingly.
     * @param context GUI_ACTION_CONTEXT
     * @param container Scene
     * @return Scene
     */
    public Scene inflate(GUI_ACTION_CONTEXT context, Scene container) {

        container.getStylesheets().clear();
        container.getStylesheets().add("file:///" + STYLES_DIR + SharedConstants.RESOURCES.get(context));

        Pane activity = activityCollection.get(context);
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