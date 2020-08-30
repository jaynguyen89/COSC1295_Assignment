package cosc1295.src.views.gui;

import cosc1295.src.controllers.activities.Activity;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.GUI_ACTION_CONTEXT;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TeamGuiView extends Application {

	private final ContentInflator inflator = ContentInflator.getInstance();

	public void launchGuiWindow() {
		launch();
	}

	@Override
	public void start(Stage applicationWindow) {
		applicationWindow.setAlwaysOnTop(true);
		applicationWindow.setFullScreen(false);
		applicationWindow.setMaximized(false);
		applicationWindow.setTitle("Team Management UI");

		applicationWindow.setWidth(SharedConstants.DIMENSIONS.get("WIDTH"));
		applicationWindow.setHeight(SharedConstants.DIMENSIONS.get("HEIGHT"));

		applicationWindow.setMaxWidth(SharedConstants.DIMENSIONS.get("MAX_WIDTH"));
		applicationWindow.setMaxHeight(SharedConstants.DIMENSIONS.get("MAX_HEIGHT"));

		applicationWindow.setMinWidth(SharedConstants.DIMENSIONS.get("MIN_WIDTH"));
		applicationWindow.setMinHeight(SharedConstants.DIMENSIONS.get("MIN_HEIGHT"));

		Scene appScene = new Scene(
				new Pane(),
				SharedConstants.DIMENSIONS.get("WIDTH"),
				SharedConstants.DIMENSIONS.get("HEIGHT")
		);

		applicationWindow.minWidthProperty().bind(appScene.heightProperty().multiply(SharedConstants.GUI_ASPECT_RATIO));
		applicationWindow.minHeightProperty().bind(appScene.widthProperty().divide(SharedConstants.GUI_ASPECT_RATIO));

		appScene = inflator.inflate(GUI_ACTION_CONTEXT.LAUNCH, appScene);

		Scene finalAppScene = appScene;
		((Activity) appScene.getRoot()).setIntent(context -> {
			inflator.inflate((GUI_ACTION_CONTEXT) context, finalAppScene);
			applicationWindow.setScene(finalAppScene);
		});

		applicationWindow.setScene(appScene);
		applicationWindow.show();
	}
}
