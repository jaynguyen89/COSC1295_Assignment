package cosc1295.src.models.generic;

import cosc1295.src.models.Role;
import helpers.commons.SharedConstants;
import helpers.utilities.Helpers;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class People implements Serializable {

    protected int id;

    protected String firstName;

    protected String lastName;

    protected String emailAddress;

    protected Role role;

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + SharedConstants.SPACE + lastName;
    }

    public  void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }

    public boolean setRole(String role, boolean isId) {
        this.role = new Role();
        boolean error = false;

        if (isId) {
            if (Helpers.isIntegerNumber(role)) {
                int id = Integer.parseInt(role);

                if (id > 0) this.role.setId(id);
                else error = true;
            }
            else error = true;
        }
        else {
            error = Helpers.isNullOrBlankOrEmpty(role);

            if (!error) {
                role = Helpers.prettifyStringLiterals(role, true);
                this.role.setRole(role);
            }
        }

        return !error;
    }

    public Boolean validateAndPrettifyFirstOrLastName(String name, boolean isFirstName) {
        Boolean nameValidation = Helpers.validateLiteralName(name, true);
        if (nameValidation == null || !nameValidation)
            return nameValidation;

        name = Helpers.prettifyStringLiterals(name, true);
        if (isFirstName) firstName = name;
        else lastName = name;

        return true;
    }

    public Boolean validateAndPrettifyEmailAddress() {
        if (Helpers.isNullOrBlankOrEmpty(emailAddress))
            return null;

        emailAddress = Helpers.prettifyStringLiterals(emailAddress, false);
        emailAddress = emailAddress.toLowerCase();

        Pattern emailRegex = Pattern.compile("^[\\w._\\-]+@[\\w\\-]+.[\\w]{2,5}+(.[\\w]{2,3})?$");
        Matcher matcher = emailRegex.matcher(emailAddress);

        return matcher.matches();
    }
}
