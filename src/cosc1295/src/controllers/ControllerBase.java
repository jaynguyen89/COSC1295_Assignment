package cosc1295.src.controllers;

import cosc1295.designs.Flasher;
import cosc1295.src.models.*;
import helpers.commons.SharedConstants;
import helpers.utilities.Helpers;
import helpers.utilities.LogicalAssistant;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class ControllerBase {

    protected final Flasher flasher = Flasher.getInstance();

    /**
     * Calculates the Standard Deviation for Skill Competency, Preference Satisfaction and Skill Shortfalls.
     * Returns a List containing 3 numbers of double type for 3 deviations respectively.
     * @param teams List<Team>
     * @param projects List<Project>
     * @return List<Double>
     */
    protected List<Double> calculateSkillCompetencyStandardDeviation(List<Team> teams, List<Project> projects, List<Preference> preferences) {
        double totalCompetencyAcrossProjects = 0;
        double totalSatisfactionAcrossProjects = 0;
        double totalShortfallsAcrossTeams = 0;

        //Step 1: Calculate the sums for each metric
        for (Team team : teams) {
            TeamFitness teamFitness = LogicalAssistant.calculateTeamFitnessMetricsFor(team, projects, preferences);
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

    public List<Double> executeStandardDeviationCalculationForTest(List<Team> teams, List<Project> projects, List<Preference> preferences) {
        return calculateSkillCompetencyStandardDeviation(teams, projects, preferences);
    }
}
