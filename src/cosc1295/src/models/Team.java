package cosc1295.src.models;

import cosc1295.src.models.generic.IThing;
import helpers.commons.SharedConstants;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Team implements IThing, Serializable {

	private static final long serialVersionUID = -35799684266491980L;

	private int id;
    private transient String uniqueId; //unused attribute
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
        return members.removeIf(m -> m.getUniqueId().equals(uniqueId));
    }

    //Return true if member is replaced successfully, otherwise false
    public boolean replaceMemberByUniqueId(String uniqueId, Student other) {
        if (removeMemberByUniqueId(uniqueId))
            return members.add(other);

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

    public Team clone() {
        Team clone = new Team();
        clone.setId(id);
        clone.setFitnessMetrics(fitnessMetrics == null ? null : fitnessMetrics.clone());
        clone.setMembers(members == null ? null : new ArrayList<>(members));
        clone.setProject(project == null ? null : project.clone());
        clone.setNewlyAdded(newlyAdded);

        return clone;
    }

    public List<String> composeRaw(ResultSet rs) throws SQLException {
        List<String> data = new ArrayList<>();

        int teamIdTracker = -1;
        StringBuilder rawData = new StringBuilder(SharedConstants.EMPTY_STRING);
        while (rs.next()) {
            if (teamIdTracker != rs.getInt("id")) {
                if (rawData.length() != 0) data.add(rawData.toString());

                String metricId = rs.getString("fitness_metric_id");
                rawData = new StringBuilder(
                    rs.getInt("id") + SharedConstants.TEXT_DELIMITER +
                    rs.getInt("project_id") + SharedConstants.TEXT_DELIMITER +
                    (metricId == null ? 0 : metricId) + SharedConstants.TEXT_DELIMITER
                );
                teamIdTracker = rs.getInt("id");
            }

            rawData.append(rs.getString("unique_id"))
                    .append(SharedConstants.TEXT_DELIMITER);

            if (rs.isLast()) data.add(rawData.toString());
        }

        return data;
    }

    public String compact() {
        return "Team #" + id + ": " + SharedConstants.SPACE +
                members.size() + SharedConstants.SPACE + (
                    members.size() == 1 ? "member" : "members"
                ) + " - " + project.getUniqueId();
    }
}
