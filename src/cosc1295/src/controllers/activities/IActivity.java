package cosc1295.src.controllers.activities;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public interface IActivity {

    double MARGIN = 20.0;

    <T> void setIntent(Consumer<T> callback);

    static void changeElementText(Class<Label> type, String newText, String elementId, Pane activity) {
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
}
