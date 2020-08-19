package cosc1295.src.controllers;

import cosc1295.providers.services.StudentService;

public class TeamFormationController extends ControllerBase {

    private final StudentService studentService;

    public TeamFormationController() {
        studentService = new StudentService();
    }

    public Boolean executeTeamSelectionTask() {
        //TODO
        return null;
    }

    public void displayTeamSelectionResult(Boolean taskResult) {
        //TODO
    }

    public Boolean executeTeamFitnessMetricsTask() {
        //TODO
        return null;
    }

    public void displayTeamFitnessMetricsResult(Boolean taskResult) {
        //TODO
    }
}
