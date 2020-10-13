package cosc1295.src.controllers;

import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.services.HistoryService;
import cosc1295.src.views.UndoView;
import helpers.commons.SharedConstants;
import javafx.util.Pair;

import java.util.List;

public class UndoController extends ControllerBase {

    private final HistoryService history = HistoryService.getInstance();

    private final UndoView undoView;
    private final TeamService teamService;

    private final List<Project> projects;
    private final List<Preference> preferences;

    public UndoController() {
        undoView = new UndoView();
        teamService = new TeamService();

        projects = (new ProjectService()).readAllProjectsFromFile();
        preferences = (new StudentService()).readAllStudentPreferencesFromFile();
    }

    public void undoLastChange() {
        if (undoView.promptForUndoConfirmation()) {
            Pair<
                Pair<Team, Team>,
                Pair<Student, Student>
            > historyItem = history.getLastChangeAndRemove();

            Pair<Team, Team> teams = historyItem.getKey();
            Pair<Student, Student> students = historyItem.getValue();

            if (teams.getValue() == null) undoAssignment(teams.getKey().clone(), students);
            else undoSwapping(teams, students);
        }
    }

    private void undoSwapping(Pair<Team, Team> teams, Pair<Student, Student> students) {
        Team first = teams.getKey().clone();
        Team second = teams.getValue().clone();

        first.replaceMemberByUniqueId(students.getKey().getUniqueId(), students.getValue());
        second.replaceMemberByUniqueId(students.getValue().getUniqueId(), students.getKey());

        if (first.getMembers().size() == SharedConstants.GROUP_LIMIT)
            first.setFitnessMetrics(calculateTeamFitnessMetricsFor(first, projects, preferences));

        if (second.getMembers().size() == SharedConstants.GROUP_LIMIT)
            second.setFitnessMetrics(calculateTeamFitnessMetricsFor(second, projects, preferences));

        if (!teamService.updateTeam(first) || !teamService.updateTeam(second))
            undoView.displayUndoFailMessage();
        else
            undoView.displayUndoSuccessMessage();
    }

    private void undoAssignment(Team team, Pair<Student, Student> students) {
        if (students.getValue() == null)
            team.removeMemberByUniqueId(students.getKey().getUniqueId());
        else
            team.replaceMemberByUniqueId(students.getKey().getUniqueId(), students.getValue());

        Boolean result = null;
        if (team.getMembers().size() == 0)
            result = teamService.deleteTeam(team);
        else
            result = teamService.updateTeam(team);

        if (result != null && result)
            undoView.displayUndoSuccessMessage();
        else
            undoView.displayUndoFailMessage();
    }
}
