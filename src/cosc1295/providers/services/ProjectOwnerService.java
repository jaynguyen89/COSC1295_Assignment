package cosc1295.providers.services;

import cosc1295.providers.bases.DatabaseContext;
import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IProjectOwnerService;
import cosc1295.src.models.Company;
import cosc1295.src.models.ProjectOwner;
import cosc1295.src.models.Role;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.DATA_TYPES;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * For Dependency Injection
 */
public class ProjectOwnerService extends TextFileServiceBase implements IProjectOwnerService {

    private final DatabaseContext context;

    public ProjectOwnerService() {
        context = DatabaseContext.getInstance();
    }

    @Override
    public boolean saveNewProjectOwner(ProjectOwner projectOwner) {
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            return saveEntryToTextFile(projectOwner);

        return saveEntryToDatabase(projectOwner);
    }

    @Override
    public List<ProjectOwner> readAllProjectOwnersFromFile() {
        List<String> rawProjectOwnerData;
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            rawProjectOwnerData = readAllDataFromFile(DATA_TYPES.PROJECT_OWNER);
        else
            rawProjectOwnerData = context.retrieveSimpleDataForType(ProjectOwner.class);

        if (rawProjectOwnerData == null) return null;
        if (rawProjectOwnerData.isEmpty()) return new ArrayList<>();

        List<ProjectOwner> projectOwners = new ArrayList<>();
        try {
            for (String rawProjectOwner : rawProjectOwnerData) {
                if (rawProjectOwner.isEmpty()) continue;

                String[] projectOwnerTokens = rawProjectOwner.split(SharedConstants.TEXT_DELIMITER);
                ProjectOwner projectOwner = new ProjectOwner();

                projectOwner.setId(Integer.parseInt(projectOwnerTokens[0]));
                projectOwner.setUniqueId(projectOwnerTokens[1]);
                projectOwner.setFirstName(projectOwnerTokens[2]);
                projectOwner.setLastName(projectOwnerTokens[3]);
                projectOwner.setEmailAddress(projectOwnerTokens[4]);

                Role role = new Role();
                role.setId(Integer.parseInt(projectOwnerTokens[5]));
                projectOwner.setRole(role);

                Company company = new Company();
                company.setId(Integer.parseInt(projectOwnerTokens[6]));
                projectOwner.setCompany(company);

                projectOwners.add(projectOwner);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            return null;
        }

        return projectOwners;
    }

    @Override
    public boolean isUniqueIdDuplicated(String uniqueId) {
        return SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName())
                ? isRedundantUniqueId(uniqueId, DATA_TYPES.PROJECT_OWNER)
                : context.isRedundantUniqueId(ProjectOwner.class, uniqueId);
    }

    private boolean saveEntryToTextFile(ProjectOwner projectOwner) {
        int newInstanceId = getNextEntryIdForNewEntry(DATA_TYPES.PROJECT_OWNER);
        if (newInstanceId == -1) return false;

        projectOwner.setId(newInstanceId);
        String normalizedProjectOwner = projectOwner.stringify();

        return saveEntryToFile(normalizedProjectOwner, DATA_TYPES.PROJECT_OWNER);
    }

    private boolean saveEntryToDatabase(ProjectOwner projectOwner) {
        String query = "INSERT INTO `project_owners` (`role_id`, `company_id`, `unique_id`, `first_name`, `last_name`, `email_address`)" +
                       "  VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_INSERT);
        if (statement == null) return false;

        try {
            statement.setInt(1, projectOwner.getRole().getId());
            statement.setInt(2, projectOwner.getCompany().getId());
            statement.setString(3, projectOwner.getUniqueId());
            statement.setString(4, projectOwner.getFirstName());
            statement.setString(5, projectOwner.getLastName());
            statement.setString(6, projectOwner.getEmailAddress());

            int result = context.executeDataInsertionQuery(statement);
            return result > 0;
        } catch (SQLException ex) {
            return false;
        }
    }
}
