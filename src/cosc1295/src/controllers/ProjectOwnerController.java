package cosc1295.src.controllers;

import cosc1295.design.interfaces.IProjectOwnerService;
import cosc1295.design.services.ProjectOwnerService;
import cosc1295.src.models.ProjectOwner;
import cosc1295.src.views.ProjectOwnerView;

public class ProjectOwnerController {

    private final ProjectOwnerView projectOwnerView;
    private final IProjectOwnerService projectOwnerService;

    public ProjectOwnerController() {
        projectOwnerView = new ProjectOwnerView();
        projectOwnerService = new ProjectOwnerService();
    }

    public boolean executeAddProjectOwnerTask() {
        ProjectOwner projectOwner = projectOwnerView.getProjectOwnerDetails();
        return projectOwnerService.saveNewProjectOwner(projectOwner);
    }
}
