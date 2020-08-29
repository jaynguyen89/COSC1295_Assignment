package cosc1295.src.views.gui.activities;

import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LaunchActivity extends AnchorPane {

    public Scene drawContentsToScene(Scene container) {
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getStyleClass().add("panel-wrapper");
        anchorPane.prefWidthProperty().bind(container.widthProperty());
        anchorPane.prefHeightProperty().bind(container.heightProperty());

        Label title = new Label();
        title.setText("Team Management UI");
        anchorPane.getChildren().add(title);

        title.getStyleClass().add("title");
        title.prefWidthProperty().bind(container.widthProperty());
        title.setLayoutY(40);

        Label subtitle = new Label();
        subtitle.setText("Please select your task");
        anchorPane.getChildren().add(subtitle);

        subtitle.getStyleClass().add("subtitle");
        subtitle.prefWidthProperty().bind(container.widthProperty());
        subtitle.setLayoutY(90);

        ComboBox<String> taskDropdown = new ComboBox<>();
        anchorPane.getChildren().add(taskDropdown);

        List<String> menuItems = Stream.of(SharedEnums.GUI_ACTION_CONTEXT.values())
                                 .map(SharedEnums.GUI_ACTION_CONTEXT::getValue)
                                 .collect(Collectors.toList());

        taskDropdown.getStyleClass().add("task-dropdown");
        taskDropdown.getItems().addAll(menuItems);
        taskDropdown.setValue(menuItems.get(0));

        taskDropdown.prefWidthProperty().bind(container.widthProperty().divide(SharedConstants.GUI_ASPECT_RATIO));
        taskDropdown.setLayoutY(180);
        taskDropdown.setLayoutX((anchorPane.getPrefWidth() - taskDropdown.getPrefWidth())/2);

        Button goButton = new Button();
        goButton.getStyleClass().add("go-button");
        anchorPane.getChildren().add(goButton);

        goButton.setText("GO");
        goButton.setLayoutY(390);
        goButton.setLayoutX((anchorPane.getPrefWidth() - goButton.getPrefWidth())/2);

        container.setRoot(anchorPane);
        return container;
    }
}
