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

    /**
     * Calculates the Fitness Metrics for each possible pair combination of Students accross all Teams.
     * `Possible` means each pair is checked against all team requirements.
     * Returns HashMap of Pair<Student, Student> being the possible swap,
     * and Pair<TeamFitness, TeamFitness> being the Fitness Metrics of 2 Teams.
     * @return HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>>
     */
    HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> calculateSwapMetrics() {
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData = new HashMap<>();

        for (int i = 0; i < teams.size(); i++) {
            Team first = teams.get(i);

            for (int j = i + 1; j < teams.size(); j++) {
                Team second = teams.get(j);

                for (Student firstMember : first.getMembers())
                    for (Student secondMember : second.getMembers()) {
                        Pair<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metrics = produceMetricsData(
                                first, second, firstMember, secondMember
                        );
                        if (metrics == null) continue;

                        metricsData.put(metrics.getKey(), metrics.getValue());
                    }
            }

            if (i == teams.size() - 2) break;
        }

        return metricsData;
    }

    Pair<Team, Team> findTeamsByMembers(Pair<Student, Student> members) {
        Team first = null;
        Team second = null;

        for (Team team : teams) {
            for (Student member : team.getMembers()) {
                if (member.getUniqueId().equalsIgnoreCase(members.getKey().getUniqueId())) {
                    first = team;
                    break;
                }

                if (member.getUniqueId().equalsIgnoreCase(members.getValue().getUniqueId())) {
                    second = team;
                    break;
                }
            }

            if (first != null && second != null) break;
        }

        return new Pair<>(first, second);
    }

    /**
     * Calculates the averages of of the Fitness Metrics for each Team.
     * Used for AssignStudentAnalyzer to produce a suggestion.
     * Returns HashMap with Key being a pair of Students that can be assigned/replaced,
     * and Value being the averages for later comparisons.
     * @param metricsData HashMap<Pair<Student, Student>, TeamFitness>
     * @return HashMap<Pair<Student, Student>, Double>
     */
    <T> HashMap<T, Pair<Double, Double>> calculateAssignMetricsAverages(HashMap<T, TeamFitness> metricsData) {
        HashMap<T, Pair<Double, Double>> averages = new HashMap<>();

        for (Map.Entry<T, TeamFitness> entry : metricsData.entrySet()) {
            TeamFitness metrics = entry.getValue();
            double entryMetricsAverage = (
                metrics.getAverageTeamSkillCompetency() +
                metrics.getPreferenceSatisfaction().getKey()
            ) / 3;

            averages.put(entry.getKey(), new Pair<>(entryMetricsAverage, metrics.getAverageSkillShortfall()));
        }

        return averages;
    }

    /**
     * Calculates the averages of of the Fitness Metrics for each Team.
     * Used for TeamsSwapAnalyzer, SecondTeamAnalyzer, SwapStudentAnalyser to produce a suggestion.
     * Returns HashMap with Key being a pair of Students that can be assigned/replaced,
     * and Value being the averages for later comparisons.
     * @param metricsData HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>>
     * @return HashMap<Pair<Student, Student>, Double>
     */
    HashMap<Pair<Student, Student>, Pair<Double, Double>> calculateSwapMetricsAverages(
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData
    ) {
        HashMap<Pair<Student, Student>, Pair<Double, Double>> averages = new HashMap<>();

        for (Map.Entry<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> entry : metricsData.entrySet()) {
            TeamFitness firstMetrics = entry.getValue().getKey();
            TeamFitness secondMetrics = entry.getValue().getValue();

            double metricsAverage = (
                (firstMetrics.getAverageTeamSkillCompetency() + secondMetrics.getAverageTeamSkillCompetency()) / 2 +
                (firstMetrics.getPreferenceSatisfaction().getKey() + secondMetrics.getPreferenceSatisfaction().getKey()) / 2
            ) / 3;

            averages.put(
                entry.getKey(),
                new Pair<>(
                    metricsAverage,
                    (firstMetrics.getAverageSkillShortfall() + secondMetrics.getAverageSkillShortfall()) / 2
                )
            );
        }

        return averages;
    }

    /**
     * Checks if a Student has its data for Project Preference or it's missing.
     * @param student Student
     * @param preferences List<Preference>
     * @return boolean
     */
    boolean hasPreferenceData(Student student, List<Preference> preferences) {
        for (Preference preference : preferences)
            if (preference.getStudentUniqueId().equalsIgnoreCase(student.getUniqueId()))
                return true;

        return false;
    }

    /**
     * Returns a suggestion for assigning Student to a Team, with the Key being
     * the Student to be assign, and Value (if not null) being a Team's member possibly replaced.
     * @param metricsData HashMap<Pair<Student, Student>, TeamFitness>
     * @return Pair<Student, Student>
     */
    <T> T produceAssignSuggestion(HashMap<T, TeamFitness> metricsData) {
        HashMap<T, Pair<Double, Double>> metricsAverages = calculateAssignMetricsAverages(metricsData);
        return getPairOnLowestMetricsAverages(metricsAverages);
    }

    /**
     * Returns the suggestion to use. Key must hold a Student (assignee), Value (nullable) is a member to be replaced.
     * @param metricsData HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>>
     * @return Pair<Student, Student>
     */
    Pair<Student, Student> produceSwapSuggestion(
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData
    ) {
        HashMap<Pair<Student, Student>, Pair<Double, Double>> metricsAverages = calculateSwapMetricsAverages(metricsData);
        return getPairOnLowestMetricsAverages(metricsAverages);
    }

    /**
     * Calculates the Fitness Metrics for each pair of Students.
     * Each pair combination of Students can be a suggestion.
     * @param firstTeam Team
     * @param secondTeam Team
     * @param firstTeamMember Student
     * @param secondTeamMember Student
     * @return Pair<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>>
     */
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

    /**
     * Inspects the averages of Fitness Metrics of each team to get a Student pair for suggestion.
     * In the metrics averages, there are 2 groups:
     * The Average of Skill Competency and Preference Satisfaction: the larger, the better.
     * The Average of Skill Shortfall: the lower, the better.
     * So the Heuristic solution is: calculate the sum of metrics averages for each entry, then compute the average of all the sums,
     * and select the entry that has its sum nearest to the average of all sums. If more than 1 entry have the same absolute difference to
     * the average of all sums, select entry having smallest average of skill shortfall (same as having largest average of SK and PS).
     * For example:
     * E1. 19 3.7 22.7 -4.12,
     * E2. 16 3.9 19.9 -1.32,
     * E3. 15 4.9 19.9 1.32,
     * E4. 14 3.3 17.3 1.28,
     * E5. 10 3.1 13.1 5.48
     * Average = 18.58. E2 and E3 have the same difference to the average. Select E2.
     * @param metricsAverages HashMap<T, Pair<Double, Double>>
     * @param <T> Type
     * @return T
     */
    private <T> T getPairOnLowestMetricsAverages(HashMap<T, Pair<Double, Double>> metricsAverages) {
        T suggestion = null;
        double sumOfAverages = 0.0;

        //Sum up all the averages in the map
        for (Map.Entry<T, Pair<Double, Double>> entry : metricsAverages.entrySet())
            sumOfAverages += (entry.getValue().getKey() + entry.getValue().getValue());

        //Select an entry from the map for suggestion
        double difference = 0.0;
        double previousShortfallAvg = 0.0;
        boolean firstEntry = true;
        for (Map.Entry<T, Pair<Double, Double>> entry : metricsAverages.entrySet()) {
            if (firstEntry) { //on first entry, only calculate the different
                difference = Math.abs(entry.getValue().getKey() + entry.getValue().getValue() - sumOfAverages);
                firstEntry = false;
                continue;
            }

            //from 2nd entry, start selecting a suggestion
            double temp = Math.abs(entry.getValue().getKey() + entry.getValue().getValue() - sumOfAverages);
            if (temp < difference) { //select entry having sum of its averages closest to `difference`
                previousShortfallAvg = entry.getValue().getValue();
                difference = temp;
                suggestion = entry.getKey();
            }

            //in case 2 entries having their sum of averages the same distance to the `difference`,
            //select entry having lowest skill shortfall average (it's same as having largest SK and PS averages)
            if (temp == difference && entry.getValue().getValue() < previousShortfallAvg)
                suggestion = entry.getKey();
        }

        return suggestion;
    }
}
