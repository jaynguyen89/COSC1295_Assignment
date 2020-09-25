package cosc1295.src.services;

import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import helpers.commons.SharedConstants;
import helpers.utilities.LogicalAssistant;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TeamFitnessMetricService extends Thread {

    private static final long INTERVAL = 900000L; //15 minutes
    private final Timer timer = new Timer();

    private final StudentService studentService;
    private final TeamService teamService;
    private final ProjectService projectService;

    public TeamFitnessMetricService() {
        super(TeamFitnessMetricService.class.getSimpleName() + (new Date()).getTime());

        studentService = new StudentService();
        teamService = new TeamService();
        projectService = new ProjectService();
    }

    private void computeTeamFitnessMetricsInBackground() {
        List<Team> teams = teamService.readAllTeamsFromFile();
        List<Student> students = studentService.readAllStudentsFromFile();
        List<Project> projects = projectService.readAllProjectsFromFile();
        List<Preference> preferences = studentService.readAllStudentPreferencesFromFile();

        if (teams != null && students != null && projects != null && preferences != null) {
            LogicalAssistant.setStudentDataInTeams(teams, students);

            for (Team team : teams)
                if (team.getMembers().size() == SharedConstants.GROUP_LIMIT) {
                    team.setFitnessMetrics((new ControllerBase()).calculateTeamFitnessMetricsFor(team, projects, preferences));
                    teamService.updateTeam(team);
                }
        }
    }

    @Override
    public void run() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                computeTeamFitnessMetricsInBackground();
            }
        }, 0, INTERVAL);
    }
}
