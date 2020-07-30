package cosc1295.designs;

import cosc1295.src.controllers.CompanyController;
import cosc1295.src.controllers.ProjectController;
import cosc1295.src.controllers.ProjectOwnerController;

public class ApplicationFacade {

    private final CompanyController companyController;
    private final ProjectOwnerController projectOwnerController;
    private final ProjectController projectController;

    public ApplicationFacade() {
        companyController = new CompanyController();
        projectOwnerController = new ProjectOwnerController();
        projectController = new ProjectController();
    }

    public void runAddCompanyFeature() {
        boolean featureDone = false;

        while (!featureDone) {
            if (!companyController.executeAddCompanyTask()) {
                if (companyController.promptToRerunAddCompanyTaskAfterFailure())
                    continue;

                companyController.displayAddCompanyResult(false);
                featureDone = true;
                continue;
            }

            companyController.displayAddCompanyResult(true);
            featureDone = true;
        }
    }

    public void runAddProjectOwnerFeature() {
        boolean featureDone = false;

        while (!featureDone) {
            Boolean taskResult = projectOwnerController.executeAddProjectOwnerTask();
            if (taskResult == null) {
                featureDone = true;
                continue;
            }

            projectOwnerController.displayProjectOwnerResult(taskResult);
            featureDone = true;
        }
    }

    public void runAddProjectFeature() {
        boolean taskResult = projectController.executeAddProjectTask();
        projectController.displayAddProjectResult(taskResult);
    }
}
