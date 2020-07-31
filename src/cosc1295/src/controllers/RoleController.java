package cosc1295.src.controllers;

import cosc1295.providers.interfaces.IRoleService;
import cosc1295.providers.services.RoleService;
import cosc1295.src.models.Role;

import java.util.List;

public class RoleController extends ControllerBase {

    private final IRoleService roleService;

    public RoleController() {
        roleService = new RoleService();
    }

    public List<Role> retrieveAllRoles() {
        return roleService.readAllRolesFromFile();
    }
}
