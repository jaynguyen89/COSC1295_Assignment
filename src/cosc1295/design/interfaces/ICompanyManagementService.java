package cosc1295.design.interfaces;

import cosc1295.src.models.Company;

public interface ICompanyManagementService {

    boolean writeNewCompanyToFile(Company company);
}
