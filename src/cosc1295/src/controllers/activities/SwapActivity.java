package cosc1295.src.controllers.activities;

import com.sun.istack.internal.Nullable;
import cosc1295.providers.services.ProjectService;
import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Project;
import helpers.commons.SharedEnums.PERSONALITIES;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import helpers.utilities.Helpers;
import helpers.utilities.LogicalAssistant;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
/**
 * This activity allows user to swap Students between 2 selected Teams.
 * When user select both Teams and Students, the requirements will be produced for each Team,
 * and displayed to inform if the Students are good to swap, or any requirements that are
 * not satisfied, so user is informed and can adjust their selection.
 * After swapping, if a team has 4 members, recalculate its Fitness Metrics.
 */
public class SwapActivity extends AnchorPane implements IActivity {

    private Consumer<SharedEnums.GUI_ACTION_CONTEXT> intent;
    private static final String FIRST_TEAM = "FIRST_TEAM";
    private static final String SECOND_TEAM = "SECOND_TEAM";

    //Dependency injections to access data processing services
    private final StudentService studentService;
    private final TeamService teamService;
    private final ControllerBase controllerBase;

    //The observable objects to keep track of the changes made to data
    private final SimpleObjectProperty<ArrayList<Team>> observableTeams;
    private final SimpleObjectProperty<Team> firstTeamInSwap;
    private final SimpleObjectProperty<Student> firstTeamMember;
    private final SimpleObjectProperty<Team> secondTeamInSwap;
    private final SimpleObjectProperty<Student> secondTeamMember;

    public SwapActivity() {
        studentService = new StudentService();
        teamService = new TeamService();
        controllerBase = new ControllerBase();

        observableTeams = new SimpleObjectProperty<>(null);
        firstTeamInSwap = new SimpleObjectProperty<>(null);
        firstTeamMember = new SimpleObjectProperty<>(null);
        secondTeamInSwap = new SimpleObjectProperty<>(null);
        secondTeamMember = new SimpleObjectProperty<>(null);
    }

    public void drawSwappingTaskContents(Scene container, @Nullable String postMessage) {
        this.setId(this.getClass().getSimpleName());
        IActivity.drawActivityTitle(container, this, "Swap Students Between Teams");

        List<Team> teams = teamService.readAllTeamsFromFile();
        List<Student> students = studentService.readAllStudentsFromFile();

        boolean error = teams == null || students == null;
        if (error) drawActivityFailMessage(container, "An error occurred while retrieving data from files.\nPlease try again.");

        error = !error && teams.size() < 2;
        if (error) drawActivityFailMessage(container, "The number of Teams must be at least 2 to perform this task.\nPlease create more Teams first.");

        if (!error) {
            if (postMessage != null) IActivity.drawSuccessMessage(postMessage, this);

            double tablePrefWidth = (container.getWidth() - MARGIN * 3) / 2;
            attachListenersToObservables(tablePrefWidth);

            drawButtonBasedOnContext(container, false);
            LogicalAssistant.setStudentDataInTeams(teams, students);

            observableTeams.set((ArrayList<Team>) teams);
            drawWidgetsForSwappingStudentsTask(tablePrefWidth);
        }
    }

    private void attachListenersToObservables(double initialWidth) {
        SimpleBooleanProperty shouldEnableSwapButton = new SimpleBooleanProperty(false);

        firstTeamInSwap.addListener(observable -> {
            if (firstTeamInSwap.get() == null) {
                eraseMemberSelectionFragment(FIRST_TEAM);
                shouldEnableSwapButton.set(false);
            }
            else drawMemberSelectionFragment(firstTeamInSwap.get().getMembers(), FIRST_TEAM, initialWidth);
        });

        secondTeamInSwap.addListener(observable -> {
            if (secondTeamInSwap.get() == null) {
                eraseMemberSelectionFragment(SECOND_TEAM);
                shouldEnableSwapButton.set(false);
            }
            else drawMemberSelectionFragment(secondTeamInSwap.get().getMembers(), SECOND_TEAM, initialWidth);
        });

        firstTeamMember.addListener(observable -> {
            if (firstTeamMember.get() == null) {
                IActivity.removeElementIfExists("requirement-label", this);
                shouldEnableSwapButton.set(false);
            }
            if (firstTeamMember.get() != null && secondTeamMember.get() != null)
                manageRequirementMessagesAndSwapButton(shouldEnableSwapButton);
        });

        secondTeamMember.addListener(observable -> {
            if (secondTeamMember.get() == null) {
                IActivity.removeElementIfExists("requirement-label", this);
                shouldEnableSwapButton.set(false);
            }
            if (firstTeamMember.get() != null && secondTeamMember.get() != null)
                manageRequirementMessagesAndSwapButton(shouldEnableSwapButton);
        });

        shouldEnableSwapButton.addListener(observable -> {
            Button assignButton = (Button) this.lookup("#main-button");
            assignButton.setDisable(!shouldEnableSwapButton.get());
        });
    }

    private void drawActivityFailMessage(Scene container, String message) {
        IActivity.drawActivityMessageOnException(container, this, message);
        drawButtonBasedOnContext(container, true);
    }

    private void drawWidgetsForSwappingStudentsTask(double initialWidth) {
        drawTeamSelectionAreasFor(FIRST_TEAM, initialWidth);
        drawTeamSelectionAreasFor(SECOND_TEAM, initialWidth);
    }

    private void drawTeamSelectionAreasFor(String teamOrder, double initialWidth) {
        IActivity.removeElementIfExists("team-dropdown-" + teamOrder, this);

        ComboBox<String> teamDropdown = new ComboBox<>();
        teamDropdown.setId("team-dropdown-" + teamOrder);

        List<String> dropdownItems = new ArrayList<String>() {{ add("Select team"); }};
        prepareTeamDropdownItems(teamDropdown, dropdownItems, teamOrder);

        teamDropdown.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 0 && teamOrder.equals(FIRST_TEAM)) firstTeamInSwap.set(null);
            if (newValue.intValue() == 0 && teamOrder.equals(SECOND_TEAM)) secondTeamInSwap.set(null);

            String selectedTeamId = Helpers.getIdFromCompact(dropdownItems.get(newValue.intValue()));
            AtomicReference<Team> selectedTeam = new AtomicReference<>();
            observableTeams.get().forEach(t -> {if ((t.getId() + "").equals(selectedTeamId)) selectedTeam.set(t);} );

            if (newValue.intValue() != 0 && teamOrder.equals(FIRST_TEAM))
                firstTeamInSwap.set(selectedTeam.get());
            if (newValue.intValue() != 0 && teamOrder.equals(SECOND_TEAM))
                secondTeamInSwap.set(selectedTeam.get());

            drawTeamSelectionAreasFor(teamOrder.equals(FIRST_TEAM) ? SECOND_TEAM : FIRST_TEAM, initialWidth);
        }));

        drawTeamSelectionFragment(teamDropdown, teamOrder, initialWidth);
    }

    private void prepareTeamDropdownItems(ComboBox<String> dropdown, List<String> dropdownItems, String teamOrder) {
        observableTeams.getValue().forEach(t -> {
            if (teamOrder.equals(FIRST_TEAM) && secondTeamInSwap.get() != null
                && secondTeamInSwap.get().getId() == t.getId()
            ) return;

            if (teamOrder.equals(SECOND_TEAM) && firstTeamInSwap.get() != null
                && firstTeamInSwap.get().getId() == t.getId()
            ) return;

            dropdownItems.add(t.compact());
        });

        dropdown.getItems().addAll(dropdownItems);
        dropdown.setValue(
            teamOrder.equals(SECOND_TEAM) ? (
                secondTeamInSwap.get() == null
                    ? dropdownItems.get(0)
                    : dropdownItems.get(dropdownItems.indexOf(secondTeamInSwap.get().compact()))
            ) : (
                firstTeamInSwap.get() == null
                    ? dropdownItems.get(0)
                    : dropdownItems.get(dropdownItems.indexOf(firstTeamInSwap.get().compact()))
            )
        );
    }

    private void drawTeamSelectionFragment(ComboBox<String> dropdown, String teamOrder, double initialWidth) {
        String order = teamOrder.equals(FIRST_TEAM) ? "first" : "second";
        double leftAnchor = teamOrder.equals(FIRST_TEAM) ? MARGIN : initialWidth + MARGIN * 2;

        Label selectTeamLabel = new Label("Select the " + order + " Team in Swap");
        this.getChildren().add(selectTeamLabel);

        selectTeamLabel.getStyleClass().add("subtitle");
        AnchorPane.setLeftAnchor(selectTeamLabel, leftAnchor);
        AnchorPane.setTopAnchor(selectTeamLabel, MARGIN * 3.5);

        dropdown.getStyleClass().add("dropdown-select");
        this.getChildren().add(dropdown);

        dropdown.setPrefWidth(initialWidth);
        AnchorPane.setTopAnchor(dropdown, MARGIN * 4.5);
        AnchorPane.setLeftAnchor(dropdown, leftAnchor);

        this.prefWidthProperty().addListener(((observable, oldValue, newValue) -> {
            dropdown.setPrefWidth(initialWidth + IActivity.offset(oldValue, newValue));

            if (teamOrder.equals(SECOND_TEAM)) {
                AnchorPane.setLeftAnchor(selectTeamLabel, AnchorPane.getLeftAnchor(selectTeamLabel) + IActivity.offset(oldValue, newValue));
                AnchorPane.setLeftAnchor(dropdown, AnchorPane.getLeftAnchor(dropdown) + IActivity.offset(oldValue, newValue));
            }
        }));
    }

    private void drawMemberSelectionFragment(List<Student> members, String teamOrder, double initialWidth) {
        double leftAnchor = teamOrder.equals(FIRST_TEAM) ? MARGIN : initialWidth + MARGIN * 2;

        Label memberSelectLabel = new Label("Select a member to swap");
        memberSelectLabel.setId("member-select-label" + teamOrder);
        memberSelectLabel.getStyleClass().add("subtitle");
        this.getChildren().add(memberSelectLabel);

        AnchorPane.setTopAnchor(memberSelectLabel, MARGIN * 7);
        AnchorPane.setLeftAnchor(memberSelectLabel, leftAnchor);

        ComboBox<String> membersDropdown = new ComboBox<>();
        membersDropdown.getStyleClass().add("dropdown-select");
        membersDropdown.setId("member-select-dropdown-" + teamOrder);

        membersDropdown.getItems().add("Select member");
        members.forEach(m -> membersDropdown.getItems().add(m.display()));
        membersDropdown.setVisibleRowCount(members.size() + 1);
        membersDropdown.setPrefWidth(initialWidth);

        AnchorPane.setTopAnchor(membersDropdown, MARGIN * 8);
        AnchorPane.setLeftAnchor(membersDropdown, leftAnchor);

        membersDropdown.getSelectionModel().select(0);
        this.getChildren().add(membersDropdown);

        this.prefWidthProperty().addListener((observable, oldValue, newValue) -> {
            if (teamOrder.equals(SECOND_TEAM)) {
                AnchorPane.setLeftAnchor(memberSelectLabel, AnchorPane.getLeftAnchor(memberSelectLabel) + IActivity.offset(oldValue, newValue));
                AnchorPane.setLeftAnchor(membersDropdown, AnchorPane.getLeftAnchor(membersDropdown) + IActivity.offset(oldValue, newValue));
            }

            memberSelectLabel.setPrefWidth(initialWidth + IActivity.offset(oldValue, newValue));
            membersDropdown.setPrefWidth(initialWidth + IActivity.offset(oldValue, newValue));
        });

        membersDropdown.getSelectionModel().selectedIndexProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal.intValue() == 0 && teamOrder.equals(FIRST_TEAM)) firstTeamMember.set(null);
            if (newVal.intValue() == 0 && teamOrder.equals(SECOND_TEAM)) secondTeamMember.set(null);
            if (newVal.intValue() != 0 && teamOrder.equals(FIRST_TEAM)) firstTeamMember.set(members.get(newVal.intValue() - 1));
            if (newVal.intValue() != 0 && teamOrder.equals(SECOND_TEAM)) secondTeamMember.set(members.get(newVal.intValue() - 1));
        });
    }

    private void eraseMemberSelectionFragment(String teamOrder) {
        IActivity.removeElementIfExists("member-select-label" + teamOrder, this);
        IActivity.removeElementIfExists("member-select-dropdown-" + teamOrder, this);

        if (teamOrder.equals(FIRST_TEAM)) firstTeamMember.set(null);
        else secondTeamMember.set(null);
    }

    private void manageRequirementMessagesAndSwapButton(
        SimpleBooleanProperty shouldEnableSwapButton
    ) {
        IActivity.removeElementIfExists("requirement-label", this);
        Label requirementLabel = new Label();
        requirementLabel.setId("requirement-label");
        this.getChildren().add(requirementLabel);

        requirementLabel.getStyleClass().add("requirement-message");
        requirementLabel.setPrefWidth(this.getPrefWidth() / SharedConstants.GUI_ASPECT_RATIO);

        AnchorPane.setBottomAnchor(requirementLabel, MARGIN * 4);
        AnchorPane.setLeftAnchor(requirementLabel, (this.getPrefWidth() - requirementLabel.getPrefWidth()) / 2);

        this.prefWidthProperty().addListener(((observable, oldValue, newValue) -> {
                requirementLabel.setPrefWidth(requirementLabel.getPrefWidth() + IActivity.offset(oldValue, newValue));
                AnchorPane.setLeftAnchor(requirementLabel, AnchorPane.getLeftAnchor(requirementLabel) + IActivity.offset(oldValue, newValue));
        }));

        Pair<Pair<Boolean, String>, Pair<Boolean, String>> requirements = LogicalAssistant.isTeamRequirementsMutuallySatisfied(
            new Pair<>(firstTeamInSwap.get(), firstTeamMember.get()), new Pair<>(secondTeamInSwap.get(), secondTeamMember.get())
        );

        Pair<Pair<Boolean, List<PERSONALITIES>>, Pair<Boolean, List<PERSONALITIES>>> personalityCheck =
                LogicalAssistant.isImbalancePersonalityOnSwap(
                    new Pair<>(firstTeamInSwap.get(), firstTeamMember.get()), new Pair<>(secondTeamInSwap.get(), secondTeamMember.get())
                );

        if (requirements != null || personalityCheck != null) {
            String message = SharedConstants.EMPTY_STRING;
            boolean isSwappable = true;

            if (requirements != null) {
                isSwappable = false;
                message += "Your selections are not suitable for swap:\n";

                Pair<Boolean, String> firstTeamRequirements = requirements.getKey();
                Pair<Boolean, String> secondTeamRequirements = requirements.getValue();

                if (firstTeamRequirements != null) {
                    message += firstTeamRequirements.getKey() ? "The left Team needs a Leader type member.\n" : SharedConstants.EMPTY_STRING;

                    if (firstTeamRequirements.getValue() != null)
                    message += firstTeamRequirements.getValue().contains(secondTeamMember.get().getUniqueId())
                            ? "The left Team has conflict with the selected member from the right Team.\n" : SharedConstants.EMPTY_STRING;
                }

                if (secondTeamRequirements != null) {
                    message += secondTeamRequirements.getKey() ? "The right Team needs a Leader type member.\n" : SharedConstants.EMPTY_STRING;

                    if (secondTeamRequirements.getValue() != null)
                        message += secondTeamRequirements.getValue().contains(firstTeamMember.get().getUniqueId())
                            ? "The right Team has conflict with the selected member from the left Team.\n" : SharedConstants.EMPTY_STRING;
                }
            }

            if (personalityCheck != null) {
                String swappable = "The selected Teams and members are good for a swap.\nIn your next swap, please note:\n\n";
                Pair<Boolean, List<PERSONALITIES>> firstTeamCheck = personalityCheck.getKey();
                Pair<Boolean, List<PERSONALITIES>> secondTeamCheck = personalityCheck.getValue();

                if (firstTeamCheck != null)
                    message += firstTeamCheck.getKey()
                            ? "The left Team must have the following Personality: " + firstTeamCheck.getValue().toString() + ".\n"
                            : (firstTeamCheck.getValue() != null && firstTeamCheck.getValue().size() != 0
                            ? swappable + "The left Team has rooms for the following Personalities: " + firstTeamCheck.getValue().toString() + ".\n"
                            : SharedConstants.EMPTY_STRING);

                if (secondTeamCheck != null)
                    message += secondTeamCheck.getKey()
                            ? "The right Team must have the following Personality: " + secondTeamCheck.getValue().toString() + ".\n"
                            : (secondTeamCheck.getValue() != null && secondTeamCheck.getValue().size() != 0
                            ? "The right Team has rooms for the following Personalities: " + secondTeamCheck.getValue().toString() + ".\n"
                            : SharedConstants.EMPTY_STRING);

                isSwappable = isSwappable &&
                    (firstTeamCheck == null || firstTeamCheck.getKey() == null || !firstTeamCheck.getKey()) &&
                    (secondTeamCheck == null || secondTeamCheck.getKey() == null || !secondTeamCheck.getKey());
            }

            requirementLabel.setText(message);
            shouldEnableSwapButton.set(isSwappable);
        }
        else {
            requirementLabel.setText("The selected Teams and Students are good to swap.\nBoth Teams have no other requirements.");
            shouldEnableSwapButton.set(true);
        }
    }

    private void drawButtonBasedOnContext(Scene container, boolean isErrorOccurred) {
        Button backButton = new Button(isErrorOccurred ? "Okay" : "Back");
        Button swapButton = new Button("Swap");

        IActivity.drawActivityFixedButtons(container, this, isErrorOccurred, backButton, swapButton);
        setActionListenerFor(container, swapButton);

        backButton.setOnAction(event -> {
            observableTeams.set(null);
            firstTeamInSwap.set(null);
            secondTeamInSwap.set(null);

            intent.accept(SharedEnums.GUI_ACTION_CONTEXT.LAUNCH);
        });
    }

    private void setActionListenerFor(Scene container, Button swapButton) {
        swapButton.setOnAction(event -> {
            List<Project> projects = (new ProjectService()).readAllProjectsFromFile();
            List<Preference> preferences = studentService.readAllStudentPreferencesFromFile();

            if (projects == null || preferences == null)
                drawActivityFailMessage(container, "\"An error occurred while retrieving data from files.\nPlease try again.\"");
            else {
                Team firstTeam = firstTeamInSwap.get().clone();
                Student firstMember = firstTeamMember.get().clone();

                Team secondTeam = secondTeamInSwap.get().clone();
                Student secondMember = secondTeamMember.get().clone();

                firstTeam.replaceMemberByUniqueId(firstMember.getUniqueId(), secondMember);
                secondTeam.replaceMemberByUniqueId(secondMember.getUniqueId(), firstMember);

                if (firstTeam.getMembers().size() == SharedConstants.GROUP_LIMIT)
                    firstTeam.setFitnessMetrics(controllerBase.calculateTeamFitnessMetricsFor(firstTeam, projects, preferences));

                if (secondTeam.getMembers().size() == SharedConstants.GROUP_LIMIT)
                    secondTeam.setFitnessMetrics(controllerBase.calculateTeamFitnessMetricsFor(secondTeam, projects, preferences));

                boolean result = teamService.updateTeam(firstTeam);
                if (result && teamService.updateTeam(secondTeam)) {
                    observableTeams.set(null);
                    firstTeamInSwap.set(null);
                    secondTeamInSwap.set(null);

                    drawSwappingTaskContents(container, "The Students have been swapped between 2 Teams successfully.");
                }
                else
                    drawActivityFailMessage(container, "An error occurred while updating/saving data into files.\nPlease retry your task.");
            }
        });
    }

    @Override
    public <T> void setIntent(Consumer<T> callback) {
        intent = (Consumer<SharedEnums.GUI_ACTION_CONTEXT>) callback;
    }
}
