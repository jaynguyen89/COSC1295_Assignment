package cosc1295.src.controllers;

import cosc1295.providers.interfaces.ICompanyService;
import cosc1295.providers.services.CompanyService;
import cosc1295.src.models.Address;
import cosc1295.src.models.Company;
import cosc1295.src.views.CompanyView;

public class CompanyController extends ControllerBase {

    private final CompanyView companyView;
    AddressController addressController;
    private final ICompanyService companyService;

    public CompanyController() {
        companyView = new CompanyView();
        addressController = new AddressController();
        companyService = new CompanyService();
    }

    public boolean executeAddCompanyTask() {
        Company company = companyView.getCompanyBasicDetails();
        Address address = addressController.promptForAddressInformation();

        company.setAddress(address);
        return saveNewCompany(company);
    }

    private boolean saveNewCompany(Company company) {
        Address savedAddress = addressController.saveNewAddress(company.getAddress());
        if (savedAddress != null) {
            company.setAddress(savedAddress);
            return companyService.saveNewCompany(company);
        }

        return false;
    }

    public boolean promptToRerunAddCompanyTaskAfterFailure() {
        return companyView.promptToRerunAddCompanyTaskAfterFailure();
    }

    public void displayAddCompanyResult(boolean result) {
        companyView.printAddCompanyResult(result);
    }
}
