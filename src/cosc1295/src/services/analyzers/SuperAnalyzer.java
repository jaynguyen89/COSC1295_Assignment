package cosc1295.src.services.analyzers;

import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.providers.services.TeamService;
import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.*;
import helpers.utilities.LogicalAssistant;

import javafx.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SuperAnalyzer {

    final List<Preference> preferences;
    final List<Project> projects;
    final List<Team> teams;
    final List<Student> students;

    public SuperAnalyzer() {
        preferences = (new StudentService()).readAllStudentPreferencesFromFile();
        projects = (new ProjectService()).readAllProjectsFromFile();
        teams = (new TeamService()).readAllTeamsFromFile();
        students = (new StudentService()).readAllStudentsFromFile();
        LogicalAssistant.setStudentDataInTeams(teams, students);
    }

    HashMap<Pair<Student, Student>, Double> calculateAssignMetricsAverages(
        HashMap<Pair<Student, Student>, TeamFitness> metricsData
    ) {
        HashMap<Pair<Student, Student>, Double> averages = new HashMap<>();

        for (Map.Entry<Pair<Student, Student>, TeamFitness> entry : metricsData.entrySet()) {
            TeamFitness metrics = entry.getValue();
            double entryMetricsAverage = (
                metrics.getAverageTeamSkillCompetency() +
                metrics.getPreferenceSatisfaction().getKey() +
                metrics.getAverageSkillShortfall()
            ) / 3;

            averages.put(entry.getKey(), entryMetricsAverage);
        }

        return averages;
    }

    HashMap<Pair<Student, Student>, Double> calculateSwapMetricsAverages(
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData
    ) {
        HashMap<Pair<Student, Student>, Double> averages = new HashMap<>();

        for (Map.Entry<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> entry : metricsData.entrySet()) {
            TeamFitness firstMetrics = entry.getValue().getKey();
            TeamFitness secondMetrics = entry.getValue().getValue();

            double metricsAverage = (
                (firstMetrics.getAverageTeamSkillCompetency() + secondMetrics.getAverageTeamSkillCompetency()) / 2 +
                (firstMetrics.getPreferenceSatisfaction().getKey() + secondMetrics.getPreferenceSatisfaction().getKey()) / 2 +
                (firstMetrics.getAverageSkillShortfall() + secondMetrics.getAverageSkillShortfall()) / 2
            ) / 3;

            averages.put(entry.getKey(), metricsAverage);
        }

        return averages;
    }

    boolean hasPreferenceData(Student student, List<Preference> preferences) {
        for (Preference preference : preferences)
            if (preference.getStudentUniqueId().equalsIgnoreCase(student.getUniqueId()))
                return true;

        return false;
    }

    Pair<Student, Student> produceAssignSuggestion(
        HashMap<Pair<Student, Student>, TeamFitness> metricsData
    ) {
        HashMap<Pair<Student, Student>, Double> metricsAverages = calculateAssignMetricsAverages(metricsData);
        return getStudentsPair(metricsAverages);
    }

    Pair<Student, Student> produceSwapSuggestion(
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData
    ) {
        HashMap<Pair<Student, Student>, Double> metricsAverages = calculateSwapMetricsAverages(metricsData);
        return getStudentsPair(metricsAverages);
    }

    Pair<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> produceMetricsData(
        Team firstTeam, Team secondTeam, Student firstTeamMember, Student secondTeamMember
    ) {
        ControllerBase controllerBase = new ControllerBase();

        boolean swappable = LogicalAssistant.areStudentsSwappable(
            new Pair<>(firstTeam, secondTeam),
            new Pair<>(firstTeamMember, secondTeamMember)
        );
        if (!swappable) return null;

        Team firstClone = firstTeam.clone();
        Team secondClone = secondTeam.clone();

        firstClone.replaceMemberByUniqueId(firstTeamMember.getUniqueId(), secondTeamMember);
        secondClone.replaceMemberByUniqueId(secondTeamMember.getUniqueId(), firstTeamMember);

        TeamFitness firstMetrics = controllerBase.calculateTeamFitnessMetricsFor(firstClone, projects, preferences);
        TeamFitness secondMetrics = controllerBase.calculateTeamFitnessMetricsFor(secondClone, projects, preferences);

        return new Pair(
            new Pair<>(firstTeamMember, secondTeamMember),
            new Pair<>(firstMetrics, secondMetrics)
        );
    }

    private Pair<Student, Student> getStudentsPair(HashMap<Pair<Student, Student>, Double> metricsAverages) {
        Pair<Student, Student> suggestion = null;
        double minAvg = Double.MAX_VALUE;
        for (Map.Entry<Pair<Student, Student>, Double> entry : metricsAverages.entrySet())
            if (minAvg > entry.getValue()) {
                minAvg = entry.getValue();
                suggestion = entry.getKey();
            }

        return suggestion;
    }
}
