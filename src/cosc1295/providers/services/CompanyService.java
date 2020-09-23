package cosc1295.providers.services;

import cosc1295.providers.bases.DatabaseContext;
import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.ICompanyService;
import cosc1295.src.models.Address;
import cosc1295.src.models.Company;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.DATA_TYPES;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * For Dependency Injection
 */
public class CompanyService extends TextFileServiceBase implements ICompanyService {

    private final DatabaseContext context;

    public CompanyService() {
        context = DatabaseContext.getInstance();
    }

    @Override
    public boolean saveNewCompany(Company company) {
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            return saveEntryToTextFile(company);

        return saveEntryToDatabase(company);
    }

    @Override
    public List<Company> readAllCompaniesFromFile() {
        List<String> rawCompanyData;
        if (SharedConstants.DATA_SOURCE.equals(DatabaseContext.class.getSimpleName()))
            rawCompanyData = context.retrieveSimpleDataForType(Company.class);
        else
            rawCompanyData = readAllDataFromFile(DATA_TYPES.COMPANY);

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
        return SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName())
                ? isRedundantUniqueId(uniqueId, DATA_TYPES.COMPANY)
                : context.isRedundantUniqueId(Company.class, uniqueId);
    }

    private boolean saveEntryToTextFile(Company company) {
        int newInstanceId = getNextEntryIdForNewEntry(DATA_TYPES.COMPANY);
        if (newInstanceId == -1) return false;

        company.setId(newInstanceId);
        String normalizedCompany = company.stringify();

        return saveEntryToFile(normalizedCompany, DATA_TYPES.COMPANY);
    }

    private boolean saveEntryToDatabase(Company company) {
        String query = "INSERT INTO `companies` (`address_id`, `unique_id`, `company_name`, `abn_number`, `website_url`)" +
                       "  VALUES (?, ?, ?, ?, ?)";

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_INSERT);
        if (statement == null) return false;

        try {
            statement.setInt(1, company.getAddress().getId());
            statement.setString(2, company.getUniqueId());
            statement.setString(3, company.getCompanyName());
            statement.setString(4, company.getAbnNumber());
            statement.setString(5, company.getWebsiteUrl());

            int result = context.executeDataInsertionQuery(statement);
            return result > 0;
        } catch (SQLException ex) {
            return false;
        }
    }
}
