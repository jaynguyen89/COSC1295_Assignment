package cosc1295.providers.services;

import cosc1295.providers.bases.DatabaseContext;
import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IRoleService;
import cosc1295.src.models.Role;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * For Dependency Injection
 */
public class RoleService extends TextFileServiceBase implements IRoleService {

    private final DatabaseContext context;

    public RoleService() {
        context = DatabaseContext.getInstance();
    }

    @Override
    public List<Role> readAllRolesFromFile() {
        List<String> rawRoleData;
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            rawRoleData = readAllDataFromFile(SharedEnums.DATA_TYPES.ROLE);
        else
            rawRoleData = context.retrieveSimpleDataForType(Role.class);

        if (rawRoleData == null) return null;
        if (rawRoleData.isEmpty()) return new ArrayList<>();

        List<Role> roles = new ArrayList<>();
        try {
            for (String rawRoleString : rawRoleData) {
                if (rawRoleString.isEmpty()) continue;

                String[] tokens = rawRoleString.split(SharedConstants.TEXT_DELIMITER);

                int roleId = Integer.parseInt(tokens[0]);
                Role role = new Role(roleId, tokens[1]);

                roles.add(role);
            }
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }

        return roles;
    }

    @Override
    public boolean saveNewRole(Role role) {
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            return saveEntryToTextFile(role);

        return saveEntryToDatabase(role);
    }

    private boolean saveEntryToTextFile(Role role) {
        int newInstanceId = getNextEntryIdForNewEntry(SharedEnums.DATA_TYPES.ROLE);
        if (newInstanceId == -1) return false;

        role.setId(newInstanceId);
        String normalizedRole = role.stringify();

        return saveEntryToFile(normalizedRole, SharedEnums.DATA_TYPES.ROLE);
    }

    private boolean saveEntryToDatabase(Role role) {
        String query = "INSERT INTO `roles` (`role`) VALUES (?)";

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_INSERT);
        if (statement == null) return false;

        try {
            statement.setString(1, role.getRole());
            int result = context.executeDataInsertionQuery(statement);

            return result > 0;
        } catch (SQLException ex) {
            return false;
        }
    }
}
