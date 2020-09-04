package cosc1295.src.models;

import cosc1295.src.models.generic.IThing;
import cosc1295.src.models.generic.People;
import helpers.commons.SharedConstants;
import helpers.utilities.Helpers;
import javafx.util.Pair;

import java.io.Serializable;

public class ProjectOwner extends People implements IThing, Serializable {

	private static final long serialVersionUID = -3139865993517131561L;

	private String uniqueId;
    private Company company;

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getUniqueId() { return uniqueId; }

    @Override
    public Boolean isUniqueIdAvailable() {
        return Helpers.checkUniqueIdAvailableFor(this.getClass(), uniqueId);
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Boolean validateAndPrettifyUniqueId() {
        Pair<String, Boolean> validation = Helpers.validateAndPrettifyUniqueId(uniqueId);
        if (validation == null) return null;

        uniqueId = validation.getKey();
        return validation.getValue();
    }

    /**
     * Creates string formatted with delimiter to save into file
     * @return String
     */
    public String stringify() {
        return getId() + SharedConstants.TEXT_DELIMITER +
            uniqueId + SharedConstants.TEXT_DELIMITER +
            firstName + SharedConstants.TEXT_DELIMITER +
            lastName + SharedConstants.TEXT_DELIMITER +
            emailAddress + SharedConstants.TEXT_DELIMITER +
            role.getId() + SharedConstants.TEXT_DELIMITER +
            company.getId();

    }

    public ProjectOwner clone() {
        ProjectOwner clone = new ProjectOwner();
        clone.setId(getId());
        clone.setUniqueId(uniqueId);
        clone.setCompany(company == null ? null : company.clone());

        return clone;
    }
}
