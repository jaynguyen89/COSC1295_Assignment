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

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getCompanyName() { return companyName; }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAbnNumber() { return abnNumber; }

    public void setAbnNumber(String abnNumber) {
        this.abnNumber = abnNumber;
    }

    public String getWebsiteUrl() { return websiteUrl; }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public Boolean validateAndPrettifyCompanyName() {
        companyName = Helpers.prettifyStringLiterals(companyName, true);
        return Helpers.validateLiteralName(companyName, false);
    }

    public Boolean validateAndPrettifyAbnNumber() {
        if (Helpers.isNullOrBlankOrEmpty(abnNumber))
            return null;

        abnNumber = Helpers.prettifyStringLiterals(abnNumber, false);
        abnNumber = abnNumber.toUpperCase();
        return abnNumber.matches("^[\\w ]+$");
    }

    public Boolean validateWebsiteURL() {
        if (Helpers.isNullOrBlankOrEmpty(websiteUrl))
            return null;

        websiteUrl = Helpers.prettifyStringLiterals(websiteUrl, false);

        Pattern urlRegex = Pattern.compile("^(http://|https://)?(www.)?([\\w-]+).[\\w-]*.[a-z]{2,3}+(.[a-z]{2,4})?$");
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
