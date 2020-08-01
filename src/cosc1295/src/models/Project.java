package cosc1295.src.models;

import cosc1295.src.models.generic.IThing;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import helpers.commons.SharedEnums.RANKINGS;
import helpers.utilities.Helpers;

import javafx.util.Pair;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Project implements IThing, Serializable {

    private int id;

    private String uniqueId;

    private String projectTitle;

    private String briefDescription;

    private ProjectOwner projectOwner;

    private HashMap<SKILLS, RANKINGS> skillRanking;

    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public int getId() { return id; }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getUniqueId() { return uniqueId; }

    @Override
    public Boolean isUniqueIdAvailable() {
        return Helpers.checkUniqueIdAvailableFor(this.getClass(), uniqueId);
    }

    public void setProjectTitle(String title) {
        projectTitle = title;
    }

    public String getProjectTitle() { return projectTitle; }

    public void setBriefDescription(String desc) {
        briefDescription = desc;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setProjectOwner(ProjectOwner owner) {
        projectOwner = owner;
    }

    public ProjectOwner getProjectOwner() { return projectOwner; }

    public void setSkillRanking(HashMap<SKILLS, RANKINGS> rankings) {
        skillRanking = rankings;
    }

    public Boolean validateAndPrettifyId() {
        Pair<String, Boolean> validation = Helpers.validateAndPrettifyUniqueId(uniqueId);
        if (validation == null) return null;

        uniqueId = validation.getKey();
        return validation.getValue();
    }

    public boolean validateAndPrettifyProjectTitle() {
        if (Helpers.isNullOrBlankOrEmpty(projectTitle))
            return false;

        projectTitle = Helpers.prettifyStringLiterals(projectTitle, true);
        return true;
    }

    public boolean validateAndPrettifyProjectDescription() {
        if (Helpers.isNullOrBlankOrEmpty(briefDescription))
            return false;

        briefDescription = Helpers.prettifyStringLiterals(briefDescription, false);
        return true;
    }

    /**
     * Creates string formatted with delimiter to save into file
     * @return String
     */
    public String stringify() {
        StringBuilder projectString = new StringBuilder(
            id + SharedConstants.TEXT_DELIMITER +
            uniqueId + SharedConstants.TEXT_DELIMITER +
            projectTitle + SharedConstants.TEXT_DELIMITER +
            briefDescription + SharedConstants.TEXT_DELIMITER +
            projectOwner.getId() + SharedConstants.TEXT_DELIMITER
        );

        for (Map.Entry<SKILLS, RANKINGS> entry : skillRanking.entrySet())
            projectString.append(entry.getKey().name())
                         .append(entry.getValue().getValue() + 1)
                         .append(SharedConstants.TEXT_DELIMITER);

        return projectString.toString();
    }

    public String display() {
        return id + ". " + uniqueId + "\t\t" + projectTitle;
    }
}
