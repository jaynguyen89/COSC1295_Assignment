package cosc1295.design.services;

import cosc1295.design.interfaces.ICompanyManagementService;
import cosc1295.src.models.Company;
import helpers.commons.SharedEnums.DATA_TYPES;

public class CompanyManagementService extends ServiceBase implements ICompanyManagementService {

    @Override
    public boolean writeNewCompanyToFile(Company company) {
        int newInstanceId = getNextInstanceIdForNewEntry(DATA_TYPES.COMPANY);
        if (newInstanceId == -1) return false;

        company.setId(newInstanceId);
        String normalizedCompany = company.stringify();

        return writeToFile(normalizedCompany, DATA_TYPES.COMPANY);
    }
}
