package cosc1295.providers.services;

import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IRoleService;
import cosc1295.src.models.Role;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;

import java.util.ArrayList;
import java.util.List;

public class RoleService extends TextFileServiceBase implements IRoleService {

    @Override
    public List<Role> readAllRolesFromFile() {
        List<String> rawRoleData = readEntireRawDataFromFile(SharedEnums.DATA_TYPES.ROLE);

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
        int newInstanceId = getNextInstanceIdForNewEntry(SharedEnums.DATA_TYPES.ROLE);
        if (newInstanceId == -1) return false;

        role.setId(newInstanceId);
        String normalizedRole = role.stringify();

        return writeToFile(normalizedRole, SharedEnums.DATA_TYPES.ROLE);
    }
}
