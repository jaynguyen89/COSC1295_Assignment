package cosc1295.src.models;

import cosc1295.src.models.generic.People;

public class ProjectOwner extends People {

    private String uniqueId;

    private Company company;

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
