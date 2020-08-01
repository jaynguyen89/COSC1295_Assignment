package cosc1295.providers.interfaces;

import cosc1295.src.models.Company;

import java.util.List;

/**
 * Dependency Injection Design Pattern
 */
public interface ICompanyService {

    boolean saveNewCompany(Company company);

    List<Company> readAllCompaniesFromFile();

    boolean isUniqueIdDuplicated(String uniqueId);
}