package cosc1295.src.controllers;

import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.services.HistoryService;
import cosc1295.src.views.HistoryView;
import helpers.commons.SharedConstants;

import javafx.util.Pair;
import java.util.List;

public class HistoryController extends ControllerBase {

    private final HistoryService history = HistoryService.getInstance();

    private final HistoryView historyView;
    private final TeamService teamService;

    private final List<Project> projects;
    private final List<Preference> preferences;

    public HistoryController() {
        historyView = new HistoryView();
        teamService = new TeamService();

        projects = (new ProjectService()).readAllProjectsFromFile();
        preferences = (new StudentService()).readAllStudentPreferencesFromFile();
    }

    /**
     * Called when user select "undo" from APPLICATION_MENU.
     * Revert changes by 1 step each. Allows for undoing until nothing left to undo.
     */
    public void undoLastChange() {
        if (history.isEmpty()) historyView.displayUndoEmptyMessage();

        while (!history.isEmpty()) {
            if (historyView.promptForUndoConfirmation()) {
                Pair< //Get the latest item in the history to undo
                    Pair<Team, Team>,
                    Pair<Student, Student>
                > historyItem = history.popLastChange();

                //Get data from history item
                Pair<Team, Team> teams = historyItem.getKey();
                Pair<Student, Student> students = historyItem.getValue();

                //Base on item data, determine the action type was whether Assignment or Swapping
                //Read class HistoryService for details
                if (teams.getValue() == null) undoAssignment(teams.getKey().clone(), students);
                else undoSwapping(teams, students);
            }

            if (history.isEmpty()) {
                historyView.displayUndoEmptyMessage();
                continue;
            }

            if (!historyView.promptForContinueUndo())
                break;
        }
    }

    private void undoSwapping(Pair<Team, Team> teams, Pair<Student, Student> students) {
        Team first = teams.getKey().clone(); //First team in swap
        Team second = teams.getValue().clone(); //Second team in swap

        //Swap back members between teams
        first.replaceMemberByUniqueId(students.getKey().getUniqueId(), students.getValue());
        if (!second.isNewlyAdded())
            second.replaceMemberByUniqueId(students.getValue().getUniqueId(), students.getKey());

        //Recalculate Fitness Metrics
        if (first.getMembers().size() == SharedConstants.GROUP_LIMIT)
            first.setFitnessMetrics(calculateTeamFitnessMetricsFor(first, projects, preferences));

        if (second.getMembers().size() == SharedConstants.GROUP_LIMIT)
            second.setFitnessMetrics(calculateTeamFitnessMetricsFor(second, projects, preferences));

        //Save data
        if (!teamService.updateTeam(first) || ( //if first team updated failed
                !second.isNewlyAdded() && !teamService.updateTeam(second) //if second team was not a newly created team, and updated failed
            ) || (
                second.isNewlyAdded() && !teamService.deleteTeam(second) //if second team was a newly created team, and delete failed
            )
        ) historyView.displayUndoFailMessage();
        else
            historyView.displayUndoSuccessMessage();
    }

    private void undoAssignment(Team team, Pair<Student, Student> students) {
        if (students.getValue() == null) //Student added, no member was replaced
            team.removeMemberByUniqueId(students.getKey().getUniqueId());
        else //The assignee replaced a member in team
            team.replaceMemberByUniqueId(students.getKey().getUniqueId(), students.getValue());

        //Save data
        Boolean result;
        if (team.getMembers().size() == 0)
            result = teamService.deleteTeam(team);
        else
            result = teamService.updateTeam(team);

        if (result != null && result)
            historyView.displayUndoSuccessMessage();
        else
            historyView.displayUndoFailMessage();
    }
}
