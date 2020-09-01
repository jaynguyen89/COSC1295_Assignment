package cosc1295.src.controllers.activities;

import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.GUI_ACTION_CONTEXT;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LaunchActivity extends AnchorPane implements Activity {

    private Consumer<GUI_ACTION_CONTEXT> intent;

    public void drawLaunchingContents(Scene container) {
        this.getStyleClass().add("panel-wrapper");
        this.prefWidthProperty().bind(container.widthProperty());
        this.prefHeightProperty().bind(container.heightProperty());

        Label title = new Label();
        title.setText("Team Management UI");
        this.getChildren().add(title);

        title.getStyleClass().add("title");
        title.prefWidthProperty().bind(container.widthProperty());
        title.setLayoutY(MARGIN * 2);

        Label subtitle = new Label();
        subtitle.setText("Please select your task");
        this.getChildren().add(subtitle);

        subtitle.getStyleClass().add("subtitle");
        subtitle.prefWidthProperty().bind(container.widthProperty());
        subtitle.setLayoutY(MARGIN * 4.5);

        ComboBox<String> taskDropdown = new ComboBox<>();
        this.getChildren().add(taskDropdown);

        List<String> menuItems = Stream.of(GUI_ACTION_CONTEXT.values())
                                 .map(GUI_ACTION_CONTEXT::getValue)
                                 .collect(Collectors.toList());

        taskDropdown.getStyleClass().add("task-dropdown");
        taskDropdown.getItems().addAll(menuItems);
        taskDropdown.setValue(menuItems.get(0));

        taskDropdown.prefWidthProperty().bind(container.widthProperty().divide(SharedConstants.GUI_ASPECT_RATIO));
        taskDropdown.setLayoutY(MARGIN * 9.5);

        AnchorPane.setLeftAnchor(taskDropdown, (this.getPrefWidth() - taskDropdown.getPrefWidth())/2);
        container.widthProperty().addListener(((observable, oldValue, newValue) -> {
            AnchorPane.setLeftAnchor(
                taskDropdown, (
                AnchorPane.getLeftAnchor(taskDropdown) + ((Double) newValue - (Double) oldValue) / 2
            ));
        }));

        Button goButton = new Button();
        goButton.getStyleClass().add("go-button");
        this.getChildren().add(goButton);

        goButton.setText("Go");
        goButton.setPrefWidth(MARGIN * 5);
        AnchorPane.setBottomAnchor(goButton, MARGIN * 1.5);
        AnchorPane.setLeftAnchor(goButton, (this.getPrefWidth() - goButton.getPrefWidth())/2);

        container.widthProperty().addListener(((observable, oldValue, newValue) -> {
            AnchorPane.setLeftAnchor(
                goButton, (
                AnchorPane.getLeftAnchor(goButton) + ((Double) newValue - (Double) oldValue) / 2
            ));
        }));

        Label message = new Label();
        message.setVisible(false);
        message.prefWidthProperty().bind(container.widthProperty().divide(SharedConstants.GUI_ASPECT_RATIO));
        AnchorPane.setBottomAnchor(message, MARGIN * 4.25);

        AnchorPane.setLeftAnchor(message, (this.getPrefWidth() - message.getPrefWidth())/2);
        container.widthProperty().addListener(((observable, oldValue, newValue) -> {
            AnchorPane.setLeftAnchor(
                message, (
                AnchorPane.getLeftAnchor(message) + ((Double) newValue - (Double) oldValue) / 2
            ));
        }));

        message.getStyleClass().add("message");
        this.getChildren().add(message);

        taskDropdown.setOnAction(event -> { message.setVisible(false); });

        goButton.setOnAction(event -> {
            String selectedOption = taskDropdown.getSelectionModel().getSelectedItem();
            if (menuItems.indexOf(selectedOption) == 0) {
                message.setText("You haven't selected a task. Please choose one in dropdown.");
                message.setVisible(true);
            }
            else
                intent.accept(GUI_ACTION_CONTEXT.values()[menuItems.indexOf(selectedOption)]);
        });
    }

    @Override
    public <T> void setIntent(Consumer<T> intent) {
        this.intent = (Consumer<GUI_ACTION_CONTEXT>) intent;
    }
}
