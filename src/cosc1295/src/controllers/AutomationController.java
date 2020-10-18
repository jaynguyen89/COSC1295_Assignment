package cosc1295.src.controllers;

import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.services.SuggestionService;
import cosc1295.src.services.analyzers.AutoAssignAnalyzer;
import cosc1295.src.services.analyzers.AutoSwapAnalyzer;
import cosc1295.src.views.AutomationView;
import helpers.utilities.LogicalAssistant;
import javafx.util.Pair;

import java.util.List;

public class AutomationController extends ControllerBase {

    private final AutomationView automationView;
    private final SuggestionService suggestionService;
    private final TeamService teamService;

    public AutomationController() {
        automationView = new AutomationView();
        suggestionService = new SuggestionService();
        teamService = new TeamService();
    }

    public void executeAutoAssignSwapFeatures() {
        List<Project> projects = (new ProjectService()).readAllProjectsFromFile();
        List<Preference> preferences = (new StudentService()).readAllStudentPreferencesFromFile();

        if (projects == null || preferences == null)
            automationView.displayUrgentFailMessage();
        else {
            boolean runAutoAssign = automationView.promptForFeatureToRun();

            if (runAutoAssign) runAutoAssignFeature(projects, preferences);
            else runAutoSwapFeature(projects, preferences);
        }
    }

    public void runAutoAssignFeature(List<Project> projects, List<Preference> preferences) {
        boolean shouldQuit = false;

        while (!shouldQuit) {
            Pair<Student, Pair<Team, Student>> suggestion = suggestionService.runForResult(new AutoAssignAnalyzer<>());
            if (suggestion == null) {
                shouldQuit = true;
                automationView.displayNoSuggestionMessage();
                continue;
            }

            boolean shouldAssign = automationView.promptForAssignConfirmation(suggestion);
            if (shouldAssign) {
                boolean assigned = LogicalAssistant.assignStudentToTeam(
                    suggestion.getValue(), suggestion.getKey(), projects, preferences
                );

                if (assigned && teamService.updateTeam(suggestion.getValue().getKey()))
                    automationView.displaySuccessMessage();
            }

            shouldQuit = !automationView.promptForContinue();
        }
    }

    public void runAutoSwapFeature(List<Project> projects, List<Preference> preferences) {
        boolean shouldQuit = false;

        while (!shouldQuit) {
            Pair<Pair<Team, Student>, Pair<Team, Student>> suggestion = suggestionService.runForResult(new AutoSwapAnalyzer<>());
            if (suggestion == null) {
                shouldQuit = true;
                automationView.displayNoSuggestionMessage();
                continue;
            }

            boolean shouldSwap = automationView.promptForSwapConfirmation(suggestion);
            if (shouldSwap) {
                Pair<Team, Team> swapResults = LogicalAssistant.swapStudentsBetweenTeams(
                    suggestion.getValue(), suggestion.getKey(), projects, preferences
                );

                if (swapResults != null &&
                    teamService.updateTeam(swapResults.getKey()) &&
                    teamService.updateTeam(swapResults.getValue())
                )
                    automationView.displaySuccessMessage();
            }

            shouldQuit = !automationView.promptForContinue();
        }
    }
}
