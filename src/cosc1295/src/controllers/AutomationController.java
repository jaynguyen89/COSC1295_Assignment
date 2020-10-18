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

/**
 * Supports the feature that automatically assign or swap Student to/between teams.
 * The suggestion for an assign or a swap is displayed first, so user know what is about to be done.
 * If user confirm to proceed, it will automatically assign/swap.
 * All data that have been changed will also be saved automatically.
 * User then can undo every change happened here using menu option J.
 */
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
        //Read the required data for Projects and Preferences from database
        List<Project> projects = (new ProjectService()).readAllProjectsFromFile();
        List<Preference> preferences = (new StudentService()).readAllStudentPreferencesFromFile();

        if (projects == null || preferences == null) //when data retrieval failed
            automationView.displayUrgentFailMessage();
        else { //data retrieval was successful, let user user the feature
            boolean runAutoAssign = automationView.promptForFeatureToRun();

            if (runAutoAssign) runAutoAssignFeature(projects, preferences);
            else runAutoSwapFeature(projects, preferences);
        }
    }

    /**
     * Automatically assign a Student into a Team by using the SuggestionService to analyze all data
     * and produce a prospective assignment.
     * @param projects List<Project>
     * @param preferences List<Preference>
     */
    public void runAutoAssignFeature(List<Project> projects, List<Preference> preferences) {
        boolean shouldQuit = false;

        while (!shouldQuit) {
            //Analyze all data to get an assignee, a Team and (optional) a Team's member to be replaced
            Pair<Student, Pair<Team, Student>> suggestion = suggestionService.runForResult(new AutoAssignAnalyzer<>());
            if (suggestion == null) { //no suggestion: meaning all Teams have been balanced
                shouldQuit = true;
                automationView.displayNoSuggestionMessage();
                continue;
            }

            //Prompt user if they confirm to proceed the assignment
            boolean shouldAssign = automationView.promptForAssignConfirmation(suggestion);
            if (shouldAssign) {
                //Assign the Student
                boolean assigned = LogicalAssistant.assignStudentToTeam(
                    suggestion.getValue(), suggestion.getKey(), projects, preferences
                );

                //Save changes to database
                if (assigned && teamService.updateTeam(suggestion.getValue().getKey()))
                    automationView.displaySuccessMessage();
            }

            shouldQuit = !automationView.promptForContinue();
        }
    }

    /**
     * Automatically swap Students between 2 Teams by using the SuggestionService to analyze all data
     * and produce a prospective swap.
     * @param projects List<Project>
     * @param preferences List<Preference>
     */
    public void runAutoSwapFeature(List<Project> projects, List<Preference> preferences) {
        boolean shouldQuit = false;

        while (!shouldQuit) {
            //Analyze all data to get 2 Teams including 1 member in each Team for a swap
            Pair<Pair<Team, Student>, Pair<Team, Student>> suggestion = suggestionService.runForResult(new AutoSwapAnalyzer<>());
            if (suggestion == null) { //no suggestion: meaning all Teams have been balanced
                shouldQuit = true;
                automationView.displayNoSuggestionMessage();
                continue;
            }

            //Prompt user if they confirm to proceed with the swap
            boolean shouldSwap = automationView.promptForSwapConfirmation(suggestion);
            if (shouldSwap) {
                //Swap the Students
                Pair<Team, Team> swapResults = LogicalAssistant.swapStudentsBetweenTeams(
                    suggestion.getValue(), suggestion.getKey(), projects, preferences
                );

                //Save changes to database
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
