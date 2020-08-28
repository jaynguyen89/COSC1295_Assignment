package cosc1295.src.models;

import helpers.commons.SharedConstants;
import helpers.utilities.Helpers;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Address implements Serializable {

	private static final long serialVersionUID = -8536053812128714190L;
	
	private int id;
    private String building;
    private String street;
    private String suburb;
    private String state;
    private String postCode;
    private String country;

    public Address() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public void setBuilding(String building) {
        this.building = building;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void prettifyBuilding() {
        if (!Helpers.isNullOrBlankOrEmpty(building))
            building = Helpers.prettifyStringLiterals(building, false);
    }

    public boolean validateAndPrettifyStreet() {
        if (Helpers.isNullOrBlankOrEmpty(street))
            return false;

        street = Helpers.prettifyStringLiterals(street, true);
        return true;
    }

    public boolean validateAndPrettifySuburb() {
        if (Helpers.isNullOrBlankOrEmpty(suburb))
            return false;

        suburb = Helpers.prettifyStringLiterals(suburb, true);
        return true;
    }

    public boolean validateAndPrettifyState() {
        if (Helpers.isNullOrBlankOrEmpty(state))
            return false;

        state = Helpers.prettifyStringLiterals(state, false);
        state = state.toUpperCase();
        return true;
    }

    public boolean validateAndPrettifyPostCode() {
        if (Helpers.isNullOrBlankOrEmpty(postCode))
            return false;

        postCode = Helpers.prettifyStringLiterals(postCode, false);

        Pattern postCodeRegex = Pattern.compile("^[\\w\\s]+$");
        Matcher matcher = postCodeRegex.matcher(postCode);

        return matcher.matches();
    }

    public boolean validateAndPrettifyCountry() {
        if (Helpers.isNullOrBlankOrEmpty(country))
            return false;

        country = Helpers.prettifyStringLiterals(country, true);
        return true;
    }

    /**
     * Creates string formatted with delimiter to save into file
     * @return String
     */
    public String stringify() {
        return id + SharedConstants.TEXT_DELIMITER +
               ((building.isEmpty() ? SharedConstants.NA : building) + SharedConstants.TEXT_DELIMITER) +
               street + SharedConstants.TEXT_DELIMITER +
               suburb + SharedConstants.TEXT_DELIMITER +
               state + SharedConstants.TEXT_DELIMITER +
               postCode + SharedConstants.TEXT_DELIMITER +
               country;
    }

    /**
     * Creates a string information of Address to print out on console
     * @return String
     */
    public String prettify() {
        String sBuilding = building.isEmpty() ? SharedConstants.EMPTY_STRING : building.concat(", ");

        return sBuilding + street.concat(", ") + suburb.concat(", ") +
               state.concat(" ") + postCode.concat(", ") + country;
    }
}
