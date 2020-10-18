package cosc1295.src.views.gui;

import cosc1295.src.controllers.activities.IActivity;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.GUI_ACTION_CONTEXT;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * This class is the entry point to GUI features.
 * The design of GUI part follows the structure of an Android application.
 */
public class TeamGuiView extends Application {

	//The inflator is to inflate an Activity into the Scene
	private final ContentInflator inflator = ContentInflator.getInstance();

	public void launchGuiWindow() {
		try {
			launch();
		} catch (IllegalStateException ex) {
			System.out.println(
				"\nYou have launched the GUI once during this session.\n" +
				"If you need to use the GUI again, please restart the app.\n\n"
			);
		}
	}

	@Override
	public void start(Stage applicationWindow) {
		applicationWindow.setTitle("Team Management UI");
		applicationWindow.maximizedProperty().addListener(((observable, oldValue, newValue) -> {
			if (newValue) applicationWindow.setMaximized(false);
		}));

		applicationWindow.setWidth(SharedConstants.DIMENSIONS.get("WIDTH")); //896px
		applicationWindow.setHeight(SharedConstants.DIMENSIONS.get("HEIGHT")); //504px

		applicationWindow.setMaxWidth(SharedConstants.DIMENSIONS.get("MAX_WIDTH")); //1024px
		applicationWindow.setMaxHeight(SharedConstants.DIMENSIONS.get("MAX_HEIGHT")); //576px

		applicationWindow.setMinWidth(SharedConstants.DIMENSIONS.get("WIDTH")); //896px
		applicationWindow.setMinHeight(SharedConstants.DIMENSIONS.get("HEIGHT")); //504px

		Scene appScene = new Scene(
				new Pane(),
				SharedConstants.DIMENSIONS.get("WIDTH"), //896px
				SharedConstants.DIMENSIONS.get("HEIGHT") //504px
		);

		applicationWindow.minWidthProperty().bind(appScene.heightProperty().multiply(SharedConstants.GUI_ASPECT_RATIO));
		applicationWindow.minHeightProperty().bind(appScene.widthProperty().divide(SharedConstants.GUI_ASPECT_RATIO));

		//On launching the GUI, the LaunchActivity is inflated to Scene by default...
		appScene = inflator.inflate(GUI_ACTION_CONTEXT.LAUNCH, appScene);

		//...then, basing on the navigation option selected by user, an appropriate Activity is inflated
		//The newly inflated Activity replaces the current one in the Scene.
		Scene finalAppScene = appScene;
		((IActivity) appScene.getRoot()).setIntent(context -> {
			inflator.inflate((GUI_ACTION_CONTEXT) context, finalAppScene); //Inflate the Activity

			//Then set an Intent on that Activity to control the navigations from it
			((IActivity) finalAppScene.getRoot()).setIntent(secondaryContext -> {
				inflator.inflate((GUI_ACTION_CONTEXT) secondaryContext, finalAppScene);
				applicationWindow.setScene(finalAppScene);
			});

			applicationWindow.setScene(finalAppScene);
		});

		applicationWindow.setScene(appScene);
		applicationWindow.show();
	}
}
