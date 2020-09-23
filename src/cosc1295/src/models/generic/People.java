package cosc1295.src.models.generic;

import cosc1295.src.models.Role;
import helpers.commons.SharedConstants;
import helpers.utilities.Helpers;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class People implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2845987566274710291L;

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

    public String getEmailAddress() {
        return emailAddress;
    }

    public  void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }

    /**
     * The string `role` can be `id` or `uniqueId` attribute in the Role class.
     * The boolean `isId` tells which attribute it is intended to use.
     * If it is `id`, make a number then set to Role `id`, otherwise,
     * reprocess it then set to role name. Return error if the string `role` conflicts with boolean `isId`.
     * @param role String
     * @param isId boolean
     * @return boolean
     */
    public boolean setRole(String role, boolean isId) {
        this.role = new Role();
        boolean error = false;

        if (isId) {
            if (Helpers.isIntegerNumber(role)) {
                int id = Integer.parseInt(role);

                if (id > 0) this.role.setId(id);
                else error = true;
            }
            else error = true; //Not a number
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
