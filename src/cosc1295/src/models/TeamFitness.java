package cosc1295.src.models;

import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;

import javafx.util.Pair;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TeamFitness implements Serializable {

	private static final long serialVersionUID = 9102396648922501096L;

	private int id;
    private double averageTeamSkillCompetency;
    private HashMap<SKILLS, Double> teamCompetencyBySkills;
    private Pair<Double, Pair<Double, Double>> preferenceSatisfaction;
    private double averageSkillShortfall;
    private HashMap<String, Double> skillShortFall;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setAverageTeamSkillCompetency(double averageTeamSkillCompetency) {
        this.averageTeamSkillCompetency = averageTeamSkillCompetency;
    }

    public double getAverageTeamSkillCompetency() {
        return averageTeamSkillCompetency;
    }

    public void setTeamCompetency(HashMap<SKILLS, Double> competency) {
        teamCompetencyBySkills = competency;
    }

    public HashMap<SKILLS, Double> getTeamCompetency() {
        return teamCompetencyBySkills;
    }

    public void setCompetencyForSkill(SKILLS skill, double score) {
        teamCompetencyBySkills.put(skill, score);
    }

    public void setPreferenceSatisfaction(Pair<Double, Pair<Double, Double>> satisfaction) {
        preferenceSatisfaction = satisfaction;
    }

    public Pair<Double, Pair<Double, Double>> getPreferenceSatisfaction() {
        return preferenceSatisfaction;
    }

    public void setOverallSatisfaction(double score) {
        Pair<Double, Double> subscores = preferenceSatisfaction.getValue();
        preferenceSatisfaction = new Pair<>(score, subscores);
    }

    public void setFirstPreferenceSatisfaction(double score) {
        Pair<Double, Double> subscores = preferenceSatisfaction.getValue();
        double secondPreferenceSatisfaction = subscores.getValue();

        preferenceSatisfaction = new Pair<>(
            preferenceSatisfaction.getKey(),
            new Pair<>(score, secondPreferenceSatisfaction)
        );
    }

    public void setSecondPreferenceSatisfaction(double score) {
        Pair<Double, Double> subscores = preferenceSatisfaction.getValue();
        double firstPreferenceSatisfaction = subscores.getKey();

        preferenceSatisfaction = new Pair<>(
                preferenceSatisfaction.getKey(),
                new Pair<>(firstPreferenceSatisfaction, score)
        );
    }

    public void setAverageSkillShortfall(double score) {
        averageSkillShortfall = score;
    }

    public double getAverageSkillShortfall() {
        return averageSkillShortfall;
    }

    public void setSkillShortFall(HashMap<String, Double> scores) {
        skillShortFall = scores;
    }

    public HashMap<String, Double> getSkillShortFall() {
        return skillShortFall;
    }

    public void addSkillShortFall(String projectUniqueId, Double score) {
        skillShortFall.put(projectUniqueId, score);
    }

    /**
     * Turns data in this Fitness Metrics object into Delimiterized String to be saved into a file.
     * @return String
     */
    public String stringify() {
        StringBuilder fitnessString = new StringBuilder(
            id + SharedConstants.TEXT_DELIMITER +
            averageTeamSkillCompetency + SharedConstants.TEXT_DELIMITER
        );

        for (Map.Entry<SKILLS, Double> entry : teamCompetencyBySkills.entrySet())
            fitnessString.append(entry.getKey().name())
                         .append(SharedConstants.TEXT_DELIMITER)
                         .append(entry.getValue())
                         .append(SharedConstants.TEXT_DELIMITER);

        fitnessString.append(preferenceSatisfaction.getKey())
                     .append(SharedConstants.TEXT_DELIMITER)
                     .append(preferenceSatisfaction.getValue().getKey())
                     .append(SharedConstants.TEXT_DELIMITER)
                     .append(preferenceSatisfaction.getValue().getValue())
                     .append(SharedConstants.TEXT_DELIMITER);

        fitnessString.append(averageSkillShortfall)
                     .append(SharedConstants.TEXT_DELIMITER);

        for (Map.Entry<String, Double> entry : skillShortFall.entrySet())
            fitnessString.append(entry.getKey())
                         .append(SharedConstants.TEXT_DELIMITER)
                         .append(entry.getValue())
                         .append(SharedConstants.TEXT_DELIMITER);

        return fitnessString.toString();
    }

    /**
     * Prepare formatted String with line breaks and tabs to be printed out on console.
     * @return String
     */
    public String display() {
        StringBuilder fitnessString = new StringBuilder();

        fitnessString.append("\t\tOverall Team Skill Competency: ")
                     .append(averageTeamSkillCompetency).append("\n");
        fitnessString.append("\t\tSkill Competency By Categories: ");

        for (Map.Entry<SKILLS, Double> entry : teamCompetencyBySkills.entrySet())
            fitnessString.append(entry.getKey().name())
                         .append("(")
                         .append(entry.getValue())
                         .append(")")
                         .append("\t");

        fitnessString.append("\n\t\tOverall Team Preference Satisfaction: ")
                     .append(preferenceSatisfaction.getKey()).append("\n");
        fitnessString.append("\t\t\tFirst Preference Satisfaction: ")
                     .append(preferenceSatisfaction.getValue().getKey()).append("\n");
        fitnessString.append("\t\t\tSecond Preference Satisfaction: ")
                     .append(preferenceSatisfaction.getValue().getValue()).append("\n");

        fitnessString.append("\t\tAverage Team Skill Shortfall: ")
                     .append(averageSkillShortfall).append("\n");
        fitnessString.append("\t\tSkill Shortfalls By Projects:").append("\n");

        int i = 0;
        for (Map.Entry<String, Double> entry : skillShortFall.entrySet()) {
            fitnessString.append("\t\t\t")
                         .append(entry.getKey())
                         .append("(")
                         .append(entry.getValue())
                         .append(")")
                         .append("\t");

            if (++i % 4 == 0) fitnessString.append("\n");
        }

        return fitnessString.toString();
    }

    public TeamFitness clone() {
        TeamFitness clone = new TeamFitness();
        clone.setId(id);
        clone.setAverageTeamSkillCompetency(averageTeamSkillCompetency);
        clone.setTeamCompetency(teamCompetencyBySkills == null ? null : new HashMap<>(teamCompetencyBySkills));
        clone.setPreferenceSatisfaction(
            preferenceSatisfaction == null ? null :
            new Pair<>(
                preferenceSatisfaction.getKey(),
                new Pair<>(
                    preferenceSatisfaction.getValue().getKey(),
                    preferenceSatisfaction.getValue().getValue()
                )
            )
        );
        clone.setAverageSkillShortfall(averageSkillShortfall);
        clone.setSkillShortFall(skillShortFall == null ? null : new HashMap<>(skillShortFall));

        return clone;
    }
}
