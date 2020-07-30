package cosc1295.providers.interfaces;

import cosc1295.src.models.Role;

import java.util.List;

public interface IRoleService {

    List<Role> readAllRolesFromFile();

    boolean saveNewRole(Role role);
}
