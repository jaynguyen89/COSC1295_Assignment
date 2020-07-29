package entry;

import cosc1295.src.controllers.CompanyController;

public class ApplicationFacade {

    private CompanyController companyController;

    public ApplicationFacade() {
        companyController = new CompanyController();
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
}
