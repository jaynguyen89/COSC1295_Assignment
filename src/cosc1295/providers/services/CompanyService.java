package cosc1295.providers.services;

import cosc1295.providers.interfaces.ICompanyService;
import cosc1295.src.models.Address;
import cosc1295.src.models.Company;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.DATA_TYPES;

import java.util.ArrayList;
import java.util.List;

public class CompanyService extends FileServiceBase implements ICompanyService {

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
                Company company = new Company();

                String[] companyTokens = rawCompany.split(SharedConstants.TEXT_DELIMITER);

                String addressId = companyTokens[companyTokens.length - 1];
                String rawAddress = lookupRawDataFromFileById(addressId.trim(), DATA_TYPES.ADDRESS);

                if (!rawAddress.isEmpty() && !rawAddress.equals(SharedConstants.NA)) {
                    String[] addressTokens = rawAddress.split(SharedConstants.TEXT_DELIMITER);
                    company.setAddress(new Address(
                        Integer.parseInt(addressTokens[0].trim()),
                        addressTokens[1].equals(SharedConstants.NA) ?
                            SharedConstants.EMPTY_STRING :
                            addressTokens[1],
                        addressTokens[2],
                        addressTokens[3],
                        addressTokens[4],
                        addressTokens[5],
                        addressTokens[6]
                    ));
                }

                company.setId(Integer.parseInt(companyTokens[0].trim()));
                company.setCompanyName(companyTokens[1]);
                company.setAbnNumber(companyTokens[2]);
                company.setWebsiteUrl(companyTokens[3]);

                companies.add(company);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            return null;
        }

        return companies;
    }
}
