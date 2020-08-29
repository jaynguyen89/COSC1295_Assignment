package cosc1295.src.views.gui;

import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.GUI_ACTION_CONTEXT;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class TeamGuiView extends Application {

	private final ContentInflator inflator = new ContentInflator();
	private Stage applicationWindow;

	public TeamGuiView() { }

	public void setupGuiWindow() {
		launch();
	}

	@Override
	public void start(Stage primaryStage) {
		applicationWindow = primaryStage;

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
		appScene = inflator.inflate(GUI_ACTION_CONTEXT.LAUNCH, appScene);

		applicationWindow.minWidthProperty().bind(appScene.heightProperty().multiply(SharedConstants.GUI_ASPECT_RATIO));
		applicationWindow.minHeightProperty().bind(appScene.widthProperty().divide(SharedConstants.GUI_ASPECT_RATIO));

		applicationWindow.setScene(appScene);
		applicationWindow.show();
	}

	public void prepareActivities() throws IOException {
		inflator.gatherResources();
	}


}
