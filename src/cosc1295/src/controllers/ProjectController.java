package cosc1295.src.controllers;

import cosc1295.providers.services.ProjectOwnerService;
import cosc1295.providers.services.ProjectService;
import cosc1295.src.models.Flash;
import cosc1295.src.models.Project;
import cosc1295.src.models.ProjectOwner;
import cosc1295.src.views.ProjectView;
import helpers.commons.SharedEnums;

import java.util.List;

public class ProjectController extends ControllerBase {

    private final ProjectView projectView;
    private final ProjectService projectService;
    private final ProjectOwnerService projectOwnerService;

    public ProjectController() {
        projectView = new ProjectView();
        projectService = new ProjectService();
        projectOwnerService = new ProjectOwnerService();
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
        projectView.printTaskResult(taskResult);
    }
}
