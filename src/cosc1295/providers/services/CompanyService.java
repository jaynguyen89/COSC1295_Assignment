package cosc1295.providers.services;

import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.ICompanyService;
import cosc1295.src.models.Address;
import cosc1295.src.models.Company;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.DATA_TYPES;

import java.util.ArrayList;
import java.util.List;

public class CompanyService extends TextFileServiceBase implements ICompanyService {

    @Override
    public boolean saveNewCompany(Company company) {
        int newInstanceId = getNextInstanceIdForNewEntry(DATA_TYPES.COMPANY);
        if (newInstanceId == -1) return false;

        company.setId(newInstanceId);
        String normalizedCompany = company.stringify();

        return writeToFile(normalizedCompany, DATA_TYPES.COMPANY);
    }

    @Override
    public List<Company> readAllCompaniesFromFile() {
        List<String> rawCompanyData = readEntireRawDataFromFile(DATA_TYPES.COMPANY);

        if (rawCompanyData == null) return null;
        if (rawCompanyData.isEmpty()) return new ArrayList<>();

        List<Company> companies = new ArrayList<>();
        try {
            for (String rawCompany : rawCompanyData) {
                if (rawCompany.isEmpty()) continue;

                Company company = new Company();
                String[] companyTokens = rawCompany.split(SharedConstants.TEXT_DELIMITER);

                company.setId(Integer.parseInt(companyTokens[0].trim()));
                company.setUniqueId(companyTokens[1]);
                company.setCompanyName(companyTokens[2]);
                company.setAbnNumber(companyTokens[3]);
                company.setWebsiteUrl(companyTokens[4]);

                Address address = new Address();
                address.setId(Integer.parseInt(companyTokens[5]));
                company.setAddress(address);

                companies.add(company);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            return null;
        }

        return companies;
    }

    @Override
    public boolean isUniqueIdDuplicated(String uniqueId) {
        return isRedundantUniqueId(uniqueId, DATA_TYPES.COMPANY);
    }
}
