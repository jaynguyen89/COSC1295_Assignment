package cosc1295.src.models;

import cosc1295.src.models.generic.People;
import helpers.commons.SharedConstants;
import helpers.utilities.Helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectOwner extends People {

    private String uniqueId;

    private Company company;

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean validateAndPrettifyUniqueId() {
        if (Helpers.isNullOrBlankOrEmpty(uniqueId))
            return false;

        uniqueId = uniqueId.trim()
                .replaceAll(
                    SharedConstants.MULTIPLE_SPACE,
                    SharedConstants.EMPTY_STRING
                )
                .toUpperCase();

        Pattern idRegex = Pattern.compile("^[\\w]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = idRegex.matcher(uniqueId);

        return matcher.matches();
    }

    public String stringify() {
        return id + SharedConstants.TEXT_DELIMITER +
            firstName + SharedConstants.TEXT_DELIMITER +
            lastName + SharedConstants.TEXT_DELIMITER +
            emailAddress + SharedConstants.TEXT_DELIMITER +
            role.getId() + SharedConstants.TEXT_DELIMITER +
            uniqueId + SharedConstants.TEXT_DELIMITER +
            company.getId();

    }
}
