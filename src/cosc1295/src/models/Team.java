package cosc1295.src.models;

import cosc1295.src.models.generic.IThing;

import java.io.Serializable;
import java.util.List;

public class Team implements IThing, Serializable {

    private int id;

    private String uniqueId;

    private double fitnessMetrics;

    private List<Student> members;

    private Project project;

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

    public void setFitnessMetrics(double value) {
        fitnessMetrics = value;
    }

    public double getFitnessMetrics() {
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

    public boolean removeMemberByUniqueId(String uniqueId) {
        //TODO
        return false;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public String stringify() {
        //TODO
        return null;
    }
}
