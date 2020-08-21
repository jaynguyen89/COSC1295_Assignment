package cosc1295.src.services;

import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TeamFitnessMetricService extends Thread {

    private static final long INTERVAL = 600000L; //10 seconds
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
