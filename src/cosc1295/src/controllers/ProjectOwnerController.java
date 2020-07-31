package cosc1295.src.controllers;

import cosc1295.providers.interfaces.ICompanyService;
import cosc1295.providers.interfaces.IProjectOwnerService;
import cosc1295.providers.interfaces.IRoleService;
import cosc1295.providers.services.CompanyService;
import cosc1295.providers.services.ProjectOwnerService;
import cosc1295.providers.services.RoleService;
import cosc1295.src.models.Company;
import cosc1295.src.models.Flash;
import cosc1295.src.models.ProjectOwner;
import cosc1295.src.models.Role;
import cosc1295.src.views.ProjectOwnerView;
import helpers.commons.SharedEnums;

import javafx.util.Pair;
import java.util.List;

public class ProjectOwnerController extends ControllerBase {

    private final ProjectOwnerView projectOwnerView;
    private final IProjectOwnerService projectOwnerService;
    private final IRoleService roleService;
    private final ICompanyService companyService;

    public ProjectOwnerController() {
        projectOwnerView = new ProjectOwnerView();
        projectOwnerService = new ProjectOwnerService();
        roleService = new RoleService();
        companyService = new CompanyService();
    }

    public Boolean executeAddProjectOwnerTask() {
        List<Role> allRoles = roleService.readAllRolesFromFile();
        List<Company> allCompanies = companyService.readAllCompaniesFromFile();

        if (allRoles == null || allCompanies == null) {
            flasher.flash(new Flash(
                    "An error occurred while retrieving Roles and Companies data from file. Please try again.",
                    SharedEnums.FLASH_TYPES.ERROR
            ));

            return false;
        }

        Pair<ProjectOwner, Boolean> projectOwnerData = projectOwnerView.getProjectOwnerDetails(allRoles, allCompanies);
        if (projectOwnerData == null) return null;

        ProjectOwner projectOwner = projectOwnerData.getKey();
        boolean isNewRoleCreated = projectOwnerData.getValue() != null &&
                                   projectOwnerData.getValue();

        if (isNewRoleCreated &&
            roleService.saveNewRole(projectOwner.getRole())
        )
            return projectOwnerService.saveNewProjectOwner(projectOwner);

        return false;
    }

    public void displayProjectOwnerResult(Boolean taskResult) {
        projectOwnerView.printTaskResult(taskResult);
    }
}
