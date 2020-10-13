package cosc1295.src.services.analyzers;

import cosc1295.src.models.*;
import javafx.util.Pair;
import java.util.HashMap;
import java.util.concurrent.Callable;

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
