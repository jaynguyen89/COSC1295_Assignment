package cosc1295.src.models;

import helpers.commons.SharedConstants;
import helpers.utilities.Helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Company {

    private int id;
    private String companyName;
    private String abnNumber;
    private String websiteUrl;
    private Address address;

    public void setId(int id) { this.id = id; }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setAbnNumber(String abnNumber) {
        this.abnNumber = abnNumber;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public boolean validateAndPrettifyCompanyName() {
        if (Helpers.isNullOrBlankOrEmpty(companyName))
            return false;

        companyName = Helpers.prettifyStringLiterals(companyName, false);
        return true;
    }

    public boolean validateAndPrettifyAbnNumber() {
        if (Helpers.isNullOrBlankOrEmpty(abnNumber))
            return false;

        abnNumber = Helpers.prettifyStringLiterals(abnNumber, false);
        if (!abnNumber.matches("^[0-9 ]+$"))
            return false;

        return true;
    }

    public boolean validateWebsiteURL() {
        if (Helpers.isNullOrBlankOrEmpty(websiteUrl))
            return false;

        websiteUrl = Helpers.prettifyStringLiterals(websiteUrl, false);

        Pattern urlRegex = Pattern.compile("^(http://|https://)?(www.)?([a-zA-Z0-9-]+).[a-zA-Z0-9-]*.[a-z]{2,3}\\.([a-z]{2,4})?$");
        Matcher matcher = urlRegex.matcher(websiteUrl);

        return matcher.matches();
    }

    public String stringify() {
        return id + SharedConstants.TEXT_DELIMITER +
                companyName + SharedConstants.TEXT_DELIMITER +
                abnNumber + SharedConstants.TEXT_DELIMITER +
                websiteUrl + SharedConstants.TEXT_DELIMITER +
                address.getId();
    }
}
