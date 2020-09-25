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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For Dependency Injection
 */
public class CompanyService extends TextFileServiceBase implements ICompanyService {
    private static final Logger logger = Logger.getLogger(DatabaseContext.class.getName());

    private final DatabaseContext context;

    public CompanyService() {
        context = DatabaseContext.getInstance();
    }

    /**
     * Saves a new Company to file or database according to DATA_SOURCE.
     * Return false on failure, true on success.
     * @param company Company
     * @return boolean
     */
    @Override
    public boolean saveNewCompany(Company company) {
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            return saveEntryToTextFile(company);

        return saveEntryToDatabase(company);
    }

    /**
     * Reads all Companies from file or database according to DATA_SOURCE.
     * @return List<Company>
     */
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
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "CompanyService.readAllCompaniesFromFile : " + ex.getMessage());
            return null;
        }

        return companies;
    }

    /**
     * Checks if a Unique ID is available for creating a new Company.
     * Returns false if it's unavailable, true otherwise.
     * @param uniqueId String
     * @return boolean
     */
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
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "CompanyService.saveEntryToDatabase : " + ex.getMessage());
            return false;
        }
    }
}
