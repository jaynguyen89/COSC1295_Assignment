package entry;

import cosc1295.src.controllers.CompanyController;
import cosc1295.src.controllers.ProjectOwnerController;

public class ApplicationFacade {

    private final CompanyController companyController;
    private final ProjectOwnerController projectOwnerController;

    public ApplicationFacade() {
        companyController = new CompanyController();
        projectOwnerController = new ProjectOwnerController();
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
            if (!projectOwnerController.executeAddProjectOwnerTask()) {
                //TODO
            }

            //TODO
            featureDone = true;
        }
    }

    public void runAddProjectFeature() {

    }
}
