package cosc1295.src.controllers;

import cosc1295.providers.services.ProjectOwnerService;
import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.src.models.Flash;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Project;
import cosc1295.src.models.ProjectOwner;
import cosc1295.src.views.ProjectView;
import helpers.commons.SharedEnums;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectController extends ControllerBase {

    private final ProjectView projectView;
    private final ProjectService projectService;
    private final ProjectOwnerService projectOwnerService;
    private final StudentService studentService;

    public ProjectController() {
        projectView = new ProjectView();
        projectService = new ProjectService();
        projectOwnerService = new ProjectOwnerService();
        studentService = new StudentService();
    }

    public Boolean executeAddProjectTask() {
        List<ProjectOwner> allProjectOwners = projectOwnerService.readAllProjectOwnersFromFile();
        if (allProjectOwners == null) {
            flasher.flash(new Flash(
                    "An error occurred while retrieving Project Owner data from file. Please try again.",
                    SharedEnums.FLASH_TYPES.ERROR
            ));

            return false;
        }

        Project newProject = projectView.getProjectDetails(allProjectOwners);
        if (newProject == null) return null;

        return projectService.saveNewProject(newProject);
    }

    public void displayAddProjectResult(boolean taskResult) {
        projectView.printAddProjectTaskResult(taskResult);
    }

    public Project getAProjectFromList(List<Project> projects) {
        return projectView.getProjectFromList(projects);
    }

    public List<Project> shortlistProjectsBasedOnPreferences() {
        List<Preference> preferences = studentService.readAllStudentPreferencesFromFile();
        List<Project> projects = projectService.readAllProjectsFromFile();

        if (preferences == null || projects == null) {
            flasher.flash(new Flash(
                "An error occurred while retrieving Project data or Preference data from file. Please try again.",
                SharedEnums.FLASH_TYPES.ERROR
            ));

            return null;
        }

        HashMap<String, Integer> projectsRating = new HashMap<>();
        for (Preference preference : preferences)
            for (Map.Entry<String, Integer> entry : preference.getPreference().entrySet())
                if (projectsRating.containsKey(entry.getKey())) {
                    int rating = projectsRating.get(entry.getKey());
                    projectsRating.put(entry.getKey(), rating + entry.getValue());
                }
                else projectsRating.put(entry.getKey(), entry.getValue());

        //get 5 minimum rated projects

        return null;
    }
}
