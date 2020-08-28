package cosc1295.src.models;

import cosc1295.src.models.generic.IThing;
import helpers.commons.SharedConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team implements IThing, Serializable {

	private static final long serialVersionUID = -35799684266491980L;

	private int id;
    private transient String uniqueId;
    private TeamFitness fitnessMetrics;
    private List<Student> members = new ArrayList<>();
    private Project project;
    private transient boolean newlyAdded;

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public Boolean isUniqueIdAvailable() {
        return null;
    }

    public void setFitnessMetrics(TeamFitness value) {
        fitnessMetrics = value;
    }

    public TeamFitness getFitnessMetrics() {
        return fitnessMetrics;
    }

    public List<Student> getMembers() {
        return members;
    }

    public void setMembers(List<Student> members) {
        this.members = members;
    }

    public void addMember(Student member) {
        members.add(member);
    }

    //Return true if member is removed successfully, otherwise false
    public boolean removeMemberByUniqueId(String uniqueId) {
        Student memberToRemove = null;
        for (Student member : members)
            if (member.getUniqueId().equals(uniqueId)) {
                memberToRemove = member;
                break;
            }

        if (memberToRemove != null) {
            members.remove(memberToRemove);
            return true;
        }

        return false;
    }

    //Return true if member is replaced successfully, otherwise false
    public boolean replaceMemberByUniqueId(String uniqueId, Student other) {
        Student memberToReplace = null;
        for (Student member : members)
            if (member.getUniqueId().equals(uniqueId)) {
                memberToReplace = member;
                break;
            }

        if (memberToReplace != null) {
            members.set(members.indexOf(memberToReplace), other);
            return true;
        }

        return false;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setNewlyAdded(boolean value) {
        newlyAdded = value;
    }

    public boolean isNewlyAdded() {
        return newlyAdded;
    }

    @Override
    public String stringify() {
        StringBuilder teamString = new StringBuilder(
            id + SharedConstants.TEXT_DELIMITER +
            project.getId() + SharedConstants.TEXT_DELIMITER +
            (fitnessMetrics != null ? fitnessMetrics.getId() : 0) + SharedConstants.TEXT_DELIMITER
        );

        for (Student member : members)
            teamString.append(member.getUniqueId())
                      .append(SharedConstants.TEXT_DELIMITER);

        return teamString.toString();
    }

    public String display() {
        return "Team #" + id + ": " + project.getUniqueId() + "\t" + project.getProjectTitle();
    }
}
