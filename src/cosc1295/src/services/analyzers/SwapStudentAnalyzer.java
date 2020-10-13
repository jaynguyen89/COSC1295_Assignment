package cosc1295.src.services.analyzers;

import cosc1295.src.models.*;
import javafx.util.Pair;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Used for suggesting a swap when user have selected 2 Teams.
 * Return data of type Pair<Student, Student>
 * @param <T>
 */
public class SwapStudentAnalyzer<T> extends SuperAnalyzer implements Callable<T> {

    private final Team firstTeam;
    private final Team secondTeam;

    public SwapStudentAnalyzer(Team firstTeam, Team secondTeam) {
        super();
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T call() {
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData = calculateMetrics();
        return (T) produceSwapSuggestion(metricsData);
    }

    /**
     * Calculates the Fitness Metrics for each possible pair combination of Students across all Teams.
     * `Possible` means each pair is checked against all team requirements.
     * Returns HashMap of Pair<Student, Student> being the possible swap,
     * and Pair<TeamFitness, TeamFitness> being the Fitness Metrics of 2 Teams.
     * @return HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>>
     */
    private HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> calculateMetrics() {
        //First-Second respectively
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData = new HashMap<>();

        for (Student firstTeamMember : firstTeam.getMembers()) {
            if (!hasPreferenceData(firstTeamMember, preferences)) continue;

            for (Student secondTeamMember : secondTeam.getMembers()) {
                if (!hasPreferenceData(secondTeamMember, preferences)) continue;

                Pair<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metrics = produceMetricsData(
                    firstTeam, secondTeam, firstTeamMember, secondTeamMember
                );
                if (metrics == null) continue;

                metricsData.put(metrics.getKey(), metrics.getValue());
            }
        }

        return metricsData;
    }
}
