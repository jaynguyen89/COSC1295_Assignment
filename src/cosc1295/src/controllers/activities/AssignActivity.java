package cosc1295.src.controllers.activities;

import cosc1295.src.views.gui.ContentInflator;
import helpers.commons.SharedEnums;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.util.function.Consumer;

public class AssignActivity extends AnchorPane implements Activity {

    private Consumer<SharedEnums.GUI_ACTION_CONTEXT> intent;

    public void drawAssigningTaskContents(Scene container) {
        this.getStyleClass().add("panel-wrapper");
        this.prefWidthProperty().bind(container.widthProperty());
        this.prefHeightProperty().bind(container.heightProperty());

        Label title = new Label();
        title.setText("Assign Students To Teams");
        this.getChildren().add(title);

        title.getStyleClass().add("title");
        title.prefWidthProperty().bind(container.widthProperty());
        title.setLayoutY(40);
    }

    @Override
    public <T> void setIntent(Consumer<T> intent) {
        this.intent = (Consumer<SharedEnums.GUI_ACTION_CONTEXT>) intent;
    }
}
