package cosc1295.src.controllers.activities;

import com.sun.istack.internal.NotNull;
import helpers.commons.SharedConstants;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public interface IActivity {

    double MARGIN = 20.0;

    <T> void setIntent(Consumer<T> callback);

    static double offset(Number... values) {
        double offset = ((Double) values[1] - (Double) values[0]) / 2;

        if ((Double) values[0] > (Double) values[1] && offset > 0)
            offset *= -1;

        return offset;
    }

    static <T> boolean hasListener(@NotNull SimpleObjectProperty<T> observable) {
        Object listener;

        try {
            Field observableField = ObjectPropertyBase.class.getDeclaredField("helper");
            observableField.setAccessible(true);

            Object value = observableField.get(observable);

            observableField = value.getClass().getDeclaredField("listener");
            observableField.setAccessible(true);

            listener = observableField.get(value);
            if (listener != null) return true;

            observableField = value.getClass().getDeclaredField("changeListeners");
            observableField.setAccessible(true);

            listener = observableField.get(value);
            if (listener != null) return true;
        } catch (NullPointerException | NoSuchFieldException | IllegalAccessException e) {
            return false;
        }

        return false;
    }

    static <T> void changeElementText(Class<T> type, String newText, String elementId, Pane activity) {
        for (Node element : activity.getChildren())
            if (element.getId() != null && element.getId().equals(elementId)) {
                if (type.getSimpleName().equals(Label.class.getSimpleName()))
                    ((Label) element).setText(newText);

                if (type.getSimpleName().equals(Button.class.getSimpleName())) {
                    ((Button) element).setText(newText);
                }
            }
    }

    static void removeElementIfExists(String elementId, Pane activity) {
        activity.getChildren().removeIf(
            element -> element.getId() != null && element.getId().equals(elementId)
        );
    }

    static void toggleElement(String elementId, Pane activity) {
        AtomicReference<Node> elementToToggle = new AtomicReference<>();
        activity.getChildren().forEach(
            element -> {
                if (element.getId().equals(elementId)) elementToToggle.set(element);
            }
        );

        elementToToggle.get().setVisible(!elementToToggle.get().isVisible());
    }

    static void constraintButton(Button button, Pane activity) {
        button.setPrefWidth(MARGIN * 5);
        AnchorPane.setBottomAnchor(button, MARGIN * 2);
        AnchorPane.setLeftAnchor(button, (activity.getPrefWidth() - button.getPrefWidth())/2);

        activity.prefWidthProperty().addListener((observable, oldValue, newValue) ->
            AnchorPane.setLeftAnchor(
                button, (
                    AnchorPane.getLeftAnchor(button) + ((Double) newValue - (Double) oldValue) / 2
                )
            )
        );
    }

    static void drawActivityTitle(Scene container, Pane activity, String title) {
        activity.getStyleClass().add("panel-wrapper");
        activity.prefWidthProperty().bind(container.widthProperty());
        activity.prefHeightProperty().bind(container.heightProperty());

        Label titleLabel = new Label(title);
        activity.getChildren().add(titleLabel);

        titleLabel.getStyleClass().add("title");
        titleLabel.prefWidthProperty().bind(container.widthProperty());
        titleLabel.setLayoutY(MARGIN / 2);
    }

    static void drawActivityMessageOnException(Scene container, Pane activity, String message) {
        Label warning = new Label(message);
        activity.getChildren().add(warning);

        warning.getStyleClass().add("warning");
        warning.setPrefWidth(MARGIN * 25);

        AnchorPane.setTopAnchor(warning, MARGIN * 5);
        AnchorPane.setLeftAnchor(warning, (activity.getPrefWidth() - warning.getPrefWidth())/2);

        container.widthProperty().addListener((observable, oldValue, newValue) ->
            AnchorPane.setLeftAnchor(
                warning, (
                    AnchorPane.getLeftAnchor(warning) + ((Double) newValue - (Double) oldValue) / 2
                )
            ));
    }

    static void drawActivityFixedButtons(
        Scene container, Pane activity, boolean isErrorOccurred, Button... buttons
    ) {
        Button backButton = buttons[0];
        backButton.getStyleClass().add(isErrorOccurred ? "done-button" : "back-button");
        activity.getChildren().add(backButton);

        if (!isErrorOccurred) {
            backButton.setLayoutY(MARGIN / 2);
            backButton.setLayoutX(MARGIN / 2);

            Button mainButton = buttons[1];
            mainButton.setId("main-button");
            mainButton.setDisable(true);
            mainButton.getStyleClass().add("done-button");
            mainButton.setPrefWidth(MARGIN * 5);

            mainButton.getStyleClass().add("done-button");
            activity.getChildren().add(mainButton);

            AnchorPane.setBottomAnchor(mainButton, MARGIN / 1.75);
            AnchorPane.setLeftAnchor(mainButton, (activity.getPrefWidth() - mainButton.getPrefWidth()) / 2);

            container.widthProperty().addListener((observable, oldValue, newValue) ->
                AnchorPane.setLeftAnchor(
                    mainButton,
                    AnchorPane.getLeftAnchor(mainButton) + ((Double) newValue - (Double) oldValue) / 2
                )
            );
        }
        else
            constraintButton(backButton, activity);
    }

    static void drawSuccessMessage(String message, Pane activity) {
        Label success = new Label(message + "           ");
        success.getStyleClass().add("message-success");
        activity.getChildren().add(success);

        AnchorPane.setLeftAnchor(success, (activity.getPrefWidth() - success.getPrefWidth()) / 2);
        if (activity.getId().toLowerCase().contains(AssignActivity.class.getSimpleName().toLowerCase())) {
            AnchorPane.setBottomAnchor(success, MARGIN * 3.25);
            AnchorPane.setRightAnchor(success, MARGIN);

            success.setPrefWidth(activity.getPrefWidth() / 2 - MARGIN);
            success.setStyle("-fx-wrap-text: true;");

            activity.prefWidthProperty().addListener((observable, oldValue, newValue) ->
                success.setPrefWidth(success.getPrefWidth() + offset(oldValue, newValue) / 2)
            );
        }
        else {
            AnchorPane.setBottomAnchor(success, MARGIN * 4);
            success.setPrefWidth(activity.getPrefWidth() / SharedConstants.GUI_ASPECT_RATIO);

            activity.prefWidthProperty().addListener(((observable, oldValue, newValue) -> {
                success.setPrefWidth(success.getPrefWidth() + offset(oldValue, newValue));
                AnchorPane.setLeftAnchor(success, AnchorPane.getLeftAnchor(success) + offset(oldValue, newValue));
            }));
        }

        Button hider = new Button("Close");
        hider.getStyleClass().add("mini-button");
        success.setGraphic(hider);
        success.setContentDisplay(ContentDisplay.RIGHT);


        hider.setOnAction(event -> success.setVisible(false));
    }
}
