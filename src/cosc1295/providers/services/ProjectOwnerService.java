package cosc1295.providers.services;

import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IProjectOwnerService;
import cosc1295.src.models.Company;
import cosc1295.src.models.ProjectOwner;
import cosc1295.src.models.Role;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.DATA_TYPES;

import java.util.ArrayList;
import java.util.List;

/**
 * For Dependency Injection
 */
public class ProjectOwnerService extends TextFileServiceBase implements IProjectOwnerService {

    @Override
    public boolean saveNewProjectOwner(ProjectOwner projectOwner) {
        int newInstanceId = getNextInstanceIdForNewEntry(DATA_TYPES.PROJECT_OWNER);
        if (newInstanceId == -1) return false;

        projectOwner.setId(newInstanceId);
        String normalizedProjectOwner = projectOwner.stringify();

        return writeToFile(normalizedProjectOwner, DATA_TYPES.PROJECT_OWNER);
    }

    @Override
    public List<ProjectOwner> readAllProjectOwnersFromFile() {
        List<String> rawProjectOwnerData = readEntireRawDataFromFile(DATA_TYPES.PROJECT_OWNER);

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
        return isRedundantUniqueId(uniqueId, DATA_TYPES.PROJECT_OWNER);
    }
}
