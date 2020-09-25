package cosc1295.src.controllers.activities;

import com.sun.istack.internal.NotNull;
import helpers.commons.SharedConstants;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
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

/**
 * The Activity Interface
 */
public interface IActivity {

    double MARGIN = 20.0; //All constraints in the views are calculated with this value

    <T> void setIntent(Consumer<T> callback); //The Intent passed to an Activity

    /**
     * Calculates the offset to slide the widgets (up/down, left/right), and
     * resize the widgets when the GUI window is resized.
     * @param values Number[]
     * @return double
     */
    static double offset(Number... values) {
        double offset = ((Double) values[1] - (Double) values[0]) / 2;

        if ((Double) values[0] > (Double) values[1] && offset > 0)
            offset *= -1;

        return offset;
    }

    /**
     * Checks an observable object if it has any listener attached to it,
     * by checking both of `listener` and `changeListeners` fields of the observable.
     * Returns true if the observable has any attached listener, otherwise false.
     * @param observable SimpleObjectProperty<T>
     * @param <T> Type
     * @return boolean
     */
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
            return false; //No listener attached
        }

        return false;
    }

    /**
     * Replaces the texts in a Label or Button element. The element is searched by ID.
     * Param `type` is the type of element.
     * @param type Class<T>
     * @param newText String
     * @param elementId String
     * @param activity Pane
     * @param <T> Type
     */
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

    /**
     * Removes an element from the Activity. The element is searched by ID.
     * @param elementId String
     * @param activity Pane
     */
    static void removeElementIfExists(String elementId, Pane activity) {
        activity.getChildren().removeIf(
            element -> element.getId() != null && element.getId().equals(elementId)
        );
    }

    /**
     * Toggles (show/hide) an element in the Activity. The element is searched by ID.
     * @param elementId String
     * @param activity Pane
     */
    static void toggleElement(String elementId, Pane activity) {
        AtomicReference<Node> elementToToggle = new AtomicReference<>();
        activity.getChildren().forEach(
            element -> {
                if (element.getId().equals(elementId)) elementToToggle.set(element);
            }
        );

        elementToToggle.get().setVisible(!elementToToggle.get().isVisible());
    }

    /**
     * Constraints the position XY of an element in the Activity.
     * @param button Button
     * @param activity Pane
     */
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

    /**
     * Draws the title on an Activity, also constraints the size and position of title in the Activity.
     * @param container Scene
     * @param activity Pane
     * @param title String
     */
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

    /**
     * Draws a status message on an Activity when an exception occurs.
     * Also constraints the position and size of the message in the Activity.
     * This exception is usually the file reading/writing exceptions.
     * @param container Scene
     * @param activity Pane
     * @param message String
     */
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

    /**
     * Draws 2 fixed buttons on an Activity: the `Back` button and the main button.
     * The main button is a button used to commit changes made to data.
     * If isErrorOccurred==true, `Back` button is drawn, otherwise the main button.
     * Also constrains the position of the buttons in the Activity.
     * @param container Scene
     * @param activity Pane
     * @param isErrorOccurred boolean
     * @param buttons Button[]
     */
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

    /**
     * Draws a `success` status message on an Activity after user successfully commit changes made to data.
     * Also constraints the size and position of the message in the Activity.
     * @param message String
     * @param activity Pane
     */
    static void drawSuccessMessage(String message, Pane activity) {
        Label success = new Label(message);
        success.getStyleClass().add("message-success");
        activity.getChildren().add(success);

        if (activity.getId().toLowerCase().contains(AssignActivity.class.getSimpleName().toLowerCase())) {
            AnchorPane.setBottomAnchor(success, MARGIN * 3.25);
            AnchorPane.setRightAnchor(success, MARGIN);
            AnchorPane.setLeftAnchor(success, (activity.getPrefWidth() - success.getPrefWidth()) / 2);

            success.setPrefWidth(activity.getPrefWidth() / 2 - MARGIN);
            activity.prefWidthProperty().addListener((observable, oldValue, newValue) ->
                success.setPrefWidth(success.getPrefWidth() + offset(oldValue, newValue) / 2)
            );
        }
        else {
            AnchorPane.setBottomAnchor(success, MARGIN * 4);
            AnchorPane.setLeftAnchor(success, (activity.getPrefWidth() - success.getPrefWidth()) / 4);
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
