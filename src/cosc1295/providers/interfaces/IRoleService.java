package cosc1295.providers.interfaces;

import cosc1295.src.models.Role;

import java.util.List;

/**
 * Dependency Injection Design Pattern
 */
public interface IRoleService {

    List<Role> readAllRolesFromFile();

    boolean saveNewRole(Role role);
}
