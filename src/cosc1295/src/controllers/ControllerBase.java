package cosc1295.src.controllers;

import cosc1295.designs.Flasher;
import cosc1295.providers.services.StudentService;
import cosc1295.src.models.*;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import helpers.utilities.Helpers;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;

class ControllerBase {

    protected final Flasher flasher = Flasher.getInstance();
    private final StudentService studentService = new StudentService();

    protected TeamFitness calculateTeamFitnessMetricsFor(Team team, List<Project> projects) {
        TeamFitness teamFitness = team.getFitnessMetrics() == null
                                  ? new TeamFitness()
                                  : team.getFitnessMetrics();

        HashMap<SharedEnums.SKILLS, Double> skillCompetencies = new HashMap<>();
        Pair<Double, Double> satisfactions = new Pair<>(0.0, 0.0);

        for (Student member : team.getMembers()) {
            HashMap<SharedEnums.SKILLS, SharedEnums.RANKINGS> memberSkillRanking = member.getSkillRanking();

            for (Map.Entry<SharedEnums.SKILLS, SharedEnums.RANKINGS> entry : memberSkillRanking.entrySet())
                if (skillCompetencies.containsKey(entry.getKey()))
                    skillCompetencies.put(
                            entry.getKey(),
                            skillCompetencies.get(entry.getKey()) + entry.getValue().getValue()
                    );
                else
                    skillCompetencies.put(entry.getKey(), (double) entry.getValue().getValue());

            Preference memberPreferences = studentService.retrievePreferenceForStudent(member.getUniqueId());
            for (Map.Entry<String, Integer> entry : memberPreferences.getPreference().entrySet()) {
                if (entry.getKey().equals(team.getProject().getUniqueId()) && entry.getValue() == 4)
                    satisfactions = new Pair<>(satisfactions.getKey() + 1, satisfactions.getValue());

                if (entry.getKey().equals(team.getProject().getUniqueId()) && entry.getValue() == 3)
                    satisfactions = new Pair<>(satisfactions.getKey(), satisfactions.getValue() + 1);
            }
        }

        Pair<Double, HashMap<SharedEnums.SKILLS, Double>> computedCompetencies = computeAverageCompetencies(skillCompetencies);
        teamFitness.setAverageTeamSkillCompetency(computedCompetencies.getKey());

        HashMap<SharedEnums.SKILLS, Double> teamCompetencyBySkills = computedCompetencies.getValue();
        teamFitness.setTeamCompetency(teamCompetencyBySkills);

        teamFitness.setPreferenceSatisfaction(computeAverageSatisfactions(satisfactions));

        HashMap<String, Double> skillShortFalls = new HashMap<>();
        for (Project project : projects) {
            double shortfall = 0;

            for (Map.Entry<SharedEnums.SKILLS, SharedEnums.RANKINGS> ranking : project.getSkillRanking().entrySet()) {
                double requestedSkillRanking = ranking.getValue().getValue();
                double teamSkillRanking = teamCompetencyBySkills.get(ranking.getKey());

                if (requestedSkillRanking > teamSkillRanking)
                    shortfall += abs(requestedSkillRanking - teamSkillRanking);
            }

            skillShortFalls.put(project.getUniqueId(), shortfall);
        }

        teamFitness.setSkillShortFall(skillShortFalls);

        return teamFitness;
    }

    private Pair<Double, Pair<Double, Double>> computeAverageSatisfactions(Pair<Double, Double> satisfactions) {
        return new Pair<>(
                Helpers.round((satisfactions.getKey() + satisfactions.getValue()) * 100 / SharedConstants.GROUP_LIMIT),
                new Pair<>(
                        Helpers.round(satisfactions.getKey() * 100 / SharedConstants.GROUP_LIMIT),
                        Helpers.round(satisfactions.getValue() * 100 / SharedConstants.GROUP_LIMIT)
                ));
    }

    private Pair<Double, HashMap<SharedEnums.SKILLS, Double>> computeAverageCompetencies(HashMap<SharedEnums.SKILLS, Double> competencies) {
        HashMap<SharedEnums.SKILLS, Double> averageCompetencies = new HashMap<>();

        for (Map.Entry<SharedEnums.SKILLS, Double> competency : competencies.entrySet())
            averageCompetencies.put(
                    competency.getKey(),
                    Helpers.round(competency.getValue() / SharedConstants.GROUP_LIMIT)
            );

        double totalCompetency = 0;
        for (Map.Entry<SharedEnums.SKILLS, Double> entry : averageCompetencies.entrySet())
            totalCompetency += entry.getValue();

        return new Pair<>(
                Helpers.round(totalCompetency / SharedConstants.GROUP_LIMIT),
                averageCompetencies
        );
    }
}
