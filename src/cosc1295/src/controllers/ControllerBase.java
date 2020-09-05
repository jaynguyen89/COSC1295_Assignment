package cosc1295.src.controllers;

import cosc1295.designs.Flasher;
import cosc1295.src.models.*;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import helpers.utilities.Helpers;
import helpers.utilities.LogicalAssistant;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class ControllerBase {

    protected final Flasher flasher = Flasher.getInstance();

    /**
     * Calculates the Fitness Metrics for a Team, including Skill Competency (average and categorized x4),
     * Preference Satisfaction (team overall and 1st-2nd Projects), and Skill Shortfall (average and per-project).
     * @param team Team
     * @param projects List<Project>
     * @return TeamFitness
     */
    public TeamFitness calculateTeamFitnessMetricsFor(Team team, List<Project> projects, List<Preference> preferences) {
        //Create FitnessMetrics for Team that never has, otherwise update Team's FitnessMetrics
        TeamFitness teamFitness = team.getFitnessMetrics() == null
                ? new TeamFitness()
                : team.getFitnessMetrics();

        /* At first, all competency and satisfaction ratings/preferences are summed up into these 2 containers,
         * then use these 2 containers to calculate average competency and satisfaction metrics. */
        HashMap<SharedEnums.SKILLS, Double> skillCompetencies = new HashMap<>();
        Pair<Double, Double> satisfactions = new Pair<>(0.0, 0.0);

        for (Student member : team.getMembers()) { //Perform the sums
            HashMap<SharedEnums.SKILLS, SharedEnums.RANKINGS> memberSkillRanking = member.getSkillRanking();

            for (Map.Entry<SharedEnums.SKILLS, SharedEnums.RANKINGS> entry : memberSkillRanking.entrySet())
                if (skillCompetencies.containsKey(entry.getKey()))
                    skillCompetencies.put(
                        entry.getKey(),
                        skillCompetencies.get(entry.getKey()) + entry.getValue().getValue() //sum up competency ratings
                    );
                else
                    skillCompetencies.put(entry.getKey(), (double) entry.getValue().getValue());

            Preference memberPreferences = null;
            for (int i = preferences.size() - 1; i >= 0; i--)
                if (preferences.get(i).getStudentUniqueId().equalsIgnoreCase(member.getUniqueId()))
                    memberPreferences = preferences.get(i);

            //Sum up satisfaction preferences
            if (memberPreferences != null)
                for (Map.Entry<String, Integer> entry : memberPreferences.getPreference().entrySet()) {
                    if (entry.getKey().equals(team.getProject().getUniqueId()) && entry.getValue() == 4)
                        satisfactions = new Pair<>(satisfactions.getKey() + 1, satisfactions.getValue());

                    if (entry.getKey().equals(team.getProject().getUniqueId()) && entry.getValue() == 3)
                        satisfactions = new Pair<>(satisfactions.getKey(), satisfactions.getValue() + 1);
                }
            else
                satisfactions = null;
        }

        //Compute metrics for competency
        Pair<Double, HashMap<SharedEnums.SKILLS, Double>> computedCompetencies = computeAverageCompetencies(skillCompetencies);
        teamFitness.setAverageTeamSkillCompetency(computedCompetencies.getKey());

        HashMap<SharedEnums.SKILLS, Double> teamCompetencyBySkills = computedCompetencies.getValue();
        teamFitness.setTeamCompetency(teamCompetencyBySkills);

        //Compute metrics for satisfaction
        teamFitness.setPreferenceSatisfaction(satisfactions == null ? null : computeAverageSatisfactions(satisfactions));

        //Calculate the Skill Shortfall: average of all Projects, and per Project.
        //At first, all shortfall differences are summed up into a container, then use the container to calculate the averages
        HashMap<String, Double> skillShortFalls = new HashMap<>();
        for (Project project : projects) {
            double shortfall = 0;

            for (Map.Entry<SharedEnums.SKILLS, SharedEnums.RANKINGS> ranking : project.getSkillRanking().entrySet()) {
                double requestedSkillRanking = ranking.getValue().getValue();
                double teamSkillRanking = teamCompetencyBySkills.get(ranking.getKey());

                if (requestedSkillRanking > teamSkillRanking)
                    shortfall += abs(requestedSkillRanking - teamSkillRanking); //sum up the differences
            }

            skillShortFalls.put(project.getUniqueId(), shortfall);
        }

        teamFitness.setSkillShortFall(skillShortFalls); //Per-Project Shortfalls
        teamFitness.setAverageSkillShortfall(computeAverageTeamSkillShortFall(skillShortFalls)); //Average of all Projects

        return teamFitness;
    }

    private double computeAverageTeamSkillShortFall(HashMap<String, Double> skillShortfall) {
        double totalShortfall = 0;
        for (Map.Entry<String, Double> entry : skillShortfall.entrySet())
            totalShortfall += entry.getValue();

        return Helpers.round(totalShortfall / skillShortfall.size(), SharedConstants.DECIMAL_PRECISION);
    }

    private Pair<Double, Pair<Double, Double>> computeAverageSatisfactions(Pair<Double, Double> satisfactions) {
        return new Pair<>(
            Helpers.round(
                (satisfactions.getKey() + satisfactions.getValue()) * 100 / SharedConstants.GROUP_LIMIT,
                SharedConstants.DECIMAL_PRECISION
            ),
            new Pair<>(
                Helpers.round(satisfactions.getKey() * 100 / SharedConstants.GROUP_LIMIT, SharedConstants.DECIMAL_PRECISION),
                Helpers.round(satisfactions.getValue() * 100 / SharedConstants.GROUP_LIMIT, SharedConstants.DECIMAL_PRECISION)
            )
        );
    }

    private Pair<Double, HashMap<SharedEnums.SKILLS, Double>> computeAverageCompetencies(HashMap<SharedEnums.SKILLS, Double> competencies) {
        HashMap<SharedEnums.SKILLS, Double> averageCompetencies = new HashMap<>();

        for (Map.Entry<SharedEnums.SKILLS, Double> competency : competencies.entrySet())
            averageCompetencies.put(
                competency.getKey(),
                Helpers.round(competency.getValue() / SharedConstants.GROUP_LIMIT, SharedConstants.DECIMAL_PRECISION)
            );

        double totalCompetency = 0;
        for (Map.Entry<SharedEnums.SKILLS, Double> entry : averageCompetencies.entrySet())
            totalCompetency += entry.getValue();

        return new Pair<>(
            Helpers.round(
                totalCompetency / SharedConstants.GROUP_LIMIT * SharedEnums.getAllEnumAttributesAsList(SharedEnums.SKILLS.class).size(),
                SharedConstants.DECIMAL_PRECISION
            ),
            averageCompetencies
        );
    }

    /**
     * Calculates the Standard Deviation for Skill Competency, Preference Satisfaction and Skill Shortfalls.
     * Returns a List containing 3 numbers of double type for 3 deviations respectively.
     * @param teams List<Team>
     * @param projects List<Project>
     * @return List<Double>
     */
    public List<Double> calculateStandardDeviationsForFitnessMetrics(List<Team> teams, List<Project> projects, List<Preference> preferences) {
        double totalCompetencyAcrossProjects = 0;
        double totalSatisfactionAcrossProjects = 0;
        double totalShortfallsAcrossTeams = 0;

        //Step 1: Calculate the sums for each metric
        for (Team team : teams) {
            TeamFitness teamFitness = calculateTeamFitnessMetricsFor(team, projects, preferences);
            team.setFitnessMetrics(teamFitness);

            totalCompetencyAcrossProjects += teamFitness.getAverageTeamSkillCompetency();
            totalSatisfactionAcrossProjects += teamFitness.getPreferenceSatisfaction().getKey();
            totalShortfallsAcrossTeams += teamFitness.getAverageSkillShortfall();
        }

        //Step 2: Calculate the mean for each metric
        double averageAllTeamsCompetency = totalCompetencyAcrossProjects / teams.size();
        double averageAllTeamsSatisfaction = totalSatisfactionAcrossProjects / teams.size();
        double averageAllTeamsShortfall = totalShortfallsAcrossTeams / teams.size();

        //Step 3: Calculate the deltas and sums of deltas for each metric
        double sumCompetencyDeltaSquares = 0;
        double sumSatisfactionDeltaSquares = 0;
        double sumShortfallDeltaSquares = 0;
        for (Team team : teams) {
            double competencyDelta = abs(team.getFitnessMetrics().getAverageTeamSkillCompetency() - averageAllTeamsCompetency);
            double satisfactionDelta = abs(team.getFitnessMetrics().getPreferenceSatisfaction().getKey() - averageAllTeamsSatisfaction);
            double shortfallDelta = abs(team.getFitnessMetrics().getAverageSkillShortfall() - averageAllTeamsShortfall);

            sumCompetencyDeltaSquares += (competencyDelta * competencyDelta);
            sumSatisfactionDeltaSquares += (satisfactionDelta * satisfactionDelta);
            sumShortfallDeltaSquares += (shortfallDelta * shortfallDelta);
        }

        //Step 4: calculate the deviation for each metric
        double competencySD = Helpers.round(sqrt(sumCompetencyDeltaSquares / teams.size()), SharedConstants.DECIMAL_PRECISION + 1);
        double satisfactionSD = Helpers.round(sqrt(sumSatisfactionDeltaSquares / teams.size()), SharedConstants.DECIMAL_PRECISION + 1);
        double shortfallSD = Helpers.round(sqrt(sumShortfallDeltaSquares / teams.size()), SharedConstants.DECIMAL_PRECISION + 1);

        return new ArrayList<Double>() {/**
			 * 
			 */
			private static final long serialVersionUID = 2543652074411051826L;

		{ add(competencySD); add(satisfactionSD); add(shortfallSD); }}; //done
    }
}
