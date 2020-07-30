package cosc1295.src.models;

import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import helpers.commons.SharedEnums.RANKINGS;

import helpers.utilities.Helpers;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Project {

    private String uniqueId;

    private String projectTitle;

    private String briefDescription;

    private ProjectOwner projectOwner;

    private HashMap<SKILLS, RANKINGS> skillRanking;

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() { return uniqueId; }

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

    public boolean validateAndPrettifyId() {
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
}
