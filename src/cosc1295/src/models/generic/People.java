package cosc1295.src.models.generic;

public class People {

    protected int id;

    protected String firstName;

    protected String lastName;

    protected String emailAddress;

    protected String role;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public  void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
