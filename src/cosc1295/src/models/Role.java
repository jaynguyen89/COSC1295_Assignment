package cosc1295.src.models;

import helpers.commons.SharedConstants;

import java.io.Serializable;

public class Role implements Serializable {

	private static final long serialVersionUID = -6076212206068900878L;

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

    /**
     * Creates string formatted with delimiter to save into file
     * @return String
     */
    public String stringify() {
        return id + SharedConstants.TEXT_DELIMITER + role;
    }
}
