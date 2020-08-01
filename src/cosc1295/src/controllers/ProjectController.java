package cosc1295.src.controllers;

import cosc1295.providers.services.ProjectOwnerService;
import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.src.models.*;
import cosc1295.src.views.ProjectView;
import helpers.commons.SharedEnums;
import helpers.utilities.Helpers;

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
                "\nAn error occurred while retrieving Project data or Preference data from file. Please try again.\n",
                SharedEnums.FLASH_TYPES.ERROR
            ));

            return null;
        }

        List<Preference> refinedPreferences = discardDuplicatedPreferences(preferences);

        HashMap<String, Integer> projectsRating = new HashMap<>();
        for (Preference preference : refinedPreferences)
            for (Map.Entry<String, Integer> entry : preference.getPreference().entrySet())
                if (projectsRating.containsKey(entry.getKey())) {
                    int rating = projectsRating.get(entry.getKey());
                    projectsRating.put(entry.getKey(), rating + entry.getValue());
                }
                else projectsRating.put(entry.getKey(), entry.getValue());

        List<Map.Entry<String, Integer>> shortlist = Helpers.sortDescending(projectsRating);
        projectView.printShortlistedProjects(shortlist);

        return null;
    }

    //Duplicated preferences are discarded except for the last added one
    private List<Preference> discardDuplicatedPreferences(List<Preference> preferences) {
        List<Preference> uniquePreferences = new ArrayList<>();

        for (int i = preferences.size() - 1; i >= 0; i--) {
            if (uniquePreferences.size() == 0)
                uniquePreferences.add(preferences.get(i));

            boolean skipThisPreference = false;
            for (Preference preference : uniquePreferences)
                if (preference.getStudentUniqueId().equals(
                        preferences.get(i).getStudentUniqueId()
                )) {
                    skipThisPreference = true;
                    break;
                }

            if (skipThisPreference) continue;
            uniquePreferences.add(preferences.get(i));
        }

        return uniquePreferences;
    }
}
