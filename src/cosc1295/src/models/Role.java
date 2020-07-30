package cosc1295.src.models;

import helpers.commons.SharedConstants;

public class Role {

    private int id;

    private String role;

    public Role() { }

    public Role(int id, String role) {
        this.id = id;
        this.role = role;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() { return role; }

    public String stringify() {
        return id + SharedConstants.TEXT_DELIMITER + role;
    }
}
